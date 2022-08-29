// Code generated by Wire protocol buffer compiler, do not edit.
// Source: squareup.protos.kotlin.edgecases.Recursive in edge_cases.proto
package com.squareup.protos.kotlin.edgecases

import com.squareup.wire.FieldEncoding
import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import com.squareup.wire.ProtoWriter
import com.squareup.wire.ReverseProtoWriter
import com.squareup.wire.Syntax.PROTO_2
import com.squareup.wire.WireField
import kotlin.Any
import kotlin.AssertionError
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.DeprecationLevel
import kotlin.Int
import kotlin.Long
import kotlin.Nothing
import kotlin.String
import kotlin.Unit
import kotlin.jvm.JvmField
import okio.ByteString

public class Recursive(
  @field:WireField(
    tag = 1,
    adapter = "com.squareup.wire.ProtoAdapter#INT32",
    declaredName = "value",
  )
  public val value_: Int? = null,
  @field:WireField(
    tag = 2,
    adapter = "com.squareup.protos.kotlin.edgecases.Recursive#ADAPTER",
  )
  public val recursive: Recursive? = null,
  unknownFields: ByteString = ByteString.EMPTY,
) : Message<Recursive, Nothing>(ADAPTER, unknownFields) {
  @Deprecated(
    message = "Shouldn't be used in Kotlin",
    level = DeprecationLevel.HIDDEN,
  )
  public override fun newBuilder(): Nothing = throw
      AssertionError("Builders are deprecated and only available in a javaInterop build; see https://square.github.io/wire/wire_compiler/#kotlin")

  public override fun equals(other: Any?): Boolean {
    if (other === this) return true
    if (other !is Recursive) return false
    if (unknownFields != other.unknownFields) return false
    if (value_ != other.value_) return false
    if (recursive != other.recursive) return false
    return true
  }

  public override fun hashCode(): Int {
    var result = super.hashCode
    if (result == 0) {
      result = unknownFields.hashCode()
      result = result * 37 + (value_?.hashCode() ?: 0)
      result = result * 37 + (recursive?.hashCode() ?: 0)
      super.hashCode = result
    }
    return result
  }

  public override fun toString(): String {
    val result = mutableListOf<String>()
    if (value_ != null) result += """value_=$value_"""
    if (recursive != null) result += """recursive=$recursive"""
    return result.joinToString(prefix = "Recursive{", separator = ", ", postfix = "}")
  }

  public fun copy(
    value_: Int? = this.value_,
    recursive: Recursive? = this.recursive,
    unknownFields: ByteString = this.unknownFields,
  ): Recursive = Recursive(value_, recursive, unknownFields)

  public companion object {
    @JvmField
    public val ADAPTER: ProtoAdapter<Recursive> = object : ProtoAdapter<Recursive>(
      FieldEncoding.LENGTH_DELIMITED, 
      Recursive::class, 
      "type.googleapis.com/squareup.protos.kotlin.edgecases.Recursive", 
      PROTO_2, 
      null, 
      "edge_cases.proto"
    ) {
      public override fun encodedSize(`value`: Recursive): Int {
        var size = value.unknownFields.size
        size += ProtoAdapter.INT32.encodedSizeWithTag(1, value.value_)
        size += Recursive.ADAPTER.encodedSizeWithTag(2, value.recursive)
        return size
      }

      public override fun encode(writer: ProtoWriter, `value`: Recursive): Unit {
        ProtoAdapter.INT32.encodeWithTag(writer, 1, value.value_)
        Recursive.ADAPTER.encodeWithTag(writer, 2, value.recursive)
        writer.writeBytes(value.unknownFields)
      }

      public override fun encode(writer: ReverseProtoWriter, `value`: Recursive): Unit {
        writer.writeBytes(value.unknownFields)
        Recursive.ADAPTER.encodeWithTag(writer, 2, value.recursive)
        ProtoAdapter.INT32.encodeWithTag(writer, 1, value.value_)
      }

      public override fun decode(reader: ProtoReader): Recursive {
        var value_: Int? = null
        var recursive: Recursive? = null
        val unknownFields = reader.forEachTag { tag ->
          when (tag) {
            1 -> value_ = ProtoAdapter.INT32.decode(reader)
            2 -> recursive = Recursive.ADAPTER.decode(reader)
            else -> reader.readUnknownField(tag)
          }
        }
        return Recursive(
          value_ = value_,
          recursive = recursive,
          unknownFields = unknownFields
        )
      }

      public override fun redact(`value`: Recursive): Recursive = value.copy(
        recursive = value.recursive?.let(Recursive.ADAPTER::redact),
        unknownFields = ByteString.EMPTY
      )
    }

    private const val serialVersionUID: Long = 0L
  }
}
