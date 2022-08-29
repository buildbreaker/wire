// Code generated by Wire protocol buffer compiler, do not edit.
// Source: squareup.protos.custom_options.FooBar in custom_options.proto
package com.squareup.protos.custom_options

import com.squareup.wire.EnumAdapter
import com.squareup.wire.FieldEncoding
import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import com.squareup.wire.ProtoWriter
import com.squareup.wire.ReverseProtoWriter
import com.squareup.wire.Syntax
import com.squareup.wire.Syntax.PROTO_2
import com.squareup.wire.WireEnum
import com.squareup.wire.WireField
import com.squareup.wire.`internal`.immutableCopyOf
import com.squareup.wire.`internal`.redactElements
import com.squareup.wire.`internal`.sanitize
import kotlin.Any
import kotlin.AssertionError
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.DeprecationLevel
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Nothing
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import okio.ByteString

public class FooBar(
  @field:WireField(
    tag = 1,
    adapter = "com.squareup.wire.ProtoAdapter#INT32",
  )
  public val foo: Int? = null,
  @field:WireField(
    tag = 2,
    adapter = "com.squareup.wire.ProtoAdapter#STRING",
  )
  public val bar: String? = null,
  @field:WireField(
    tag = 3,
    adapter = "com.squareup.protos.custom_options.FooBar${'$'}Nested#ADAPTER",
  )
  public val baz: Nested? = null,
  @field:WireField(
    tag = 4,
    adapter = "com.squareup.wire.ProtoAdapter#UINT64",
  )
  public val qux: Long? = null,
  fred: List<Float> = emptyList(),
  @field:WireField(
    tag = 6,
    adapter = "com.squareup.wire.ProtoAdapter#DOUBLE",
  )
  public val daisy: Double? = null,
  nested: List<FooBar> = emptyList(),
  unknownFields: ByteString = ByteString.EMPTY,
) : Message<FooBar, Nothing>(ADAPTER, unknownFields) {
  @field:WireField(
    tag = 5,
    adapter = "com.squareup.wire.ProtoAdapter#FLOAT",
    label = WireField.Label.REPEATED,
  )
  public val fred: List<Float> = immutableCopyOf("fred", fred)

  @field:WireField(
    tag = 7,
    adapter = "com.squareup.protos.custom_options.FooBar#ADAPTER",
    label = WireField.Label.REPEATED,
  )
  public val nested: List<FooBar> = immutableCopyOf("nested", nested)

  @Deprecated(
    message = "Shouldn't be used in Kotlin",
    level = DeprecationLevel.HIDDEN,
  )
  public override fun newBuilder(): Nothing = throw
      AssertionError("Builders are deprecated and only available in a javaInterop build; see https://square.github.io/wire/wire_compiler/#kotlin")

  public override fun equals(other: Any?): Boolean {
    if (other === this) return true
    if (other !is FooBar) return false
    if (unknownFields != other.unknownFields) return false
    if (foo != other.foo) return false
    if (bar != other.bar) return false
    if (baz != other.baz) return false
    if (qux != other.qux) return false
    if (fred != other.fred) return false
    if (daisy != other.daisy) return false
    if (nested != other.nested) return false
    return true
  }

  public override fun hashCode(): Int {
    var result = super.hashCode
    if (result == 0) {
      result = unknownFields.hashCode()
      result = result * 37 + (foo?.hashCode() ?: 0)
      result = result * 37 + (bar?.hashCode() ?: 0)
      result = result * 37 + (baz?.hashCode() ?: 0)
      result = result * 37 + (qux?.hashCode() ?: 0)
      result = result * 37 + fred.hashCode()
      result = result * 37 + (daisy?.hashCode() ?: 0)
      result = result * 37 + nested.hashCode()
      super.hashCode = result
    }
    return result
  }

  public override fun toString(): String {
    val result = mutableListOf<String>()
    if (foo != null) result += """foo=$foo"""
    if (bar != null) result += """bar=${sanitize(bar)}"""
    if (baz != null) result += """baz=$baz"""
    if (qux != null) result += """qux=$qux"""
    if (fred.isNotEmpty()) result += """fred=$fred"""
    if (daisy != null) result += """daisy=$daisy"""
    if (nested.isNotEmpty()) result += """nested=$nested"""
    return result.joinToString(prefix = "FooBar{", separator = ", ", postfix = "}")
  }

  public fun copy(
    foo: Int? = this.foo,
    bar: String? = this.bar,
    baz: Nested? = this.baz,
    qux: Long? = this.qux,
    fred: List<Float> = this.fred,
    daisy: Double? = this.daisy,
    nested: List<FooBar> = this.nested,
    unknownFields: ByteString = this.unknownFields,
  ): FooBar = FooBar(foo, bar, baz, qux, fred, daisy, nested, unknownFields)

  public companion object {
    @JvmField
    public val ADAPTER: ProtoAdapter<FooBar> = object : ProtoAdapter<FooBar>(
      FieldEncoding.LENGTH_DELIMITED, 
      FooBar::class, 
      "type.googleapis.com/squareup.protos.custom_options.FooBar", 
      PROTO_2, 
      null, 
      "custom_options.proto"
    ) {
      public override fun encodedSize(`value`: FooBar): Int {
        var size = value.unknownFields.size
        size += ProtoAdapter.INT32.encodedSizeWithTag(1, value.foo)
        size += ProtoAdapter.STRING.encodedSizeWithTag(2, value.bar)
        size += Nested.ADAPTER.encodedSizeWithTag(3, value.baz)
        size += ProtoAdapter.UINT64.encodedSizeWithTag(4, value.qux)
        size += ProtoAdapter.FLOAT.asRepeated().encodedSizeWithTag(5, value.fred)
        size += ProtoAdapter.DOUBLE.encodedSizeWithTag(6, value.daisy)
        size += FooBar.ADAPTER.asRepeated().encodedSizeWithTag(7, value.nested)
        return size
      }

      public override fun encode(writer: ProtoWriter, `value`: FooBar): Unit {
        ProtoAdapter.INT32.encodeWithTag(writer, 1, value.foo)
        ProtoAdapter.STRING.encodeWithTag(writer, 2, value.bar)
        Nested.ADAPTER.encodeWithTag(writer, 3, value.baz)
        ProtoAdapter.UINT64.encodeWithTag(writer, 4, value.qux)
        ProtoAdapter.FLOAT.asRepeated().encodeWithTag(writer, 5, value.fred)
        ProtoAdapter.DOUBLE.encodeWithTag(writer, 6, value.daisy)
        FooBar.ADAPTER.asRepeated().encodeWithTag(writer, 7, value.nested)
        writer.writeBytes(value.unknownFields)
      }

      public override fun encode(writer: ReverseProtoWriter, `value`: FooBar): Unit {
        writer.writeBytes(value.unknownFields)
        FooBar.ADAPTER.asRepeated().encodeWithTag(writer, 7, value.nested)
        ProtoAdapter.DOUBLE.encodeWithTag(writer, 6, value.daisy)
        ProtoAdapter.FLOAT.asRepeated().encodeWithTag(writer, 5, value.fred)
        ProtoAdapter.UINT64.encodeWithTag(writer, 4, value.qux)
        Nested.ADAPTER.encodeWithTag(writer, 3, value.baz)
        ProtoAdapter.STRING.encodeWithTag(writer, 2, value.bar)
        ProtoAdapter.INT32.encodeWithTag(writer, 1, value.foo)
      }

      public override fun decode(reader: ProtoReader): FooBar {
        var foo: Int? = null
        var bar: String? = null
        var baz: Nested? = null
        var qux: Long? = null
        val fred = mutableListOf<Float>()
        var daisy: Double? = null
        val nested = mutableListOf<FooBar>()
        val unknownFields = reader.forEachTag { tag ->
          when (tag) {
            1 -> foo = ProtoAdapter.INT32.decode(reader)
            2 -> bar = ProtoAdapter.STRING.decode(reader)
            3 -> baz = Nested.ADAPTER.decode(reader)
            4 -> qux = ProtoAdapter.UINT64.decode(reader)
            5 -> fred.add(ProtoAdapter.FLOAT.decode(reader))
            6 -> daisy = ProtoAdapter.DOUBLE.decode(reader)
            7 -> nested.add(FooBar.ADAPTER.decode(reader))
            else -> reader.readUnknownField(tag)
          }
        }
        return FooBar(
          foo = foo,
          bar = bar,
          baz = baz,
          qux = qux,
          fred = fred,
          daisy = daisy,
          nested = nested,
          unknownFields = unknownFields
        )
      }

      public override fun redact(`value`: FooBar): FooBar = value.copy(
        baz = value.baz?.let(Nested.ADAPTER::redact),
        nested = value.nested.redactElements(FooBar.ADAPTER),
        unknownFields = ByteString.EMPTY
      )
    }

    private const val serialVersionUID: Long = 0L
  }

  public class Nested(
    @field:WireField(
      tag = 1,
      adapter = "com.squareup.protos.custom_options.FooBar${'$'}FooBarBazEnum#ADAPTER",
      declaredName = "value",
    )
    public val value_: FooBarBazEnum? = null,
    unknownFields: ByteString = ByteString.EMPTY,
  ) : Message<Nested, Nothing>(ADAPTER, unknownFields) {
    @Deprecated(
      message = "Shouldn't be used in Kotlin",
      level = DeprecationLevel.HIDDEN,
    )
    public override fun newBuilder(): Nothing = throw
        AssertionError("Builders are deprecated and only available in a javaInterop build; see https://square.github.io/wire/wire_compiler/#kotlin")

    public override fun equals(other: Any?): Boolean {
      if (other === this) return true
      if (other !is Nested) return false
      if (unknownFields != other.unknownFields) return false
      if (value_ != other.value_) return false
      return true
    }

    public override fun hashCode(): Int {
      var result = super.hashCode
      if (result == 0) {
        result = unknownFields.hashCode()
        result = result * 37 + (value_?.hashCode() ?: 0)
        super.hashCode = result
      }
      return result
    }

    public override fun toString(): String {
      val result = mutableListOf<String>()
      if (value_ != null) result += """value_=$value_"""
      return result.joinToString(prefix = "Nested{", separator = ", ", postfix = "}")
    }

    public fun copy(value_: FooBarBazEnum? = this.value_, unknownFields: ByteString =
        this.unknownFields): Nested = Nested(value_, unknownFields)

    public companion object {
      @JvmField
      public val ADAPTER: ProtoAdapter<Nested> = object : ProtoAdapter<Nested>(
        FieldEncoding.LENGTH_DELIMITED, 
        Nested::class, 
        "type.googleapis.com/squareup.protos.custom_options.FooBar.Nested", 
        PROTO_2, 
        null, 
        "custom_options.proto"
      ) {
        public override fun encodedSize(`value`: Nested): Int {
          var size = value.unknownFields.size
          size += FooBarBazEnum.ADAPTER.encodedSizeWithTag(1, value.value_)
          return size
        }

        public override fun encode(writer: ProtoWriter, `value`: Nested): Unit {
          FooBarBazEnum.ADAPTER.encodeWithTag(writer, 1, value.value_)
          writer.writeBytes(value.unknownFields)
        }

        public override fun encode(writer: ReverseProtoWriter, `value`: Nested): Unit {
          writer.writeBytes(value.unknownFields)
          FooBarBazEnum.ADAPTER.encodeWithTag(writer, 1, value.value_)
        }

        public override fun decode(reader: ProtoReader): Nested {
          var value_: FooBarBazEnum? = null
          val unknownFields = reader.forEachTag { tag ->
            when (tag) {
              1 -> try {
                value_ = FooBarBazEnum.ADAPTER.decode(reader)
              } catch (e: ProtoAdapter.EnumConstantNotFoundException) {
                reader.addUnknownField(tag, FieldEncoding.VARINT, e.value.toLong())
              }
              else -> reader.readUnknownField(tag)
            }
          }
          return Nested(
            value_ = value_,
            unknownFields = unknownFields
          )
        }

        public override fun redact(`value`: Nested): Nested = value.copy(
          unknownFields = ByteString.EMPTY
        )
      }

      private const val serialVersionUID: Long = 0L
    }
  }

  public class More(
    serial: List<Int> = emptyList(),
    unknownFields: ByteString = ByteString.EMPTY,
  ) : Message<More, Nothing>(ADAPTER, unknownFields) {
    @field:WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#INT32",
      label = WireField.Label.REPEATED,
    )
    public val serial: List<Int> = immutableCopyOf("serial", serial)

    @Deprecated(
      message = "Shouldn't be used in Kotlin",
      level = DeprecationLevel.HIDDEN,
    )
    public override fun newBuilder(): Nothing = throw
        AssertionError("Builders are deprecated and only available in a javaInterop build; see https://square.github.io/wire/wire_compiler/#kotlin")

    public override fun equals(other: Any?): Boolean {
      if (other === this) return true
      if (other !is More) return false
      if (unknownFields != other.unknownFields) return false
      if (serial != other.serial) return false
      return true
    }

    public override fun hashCode(): Int {
      var result = super.hashCode
      if (result == 0) {
        result = unknownFields.hashCode()
        result = result * 37 + serial.hashCode()
        super.hashCode = result
      }
      return result
    }

    public override fun toString(): String {
      val result = mutableListOf<String>()
      if (serial.isNotEmpty()) result += """serial=$serial"""
      return result.joinToString(prefix = "More{", separator = ", ", postfix = "}")
    }

    public fun copy(serial: List<Int> = this.serial, unknownFields: ByteString =
        this.unknownFields): More = More(serial, unknownFields)

    public companion object {
      @JvmField
      public val ADAPTER: ProtoAdapter<More> = object : ProtoAdapter<More>(
        FieldEncoding.LENGTH_DELIMITED, 
        More::class, 
        "type.googleapis.com/squareup.protos.custom_options.FooBar.More", 
        PROTO_2, 
        null, 
        "custom_options.proto"
      ) {
        public override fun encodedSize(`value`: More): Int {
          var size = value.unknownFields.size
          size += ProtoAdapter.INT32.asRepeated().encodedSizeWithTag(1, value.serial)
          return size
        }

        public override fun encode(writer: ProtoWriter, `value`: More): Unit {
          ProtoAdapter.INT32.asRepeated().encodeWithTag(writer, 1, value.serial)
          writer.writeBytes(value.unknownFields)
        }

        public override fun encode(writer: ReverseProtoWriter, `value`: More): Unit {
          writer.writeBytes(value.unknownFields)
          ProtoAdapter.INT32.asRepeated().encodeWithTag(writer, 1, value.serial)
        }

        public override fun decode(reader: ProtoReader): More {
          val serial = mutableListOf<Int>()
          val unknownFields = reader.forEachTag { tag ->
            when (tag) {
              1 -> serial.add(ProtoAdapter.INT32.decode(reader))
              else -> reader.readUnknownField(tag)
            }
          }
          return More(
            serial = serial,
            unknownFields = unknownFields
          )
        }

        public override fun redact(`value`: More): More = value.copy(
          unknownFields = ByteString.EMPTY
        )
      }

      private const val serialVersionUID: Long = 0L
    }
  }

  public enum class FooBarBazEnum(
    public override val `value`: Int,
  ) : WireEnum {
    FOO(1),
    BAR(2),
    BAZ(3),
    ;

    public companion object {
      @JvmField
      public val ADAPTER: ProtoAdapter<FooBarBazEnum> = object : EnumAdapter<FooBarBazEnum>(
        FooBarBazEnum::class, 
        PROTO_2, 
        null
      ) {
        public override fun fromValue(`value`: Int): FooBarBazEnum? = FooBarBazEnum.fromValue(value)
      }

      @JvmStatic
      public fun fromValue(`value`: Int): FooBarBazEnum? = when (value) {
        1 -> FOO
        2 -> BAR
        3 -> BAZ
        else -> null
      }
    }
  }
}
