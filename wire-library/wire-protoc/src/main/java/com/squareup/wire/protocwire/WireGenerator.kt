package com.squareup.wire.protocwire

import com.google.protobuf.AbstractMessage
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.DescriptorProto
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto
import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import com.google.protobuf.DescriptorProtos.SourceCodeInfo
import com.google.protobuf.Descriptors.EnumValueDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage
import com.google.protobuf.compiler.PluginProtos
import com.squareup.wire.Syntax
import com.squareup.wire.WireLogger
import com.squareup.wire.protocwire.Plugin.DescriptorSource
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
import com.squareup.wire.schema.internal.parser.ExtendElement
import com.squareup.wire.schema.internal.parser.FieldElement
import com.squareup.wire.schema.internal.parser.MessageElement
import com.squareup.wire.schema.internal.parser.OneOfElement
import com.squareup.wire.schema.internal.parser.OptionElement
import com.squareup.wire.schema.internal.parser.ProtoFileElement
import com.squareup.wire.schema.internal.parser.RpcElement
import com.squareup.wire.schema.internal.parser.ServiceElement
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
      val target = KotlinTarget(outDirectory = "")
      Plugin.run(WireGenerator(target))
    }
  }
}

private fun parseFileDescriptor(fileDescriptor: FileDescriptorProto, descs: DescriptorSource): ProtoFileElement {
  val packagePrefix = if (fileDescriptor.hasPackage()) ".${fileDescriptor.`package`}" else ""

  val imports = mutableListOf<String>()
  val publicImports = mutableListOf<String>()
  val publicImportIndices = fileDescriptor.publicDependencyList.toSet()
  for ((index, dependencyFile) in fileDescriptor.dependencyList.withIndex()) {
    if (index in publicImportIndices) {
      publicImports.add(dependencyFile)
    } else {
      imports.add(dependencyFile)
    }
  }

  val syntax = if (fileDescriptor.hasSyntax()) Syntax[fileDescriptor.syntax] else Syntax.PROTO_2
  val types = mutableListOf<TypeElement>()
  val baseSourceInfo = SourceInfo(fileDescriptor, descs)
  for ((sourceInfo, messageType) in fileDescriptor.messageTypeList.withSourceInfo(baseSourceInfo, FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER)) {
    types.add(parseMessage(sourceInfo, packagePrefix, messageType, syntax))
  }
  for ((sourceInfo, enumType) in fileDescriptor.enumTypeList.withSourceInfo(baseSourceInfo, FileDescriptorProto.ENUM_TYPE_FIELD_NUMBER)) {
    types.add(parseEnum(sourceInfo, enumType))
  }

  val services = mutableListOf<ServiceElement>()
  for ((sourceInfo, service) in fileDescriptor.serviceList.withSourceInfo(baseSourceInfo, FileDescriptorProto.SERVICE_FIELD_NUMBER)) {
    services.add(parseService(sourceInfo, service))
  }

  val extElementList = parseFields(baseSourceInfo, FileDescriptorProto.EXTENSION_FIELD_NUMBER, fileDescriptor.extensionList, emptyMap(), baseSourceInfo.descriptorSource, syntax)
  val zippedExts = fileDescriptor.extensionList.zip(extElementList) { descriptorProto, fieldElement -> descriptorProto to fieldElement }
  val extendsList = indexFieldsByExtendee(baseSourceInfo, FileDescriptorProto.EXTENSION_FIELD_NUMBER, zippedExts)

  return ProtoFileElement(
    location = Location.get(fileDescriptor.name),
    imports = imports,
    publicImports = publicImports,
    packageName = if (fileDescriptor.hasPackage()) fileDescriptor.`package` else null,
    types = types,
    services = services,
    extendDeclarations = extendsList,
    options = parseOptions(fileDescriptor.options, descs),
    syntax = syntax,
  )
}

private fun parseService(
  baseSourceInfo: SourceInfo,
  service: ServiceDescriptorProto,
): ServiceElement {
  val info = baseSourceInfo.info()
  val rpcs = mutableListOf<RpcElement>()

  for ((sourceInfo, method) in service.methodList.withSourceInfo(baseSourceInfo, ServiceDescriptorProto.METHOD_FIELD_NUMBER)) {
    rpcs.add(parseMethod(sourceInfo, method, baseSourceInfo.descriptorSource))
  }
  return ServiceElement(
    location = info.loc,
    name = service.name,
    documentation = info.comment,
    rpcs = rpcs,
    options = parseOptions(service.options, baseSourceInfo.descriptorSource)
  )
}

private fun parseMethod(
  baseSourceInfo: SourceInfo,
  method: MethodDescriptorProto,
  descs: DescriptorSource
): RpcElement {
  val rpcInfo = baseSourceInfo.info()
  return RpcElement(
    location = rpcInfo.loc,
    name = method.name,
    documentation = rpcInfo.comment,
    requestType = method.inputType,
    responseType = method.outputType,
    requestStreaming = method.clientStreaming,
    responseStreaming = method.serverStreaming,
    options = parseOptions(method.options, descs)
  )
}

private fun parseEnum(
  baseSourceInfo: SourceInfo,
  enum: EnumDescriptorProto,
): EnumElement {
  val info = baseSourceInfo.info()
  val constants = mutableListOf<EnumConstantElement>()
  for ((sourceInfo, enumValueDescriptorProto) in enum.valueList.withSourceInfo(baseSourceInfo, EnumDescriptorProto.VALUE_FIELD_NUMBER)) {
    val enumValueInfo = sourceInfo.info()
    constants.add(EnumConstantElement(
      location = enumValueInfo.loc,
      name = enumValueDescriptorProto.name,
      tag = enumValueDescriptorProto.number,
      documentation = enumValueInfo.comment,
      options = parseOptions(enumValueDescriptorProto.options, baseSourceInfo.descriptorSource)
    ))
  }
  return EnumElement(
    location = info.loc,
    name = enum.name,
    documentation = info.comment,
    options = parseOptions(enum.options, baseSourceInfo.descriptorSource),
    constants = constants,
    reserveds = emptyList()
  )
}

private fun parseMessage(baseSourceInfo: SourceInfo, packagePrefix: String, message: DescriptorProto, syntax: Syntax): MessageElement {
  val info = baseSourceInfo.info()
  val nestedTypes = mutableListOf<TypeElement>()

  val mapTypes = mutableMapOf<String, String>()
  for ((sourceInfo, nestedType) in message.nestedTypeList.withSourceInfo(baseSourceInfo, DescriptorProto.NESTED_TYPE_FIELD_NUMBER)) {
    if (nestedType.options.mapEntry) {
      val nestedTypeFullyQualifiedName = "$packagePrefix.${message.name}.${nestedType.name}"
      val keyTypeName = parseType(nestedType.fieldList[0])
      val valueTypeName = parseType(nestedType.fieldList[1])
      mapTypes[nestedTypeFullyQualifiedName] = "map<${keyTypeName}, ${valueTypeName}>"
      continue
    }
    nestedTypes.add(parseMessage(
      sourceInfo,
      "$packagePrefix.${message.name}",
      nestedType,
      syntax
    ))
  }

  for ((sourceInfo, nestedType) in message.enumTypeList.withSourceInfo(baseSourceInfo, DescriptorProto.ENUM_TYPE_FIELD_NUMBER)) {
    nestedTypes.add(parseEnum(sourceInfo, nestedType))
  }

  /**
   * This can be cleaned up a bit more but this is kept in order to localize code changes.
   *
   * There is a need to associate the FieldElement object with its file descriptor proto. There
   * is a need for adding new fields to FieldElement but that will be done later.
   */
  val fieldElementList = parseFields(baseSourceInfo, DescriptorProto.FIELD_FIELD_NUMBER, message.fieldList, mapTypes, baseSourceInfo.descriptorSource, syntax)
  val zippedFields = message.fieldList.zip(fieldElementList) { descriptorProto, fieldElement -> descriptorProto to fieldElement }
  val oneOfIndexToFields = indexFieldsByOneOf(zippedFields)
  val fields = zippedFields.filter { pair -> !pair.first.hasOneofIndex() }.map { pair -> pair.second }

  val extElementList = parseFields(baseSourceInfo, DescriptorProto.EXTENSION_FIELD_NUMBER, message.extensionList, emptyMap(), baseSourceInfo.descriptorSource, syntax)
  val zippedExts = message.extensionList.zip(extElementList) { descriptorProto, fieldElement -> descriptorProto to fieldElement }
  val extendsList = indexFieldsByExtendee(baseSourceInfo, DescriptorProto.EXTENSION_FIELD_NUMBER, zippedExts)

  return MessageElement(
    location = info.loc,
    name = message.name,
    documentation = info.comment,
    options = parseOptions(message.options, baseSourceInfo.descriptorSource),
    reserveds = emptyList(),
    fields = fields,
    nestedTypes = nestedTypes,
    oneOfs = parseOneOfs(baseSourceInfo, message.oneofDeclList, oneOfIndexToFields),
    extendDeclarations = extendsList,
    extensions = emptyList(),
    groups = emptyList(),
  )
}

private fun parseOneOfs(
  baseSourceInfo: SourceInfo,
  oneOfDeclList: List<DescriptorProtos.OneofDescriptorProto>,
  oneOfMap: Map<Int, List<FieldElement>>,
): List<OneOfElement> {
  val info = baseSourceInfo.info()
  val result = mutableListOf<OneOfElement>()
  for (oneOfIndex in oneOfMap.keys) {
    val fieldList = oneOfMap[oneOfIndex]!!
    if (fieldList.isEmpty()) {
      // This can happen for synthetic oneofs, generated for proto3 optional fields.
      // Just skip it.
      continue
    }
    result.add(OneOfElement(
      name = oneOfDeclList[oneOfIndex].name,
      documentation = info.comment,
      fields = fieldList,
      groups = emptyList(),
      options = parseOptions(oneOfDeclList[oneOfIndex].options, baseSourceInfo.descriptorSource)
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
    if (descriptor.hasOneofIndex() && !descriptor.proto3Optional) {
      val list = oneOfMap.getOrPut(descriptor.oneofIndex) { mutableListOf() }
      list.add(fieldElement)
    }
  }
  return oneOfMap
}

private fun indexFieldsByExtendee(
  baseSourceInfo: SourceInfo,
  pathTag: Int,
  extensions: List<Pair<FieldDescriptorProto, FieldElement>>
): List<ExtendElement> {
  val info = baseSourceInfo.clone()
  info.push(pathTag)

  val extendeeMap = mutableMapOf<String, MutableList<FieldElement>>()
  for ((descriptor, fieldElement) in extensions) {
    val list = extendeeMap.getOrPut(descriptor.extendee) { mutableListOf() }
    list.add(fieldElement)
  }

  return extendeeMap.entries.map {
    val blockInfo = info.infoContaining(it.value[0].location)
    ExtendElement(
      location = blockInfo.loc,
      documentation = blockInfo.comment,
      name = it.key,
      fields = it.value
    )
  }
}

private fun parseFields(
  baseSourceInfo: SourceInfo,
  tag: Int,
  fieldList: List<FieldDescriptorProto>,
  mapTypes: Map<String, String>,
  descs: DescriptorSource,
  syntax: Syntax,
): List<FieldElement> {
  val result = mutableListOf<FieldElement>()
  for ((sourceInfo, field) in fieldList.withSourceInfo(baseSourceInfo, tag)) {
    var label = parseLabel(field, syntax)
    var type = parseType(field)
    if (mapTypes.keys.contains(type)) {
      type = mapTypes[type]!!
      label = null
    }
    val info = sourceInfo.info()
    result.add(FieldElement(
      location = info.loc,
      label = label,
      type = type,
      name = field.name,
      defaultValue = if (field.hasDefaultValue()) field.defaultValue else null,
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

private fun parseLabel(field: FieldDescriptorProto, syntax: Syntax): Field.Label? {
  return when (field.label) {
    FieldDescriptorProto.Label.LABEL_REPEATED -> Field.Label.REPEATED
    FieldDescriptorProto.Label.LABEL_REQUIRED -> Field.Label.REQUIRED
    FieldDescriptorProto.Label.LABEL_OPTIONAL ->
      when {
        field.hasOneofIndex() && !field.proto3Optional -> Field.Label.ONE_OF
        syntax == Syntax.PROTO_3 && !field.hasExtendee() && !field.proto3Optional -> null
        else ->  Field.Label.OPTIONAL
      }
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
  val locs: Map<List<Int>, List<SourceCodeInfo.Location>> = makeLocationMap(fd.sourceCodeInfo.locationList)
  val baseLoc: Location = Location.get(fd.name)

  fun getLocation(path: List<Int>): LocationAndComments {
    val l = locs[path]?.firstOrNull()
    return toLocationAndComments(l)
  }

  private fun toLocationAndComments(l: SourceCodeInfo.Location?): LocationAndComments {
    val loc = if (l == null) baseLoc else baseLoc.at(l.getSpan(0)+1, l.getSpan(1)+1)
    var comment = l?.leadingComments
    if ((comment ?: "") == "") {
      comment = l?.trailingComments
    }
    return LocationAndComments(comment ?: "", loc)
  }

  fun findLocationContaining(path: List<Int>, other: Location): LocationAndComments {
    val matches = locs[path]
    if (!matches.isNullOrEmpty()) {
      for (loc: SourceCodeInfo.Location in matches) {
        val startLine = loc.getSpan(0)+1
        val startCol = loc.getSpan(1)+1
        val endLine = if (loc.spanCount > 3) loc.getSpan(2)+1 else startLine
        val endCol = loc.getSpan(if (loc.spanCount > 3) 3 else 2)+1
        if (
          (startLine < other.line || (startLine == other.line && startCol <= other.column))
          && (endLine > other.line || (endLine == other.line && endCol >= other.column))
        ) {
          // found a location that contains the given one
          return toLocationAndComments(loc)
        }
      }
    }
    // no match found
    return toLocationAndComments(null)
  }

  private fun makeLocationMap(locs: List<SourceCodeInfo.Location>): Map<List<Int>, List<SourceCodeInfo.Location>> {
    val m = mutableMapOf<List<Int>, MutableList<SourceCodeInfo.Location>>()
    for (loc in locs) {
      val path = mutableListOf<Int>()
      for (pathElem in loc.pathList) {
        path.add(pathElem)
      }
      val locList = m.getOrPut(path) { mutableListOf() }
      locList.add(loc)
    }
    return m
  }
}

private class SourceInfo(
  val helper: SourceCodeHelper,
  val descriptorSource: DescriptorSource,
  path: List<Int> = emptyList(),
) {
  constructor(
    fileDescriptor: FileDescriptorProto,
    descriptorSource: DescriptorSource,
    path: List<Int> = emptyList()
  ) : this(SourceCodeHelper(fileDescriptor), descriptorSource, path)

  private val path = mutableListOf(*path.toTypedArray())

  fun push(value: Int) {
    path.add(value)
  }

  fun info(): LocationAndComments {
    return helper.getLocation(path)
  }

  fun infoContaining(loc: Location): LocationAndComments {
    return helper.findLocationContaining(path, loc)
  }

  fun clone(): SourceInfo {
    return SourceInfo(helper, descriptorSource, listOf(*path.toTypedArray()))
  }
}

private fun <T> List<T>.withSourceInfo(sourceInfo: SourceInfo, value: Int): List<Pair<SourceInfo, T>> {
  val baseSource = sourceInfo.clone()
  val result = mutableListOf<Pair<SourceInfo, T>>()
  baseSource.push(value)
  for ((index, elem) in withIndex()) {
    val newSource = baseSource.clone()
    newSource.push(index)
    result.add(newSource to elem)
  }
  return result
}
