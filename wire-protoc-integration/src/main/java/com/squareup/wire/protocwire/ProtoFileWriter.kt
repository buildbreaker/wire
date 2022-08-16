package com.squareup.wire.protocwire

import com.squareup.wire.schema.FileWriter
import okio.Path

class ProtoFileWriter(
  private val response: Plugin.Response
) : FileWriter {
  override fun write(file: Path, content: String): Path {
    response.addFile(file.toString(), content)
    return file
  }
}
