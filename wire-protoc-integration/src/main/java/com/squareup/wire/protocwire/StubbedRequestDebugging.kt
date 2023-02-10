package com.squareup.wire.protocwire

import com.google.protobuf.compiler.PluginProtos
import com.squareup.wire.schema.KotlinTarget
import java.io.File
import java.io.InputStream

private val devPath = { (System.getProperty("user.home") ?: ".") + "/development/" }
// Absolute path is used because IJ and terminal has different home directories.
val stubbedRequestFile = { "${devPath()}/wire/request.binary" }

class StubbedRequestDebugging: CodeGenerator {
  companion object {
    /**
     * Helper function used to capture code generation requests for debug/break point execution.
     */
    fun debug(request: PluginProtos.CodeGeneratorRequest) {
      val directory = File(devPath())
      if (!directory.exists()) {
        throw RuntimeException("no such directory \"${directory.path}\" change the devPath in this file.")
      }
      val f = File(stubbedRequestFile())
      val folder = File(f.parent)
      folder.mkdirs()
      f.createNewFile()
      f.writeBytes(request.toByteArray())
    }

    @JvmStatic
    fun main(args: Array<String>) {
      Plugin.run(StubbedRequestDebugging(), StubbedTestEnvironment())
    }
  }

  override fun generate(
    request: PluginProtos.CodeGeneratorRequest,
    descriptorSource: Plugin.DescriptorSource,
    response: Plugin.Response
  ) {
    val parsedMap = ParameterParser.parse(request.parameter)
    val target = KotlinTargetConfig.parse(parsedMap)
    val generator = WireGenerator(target)
    generator.generate(request, descriptorSource, response)
  }
}

class StubbedTestEnvironment : Plugin.DefaultEnvironment() {
  override fun inputStream(): InputStream {
    return File(stubbedRequestFile()).inputStream()
  }
}
