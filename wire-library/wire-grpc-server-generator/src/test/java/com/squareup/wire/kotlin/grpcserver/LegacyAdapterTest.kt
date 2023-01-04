/*
 * Copyright (C) 2021 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.wire.kotlin.grpcserver

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.buildSchema
import com.squareup.wire.kotlin.grpcserver.LegacyAdapterGenerator.addLegacyAdapter
import com.squareup.wire.schema.addLocal
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.File

class LegacyAdapterTest {
  @Test
  fun legacyAdapter() {
    val path = "src/test/proto/RouteGuideProto.proto".toPath()
    val schema = buildSchema { addLocal(path) }
    val service = schema.getService("routeguide.RouteGuide")

    val code = FileSpec.builder("routeguide", "RouteGuide")
      .addType(
        TypeSpec.classBuilder("RouteGuideWireGrpc")
          .apply {
            addLegacyAdapter(
              generator = ClassNameGenerator(buildClassMap(schema, service!!)),
              builder = this,
              service,
              LegacyAdapterGenerator.Options(singleMethodServices = true),
            )
          }
          .build()
      )
      .build()
      .toString()

    println(code)
    Assertions.assertThat(code)
      .isEqualTo(File("src/test/golden/LegacyAdapter.kt").source().buffer().readUtf8())
  }
}