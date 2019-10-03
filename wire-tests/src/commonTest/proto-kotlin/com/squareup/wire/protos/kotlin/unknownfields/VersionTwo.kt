// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: unknown_fields.proto
package com.squareup.wire.protos.kotlin.unknownfields

import com.squareup.wire.FieldEncoding
import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import com.squareup.wire.ProtoWriter
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
import kotlin.collections.List
import kotlin.hashCode
import kotlin.jvm.JvmField
import okio.ByteString

class VersionTwo(
  @field:WireField(
    tag = 1,
    adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  val i: Int? = null,
  @field:WireField(
    tag = 2,
    adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  val v2_i: Int? = null,
  @field:WireField(
    tag = 3,
    adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  val v2_s: String? = null,
  @field:WireField(
    tag = 4,
    adapter = "com.squareup.wire.ProtoAdapter#FIXED32"
  )
  val v2_f32: Int? = null,
  @field:WireField(
    tag = 5,
    adapter = "com.squareup.wire.ProtoAdapter#FIXED64"
  )
  val v2_f64: Long? = null,
  @field:WireField(
    tag = 6,
    adapter = "com.squareup.wire.ProtoAdapter#STRING",
    label = WireField.Label.REPEATED
  )
  val v2_rs: List<String> = emptyList(),
  @field:WireField(
    tag = 7,
    adapter = "com.squareup.wire.protos.kotlin.unknownfields.NestedVersionTwo#ADAPTER"
  )
  val obj: NestedVersionTwo? = null,
  unknownFields: ByteString = ByteString.EMPTY
) : Message<VersionTwo, Nothing>(ADAPTER, unknownFields) {
  @Deprecated(
    message = "Shouldn't be used in Kotlin",
    level = DeprecationLevel.HIDDEN
  )
  override fun newBuilder(): Nothing = throw AssertionError()

  override fun equals(other: Any?): Boolean {
    if (other === this) return true
    if (other !is VersionTwo) return false
    return unknownFields == other.unknownFields
        && i == other.i
        && v2_i == other.v2_i
        && v2_s == other.v2_s
        && v2_f32 == other.v2_f32
        && v2_f64 == other.v2_f64
        && v2_rs == other.v2_rs
        && obj == other.obj
  }

  override fun hashCode(): Int {
    var result = super.hashCode
    if (result == 0) {
      result = unknownFields.hashCode()
      result = result * 37 + i.hashCode()
      result = result * 37 + v2_i.hashCode()
      result = result * 37 + v2_s.hashCode()
      result = result * 37 + v2_f32.hashCode()
      result = result * 37 + v2_f64.hashCode()
      result = result * 37 + v2_rs.hashCode()
      result = result * 37 + obj.hashCode()
      super.hashCode = result
    }
    return result
  }

  override fun toString(): String {
    val result = mutableListOf<String>()
    if (i != null) result += """i=$i"""
    if (v2_i != null) result += """v2_i=$v2_i"""
    if (v2_s != null) result += """v2_s=$v2_s"""
    if (v2_f32 != null) result += """v2_f32=$v2_f32"""
    if (v2_f64 != null) result += """v2_f64=$v2_f64"""
    if (v2_rs.isNotEmpty()) result += """v2_rs=$v2_rs"""
    if (obj != null) result += """obj=$obj"""
    return result.joinToString(prefix = "VersionTwo{", separator = ", ", postfix = "}")
  }

  fun copy(
    i: Int? = this.i,
    v2_i: Int? = this.v2_i,
    v2_s: String? = this.v2_s,
    v2_f32: Int? = this.v2_f32,
    v2_f64: Long? = this.v2_f64,
    v2_rs: List<String> = this.v2_rs,
    obj: NestedVersionTwo? = this.obj,
    unknownFields: ByteString = this.unknownFields
  ): VersionTwo = VersionTwo(i, v2_i, v2_s, v2_f32, v2_f64, v2_rs, obj, unknownFields)

  companion object {
    @JvmField
    val ADAPTER: ProtoAdapter<VersionTwo> = object : ProtoAdapter<VersionTwo>(
      FieldEncoding.LENGTH_DELIMITED, 
      VersionTwo::class
    ) {
      override fun encodedSize(value: VersionTwo): Int = 
        ProtoAdapter.INT32.encodedSizeWithTag(1, value.i) +
        ProtoAdapter.INT32.encodedSizeWithTag(2, value.v2_i) +
        ProtoAdapter.STRING.encodedSizeWithTag(3, value.v2_s) +
        ProtoAdapter.FIXED32.encodedSizeWithTag(4, value.v2_f32) +
        ProtoAdapter.FIXED64.encodedSizeWithTag(5, value.v2_f64) +
        ProtoAdapter.STRING.asRepeated().encodedSizeWithTag(6, value.v2_rs) +
        NestedVersionTwo.ADAPTER.encodedSizeWithTag(7, value.obj) +
        value.unknownFields.size

      override fun encode(writer: ProtoWriter, value: VersionTwo) {
        ProtoAdapter.INT32.encodeWithTag(writer, 1, value.i)
        ProtoAdapter.INT32.encodeWithTag(writer, 2, value.v2_i)
        ProtoAdapter.STRING.encodeWithTag(writer, 3, value.v2_s)
        ProtoAdapter.FIXED32.encodeWithTag(writer, 4, value.v2_f32)
        ProtoAdapter.FIXED64.encodeWithTag(writer, 5, value.v2_f64)
        ProtoAdapter.STRING.asRepeated().encodeWithTag(writer, 6, value.v2_rs)
        NestedVersionTwo.ADAPTER.encodeWithTag(writer, 7, value.obj)
        writer.writeBytes(value.unknownFields)
      }

      override fun decode(reader: ProtoReader): VersionTwo {
        var i: Int? = null
        var v2_i: Int? = null
        var v2_s: String? = null
        var v2_f32: Int? = null
        var v2_f64: Long? = null
        val v2_rs = mutableListOf<String>()
        var obj: NestedVersionTwo? = null
        val unknownFields = reader.forEachTag { tag ->
          when (tag) {
            1 -> i = ProtoAdapter.INT32.decode(reader)
            2 -> v2_i = ProtoAdapter.INT32.decode(reader)
            3 -> v2_s = ProtoAdapter.STRING.decode(reader)
            4 -> v2_f32 = ProtoAdapter.FIXED32.decode(reader)
            5 -> v2_f64 = ProtoAdapter.FIXED64.decode(reader)
            6 -> v2_rs.add(ProtoAdapter.STRING.decode(reader))
            7 -> obj = NestedVersionTwo.ADAPTER.decode(reader)
            else -> reader.readUnknownField(tag)
          }
        }
        return VersionTwo(
          i = i,
          v2_i = v2_i,
          v2_s = v2_s,
          v2_f32 = v2_f32,
          v2_f64 = v2_f64,
          v2_rs = v2_rs,
          obj = obj,
          unknownFields = unknownFields
        )
      }

      override fun redact(value: VersionTwo): VersionTwo = value.copy(
        obj = value.obj?.let(NestedVersionTwo.ADAPTER::redact),
        unknownFields = ByteString.EMPTY
      )
    }
  }
}