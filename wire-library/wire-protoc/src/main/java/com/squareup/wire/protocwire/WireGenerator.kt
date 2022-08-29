package com.squareup.wire.protocwire

import com.google.protobuf.AbstractMessage
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.SourceCodeInfo
import com.google.protobuf.Descriptors.EnumValueDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage
import com.google.protobuf.compiler.PluginProtos
import com.squareup.wire.Syntax
import com.squareup.wire.WireLogger
import com.squareup.wire.protocwire.Plugin.DescriptorSource
import com.squareup.wire.protocwire.StubbedRequestDebugging.Companion.debug
import com.squareup.wire.schema.ClaimedDefinitions
import com.squareup.wire.schema.ClaimedPaths
import com.squareup.wire.schema.CoreLoader
import com.squareup.wire.schema.EmittingRules
import com.squareup.wire.schema.ErrorCollector
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.KotlinTarget
import com.squareup.wire.schema.Linker
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.Profile
import com.squareup.wire.schema.ProfileLoader
import com.squareup.wire.schema.ProtoFile
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.SchemaHandler
import com.squareup.wire.schema.Target
import com.squareup.wire.schema.internal.parser.EnumConstantElement
import com.squareup.wire.schema.internal.parser.EnumElement
import com.squareup.wire.schema.internal.parser.FieldElement
import com.squareup.wire.schema.internal.parser.MessageElement
import com.squareup.wire.schema.internal.parser.OneOfElement
import com.squareup.wire.schema.internal.parser.OptionElement
import com.squareup.wire.schema.internal.parser.ProtoFileElement
import com.squareup.wire.schema.internal.parser.TypeElement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import okio.Path
import okio.Path.Companion.toPath

class NoOpLogger : WireLogger {
  override fun artifactHandled(outputPath: Path, qualifiedName: String, targetName: String) {}

  override fun artifactSkipped(type: ProtoType, targetName: String) {}

  override fun unusedRoots(unusedRoots: Set<String>) {}

  override fun unusedPrunes(unusedPrunes: Set<String>) {}

  override fun unusedIncludesInTarget(unusedIncludes: Set<String>) {}

  override fun unusedExcludesInTarget(unusedExcludes: Set<String>) {}
}

data class ProtocContext(
  private val response: Plugin.Response,
  override val sourcePathPaths: Set<String>
) : SchemaHandler.Context {
  override val outDirectory: Path = "".toPath()
  override val logger: WireLogger = NoOpLogger()
  override val errorCollector: ErrorCollector = ErrorCollector()
  override val emittingRules: EmittingRules = EmittingRules()
  override val claimedDefinitions: ClaimedDefinitions? = null
  override val claimedPaths: ClaimedPaths = ClaimedPaths()
  override val module: SchemaHandler.Module? = null
  override val profileLoader: ProfileLoader = object : ProfileLoader {
    private val profile = Profile()
      override fun loadProfile(name: String, schema: Schema): Profile {
        return profile
      }
    }

  override fun inSourcePath(protoFile: ProtoFile): Boolean {
    return inSourcePath(protoFile.location)
  }

  override fun inSourcePath(location: Location): Boolean {
    return location.path in sourcePathPaths
  }

  override fun createDirectories(dir: Path, mustCreate: Boolean) {
    // noop: Directory creation is handled within protoc.
  }

  override fun write(file: Path, str: String) {
    response.addFile(file.toString(), str)
  }
}

class WireGenerator(
  private val target: Target
) : CodeGenerator {
  override fun generate(request: PluginProtos.CodeGeneratorRequest, descs: DescriptorSource, response: Plugin.Response) {
    debug(request)
    val loader = CoreLoader
    val errorCollector = ErrorCollector()
    val linker = Linker(loader, errorCollector, permitPackageCycles = true, loadExhaustively = true)

    val sourcePaths = setOf(*request.fileToGenerateList.toTypedArray())
    val protoFiles = mutableListOf<ProtoFile>()
    for (fileDescriptorProto in request.protoFileList) {
      val protoFileElement = parseFileDescriptor(fileDescriptorProto, descs)
      val protoFile = ProtoFile.get(protoFileElement)
      protoFiles.add(protoFile)
    }

    try {
      val schema = linker.link(protoFiles)
      // Create a specific target and just run.
      target.newHandler().handle(schema, ProtocContext(response, sourcePaths))
    } catch (e: Throwable) {
      // Quality of life improvement.
      val current = LocalDateTime.now()
      val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
      val formatted = current.format(formatter)
      response.addFile("$formatted-error.log", e.stackTraceToString())
    }
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val target = KotlinTarget(outDirectory = "", javaInterop = true)
      Plugin.run(WireGenerator(target))
    }
  }
}

private fun parseFileDescriptor(fileDescriptor: FileDescriptorProto, descs: DescriptorSource): ProtoFileElement {
  val packagePrefix = if (fileDescriptor.hasPackage()) ".${fileDescriptor.`package`}" else ""
  val helper = SourceCodeHelper(fileDescriptor)

  val imports = mutableListOf<String>()
  val publicImports = mutableListOf<String>()
  val types = mutableListOf<TypeElement>()

  val messagePath = mutableListOf(FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER, 0)
  for ((index, messageType) in fileDescriptor.messageTypeList.withIndex()) {
    messagePath[1] = index
    types.add(parseMessage(messagePath, helper, packagePrefix, messageType, descs))
  }

  val enumPath = mutableListOf(FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER, 0)
  for ((index, enumType) in fileDescriptor.enumTypeList.withIndex()) {
    enumPath[1] = index
    types.add(parseEnum(messagePath, helper, enumType, descs))
  }

  for ((index, dependencyFile) in fileDescriptor.dependencyList.withIndex()) {
    if (index in fileDescriptor.publicDependencyList.toSet()) {
      publicImports.add(dependencyFile)
    } else {
      imports.add(dependencyFile)
    }
  }

  return ProtoFileElement(
    location = Location.get(fileDescriptor.name),
    imports = imports,
    publicImports = publicImports,
    packageName = if (fileDescriptor.hasPackage()) fileDescriptor.`package` else null,
    types = types,
    services = emptyList(),
    options = parseOptions(fileDescriptor.options, descs),
    syntax = if (fileDescriptor.hasSyntax()) Syntax[fileDescriptor.syntax] else Syntax.PROTO_2,
  )
}

private fun parseEnum(path: List<Int>, helper: SourceCodeHelper, enum: EnumDescriptorProto, descs: DescriptorSource): EnumElement {
  val info = helper.getLocation(path)
  val constants = mutableListOf<EnumConstantElement>()
  val enumPaths = mutableListOf(*path.toTypedArray())
  enumPaths.addAll(listOf(EnumDescriptorProto.VALUE_FIELD_NUMBER, 0))
  for ((index, enumValueDescriptorProto) in enum.valueList.withIndex()) {
    enumPaths[enumPaths.size - 1] = index
    val enumValueInfo = helper.getLocation(enumPaths)
    constants.add(EnumConstantElement(
      location = enumValueInfo.loc,
      name = enumValueDescriptorProto.name,
      tag = enumValueDescriptorProto.number,
      documentation = enumValueInfo.comment,
      options = parseOptions(enumValueDescriptorProto.options, descs)
    ))
  }
  return EnumElement(
    location = info.loc,
    name = enum.name,
    documentation = info.comment,
    options = parseOptions(enum.options, descs),
    constants = constants,
    reserveds = emptyList()
  )
}

private fun parseMessage(path: List<Int>, helper: SourceCodeHelper, packagePrefix: String, message: DescriptorProto, descs: DescriptorSource): MessageElement {
  val info = helper.getLocation(path)
  val nestedTypes = mutableListOf<TypeElement>()
  val nestedMessagePath = mutableListOf(*path.toTypedArray())
  nestedMessagePath.addAll(listOf(DescriptorProto.NESTED_TYPE_FIELD_NUMBER, 0))

  val mapTypes = mutableMapOf<String, String>()
  for ((index, nestedType) in message.nestedTypeList.withIndex()) {
    if (nestedType.options.mapEntry) {
      val nestedTypeFullyQualifiedName = "$packagePrefix.${message.name}.${nestedType.name}"
      val keyTypeName = parseType(nestedType.fieldList[0])
      val valueTypeName = parseType(nestedType.fieldList[1])
      mapTypes[nestedTypeFullyQualifiedName] = "map<${keyTypeName}, ${valueTypeName}>"
      continue
    }
    nestedMessagePath[nestedMessagePath.size - 1] = index
    nestedTypes.add(parseMessage(nestedMessagePath, helper, "$packagePrefix.${nestedType.name}", nestedType, descs))
  }

  val nestedEnumPath = mutableListOf(*path.toTypedArray())
  nestedEnumPath.addAll(listOf(DescriptorProto.ENUM_TYPE_FIELD_NUMBER, 0))
  for ((index, nestedType) in message.enumTypeList.withIndex()) {
    nestedEnumPath[nestedEnumPath.size - 1] = index
    nestedTypes.add(parseEnum(nestedEnumPath, helper, nestedType, descs))
  }

  val fieldElementList = parseFields(path, helper, message.fieldList, mapTypes, descs)
  val zippedFields = message.fieldList.zip(fieldElementList) { descriptorProto, fieldElement -> descriptorProto to fieldElement }
  val oneOfIndexToFields = indexFieldsByOneOf(zippedFields)
  val fields = zippedFields.filter { pair -> !pair.first.hasOneofIndex() }.map { pair -> pair.second }
  return MessageElement(
    location = info.loc,
    name = message.name,
    documentation = info.comment,
    options = parseOptions(message.options, descs),
    reserveds = emptyList(),
    fields = fields,
    nestedTypes = nestedTypes,
    oneOfs = parseOneOfs(path, helper, message.oneofDeclList, oneOfIndexToFields, descs),
    extensions = emptyList(),
    groups = emptyList(),
  )
}

private fun parseOneOfs(
  path: List<Int>, helper: SourceCodeHelper,
  oneOfDeclList: List<DescriptorProtos.OneofDescriptorProto>,
  oneOfMap: Map<Int, List<FieldElement>>,
  descs: DescriptorSource
): List<OneOfElement> {
  val info = helper.getLocation(path)
  val result = mutableListOf<OneOfElement>()
  val oneOfPath = mutableListOf(*path.toTypedArray())
  oneOfPath.addAll(listOf(DescriptorProto.ONEOF_DECL_FIELD_NUMBER, 0))
  for (oneOfIndex in oneOfMap.keys) {
    val fieldList = oneOfMap[oneOfIndex]!!
    result.add(OneOfElement(
      name = oneOfDeclList[oneOfIndex].name,
      documentation = info.comment,
      fields = fieldList,
      groups = emptyList(),
      options = parseOptions(oneOfDeclList[oneOfIndex].options, descs)
    ))
  }
  return result
}

/**
 * The association between the FieldDescriptorProto and the FieldElement is primarily have a way to
 * look up the oneof index of the field . The FieldElement data type loses this information on the conversion.
 *
 * This can be avoided if the FieldElement class contains a reference to the oneof index.
 */
private fun indexFieldsByOneOf(
  fields: List<Pair<FieldDescriptorProto, FieldElement>>
): Map<Int, List<FieldElement>> {
  val oneOfMap = mutableMapOf<Int, MutableList<FieldElement>>()
  for ((descriptor, fieldElement) in fields) {
    if (descriptor.hasOneofIndex()) {
      val list = oneOfMap.getOrPut(descriptor.oneofIndex) { mutableListOf() }
      list.add(fieldElement)
    }
  }
  return oneOfMap
}

private fun parseFields(path: List<Int>, helper: SourceCodeHelper, fieldList: List<FieldDescriptorProto>, mapTypes: MutableMap<String, String>, descs: DescriptorSource): List<FieldElement> {
  val result = mutableListOf<FieldElement>()
  val fieldPath = mutableListOf(*path.toTypedArray())
  fieldPath.addAll(listOf(DescriptorProto.FIELD_FIELD_NUMBER, 0))
  for ((index, field) in fieldList.withIndex()) {
    var label = parseLabel(field.label)
    var type = parseType(field)
    if (mapTypes.keys.contains(type)) {
      type = mapTypes[type]!!
      label = null
    }
    fieldPath[fieldPath.size - 1] = index
    val info = helper.getLocation(fieldPath)
    result.add(FieldElement(
      location = info.loc,
      label = label,
      type = type,
      name = field.name,
//      defaultValue = field.defaultValue,
      jsonName = field.jsonName,
      tag = field.number,
      documentation = info.comment,
      options = parseOptions(field.options, descs)
    ))
  }
  return result
}

private fun parseType(field: FieldDescriptorProto): String {

  return when (field.type) {
    FieldDescriptorProto.Type.TYPE_DOUBLE -> "double"
    FieldDescriptorProto.Type.TYPE_FLOAT -> "float"
    FieldDescriptorProto.Type.TYPE_INT64 -> "int64"
    FieldDescriptorProto.Type.TYPE_UINT64 -> "uint64"
    FieldDescriptorProto.Type.TYPE_INT32 -> "int32"
    FieldDescriptorProto.Type.TYPE_FIXED64 -> "fixed64"
    FieldDescriptorProto.Type.TYPE_FIXED32 -> "fixed32"
    FieldDescriptorProto.Type.TYPE_BOOL -> "bool"
    FieldDescriptorProto.Type.TYPE_STRING -> "string"
    FieldDescriptorProto.Type.TYPE_BYTES -> "bytes"
    FieldDescriptorProto.Type.TYPE_UINT32 -> "uint32"
    FieldDescriptorProto.Type.TYPE_SFIXED32 -> "sfixed32"
    FieldDescriptorProto.Type.TYPE_SFIXED64 -> "sfixed64"
    FieldDescriptorProto.Type.TYPE_SINT32 -> "sint32"
    FieldDescriptorProto.Type.TYPE_SINT64 -> "sint64"
    // Collapsing enums and messages are the same.
    FieldDescriptorProto.Type.TYPE_ENUM,
    FieldDescriptorProto.Type.TYPE_MESSAGE -> {
      field.typeName
    }
    // TODO: Figure out group types
    FieldDescriptorProto.Type.TYPE_GROUP -> ""
    else -> throw RuntimeException("else case found for ${field.type}")
  }
}

private fun parseLabel(label: FieldDescriptorProto.Label): Field.Label? {
  return when (label) {
    FieldDescriptorProto.Label.LABEL_OPTIONAL -> Field.Label.OPTIONAL
    FieldDescriptorProto.Label.LABEL_REQUIRED -> Field.Label.REQUIRED
    FieldDescriptorProto.Label.LABEL_REPEATED -> Field.Label.REPEATED
    else -> null
  }
}

private fun <T : ExtendableMessage<T>> parseOptions(options: T, descs: DescriptorSource): List<OptionElement> {
  val optDesc = options.descriptorForType
  val overrideDesc = descs.findMessageTypeByName(optDesc.fullName)
  if (overrideDesc != null) {
    val optsDm = DynamicMessage.newBuilder(overrideDesc)
      .mergeFrom(options)
      .build()
    return createOptionElements(optsDm)
  }
  return createOptionElements(options)
}

private fun createOptionElements(options: AbstractMessage): List<OptionElement> {
  val elements = mutableListOf<OptionElement>()
  for (entry in options.allFields.entries) {
    val fld = entry.key
    val name = if (fld.isExtension) fld.fullName else fld.name
    val (value, kind) = valueOf(entry.value)
    elements.add(OptionElement(name, kind, value, fld.isExtension))
  }
  return elements
}

private fun valueOf(value: Any): OptionValueAndKind {
  return when (value) {
    is Number -> OptionValueAndKind(value.toString(), OptionElement.Kind.NUMBER)
    is Boolean -> OptionValueAndKind(value.toString(), OptionElement.Kind.BOOLEAN)
    is String -> OptionValueAndKind(value, OptionElement.Kind.STRING)
    is ByteArray -> OptionValueAndKind(String(toCharArray(value)), OptionElement.Kind.STRING)
    is EnumValueDescriptor -> OptionValueAndKind(value.name, OptionElement.Kind.ENUM)
    is List<*> -> OptionValueAndKind(valueOfList(value), OptionElement.Kind.LIST)
    is AbstractMessage -> OptionValueAndKind(valueOfMessage(value), OptionElement.Kind.MAP)
    else -> throw IllegalStateException("Unexpected field value type: ${value::class.qualifiedName}")
  }
}

private fun toCharArray(bytes: ByteArray): CharArray {
  val ch = CharArray(bytes.size)
  bytes.forEachIndexed { index, element -> ch[index] = element.toInt().toChar() }
  return ch
}

private fun simpleValue(optVal: OptionValueAndKind): Any {
  return if (optVal.kind == OptionElement.Kind.BOOLEAN ||
    optVal.kind == OptionElement.Kind.ENUM ||
    optVal.kind == OptionElement.Kind.NUMBER) {
    OptionElement.OptionPrimitive(optVal.kind, optVal.value)
  } else {
    optVal.value
  }
}

private fun valueOfList(list: List<*>): List<Any> {
  val ret = mutableListOf<Any>()
  for (element in list) {
    if (element == null) {
      throw NullPointerException("list value should not contain null")
    }
    ret.add(simpleValue(valueOf(element)))
  }
  return ret
}

private fun valueOfMessage(msg: AbstractMessage): Map<String, Any> {
  val ret = mutableMapOf<String, Any>()
  for (entry in msg.allFields.entries) {
    val fld = entry.key
    val name = if (fld.isExtension) "[${fld.fullName}]" else fld.name
    ret[name] = simpleValue(valueOf(entry.value))
  }
  return ret
}

private data class OptionValueAndKind(val value: Any, val kind: OptionElement.Kind)

private data class LocationAndComments(val comment: String, val loc: Location)

private class SourceCodeHelper(
  fd: FileDescriptorProto
) {
  val locs: Map<List<Int>, SourceCodeInfo.Location> = makeLocationMap(fd.sourceCodeInfo.locationList)
  val baseLoc: Location = Location.get(fd.name)

  fun getLocation(path: List<Int>): LocationAndComments {
    val l = locs[path]
    val loc = if (l == null) baseLoc else baseLoc.at(l.getSpan(0), l.getSpan(1))
    var comment = l?.leadingComments
    if ((comment ?: "") == "") {
      comment = l?.trailingComments
    }
    return LocationAndComments(comment ?: "", loc)
  }

  private fun makeLocationMap(locs: List<SourceCodeInfo.Location>): Map<List<Int>, SourceCodeInfo.Location> {
    val m = mutableMapOf<List<Int>, SourceCodeInfo.Location>()
    for (loc in locs) {
      val path = mutableListOf<Int>()
      for (pathElem in loc.pathList) {
        path.add(pathElem)
      }
      m[path] = loc
    }
    return m
  }
}
