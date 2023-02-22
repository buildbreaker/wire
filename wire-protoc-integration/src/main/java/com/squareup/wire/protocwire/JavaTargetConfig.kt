/*
 * Copyright 2023 Block Inc.
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
package com.squareup.wire.protocwire

import com.squareup.wire.schema.JavaTarget
import com.squareup.wire.gradle.JavaOutput

private const val ANDROID = "android"
private const val ANDROID_ANNOTATIONS = "androidAnnotations"
private const val COMPACT = "compact"
private const val EMIT_DECLARED_OPTIONS = "emitDeclaredOptions"
private const val EMIT_APPLIED_OPTIONS = "emitAppliedOptions"
private const val BUILDERS_ONLY = "buildersOnly"

/**
 * Translates the protoc options into known options for the
 * JavaTarget via the gradle plugin JavaOutput.
 */
internal class JavaTargetConfig {
  companion object {
    /**
     * See [ParameterParser] for more details
     * on how the parameters are parsed.
     *
     * @param parameters the map of string to string from the protoc options.
     * @return the translated JavaTarget.
     */
    internal fun parse(parameters: Map<String, String>): JavaTarget {
      val output = JavaOutput()
      // The internal values are initialized with defaults.
      // Only set when the value from parameters are null.
      output.android = parameters[ANDROID]?.toBoolean() ?: output.android
      output.androidAnnotations = parameters[ANDROID_ANNOTATIONS]?.toBoolean() ?: output.androidAnnotations
      output.compact = parameters[COMPACT]?.toBoolean() ?: output.compact
      output.emitDeclaredOptions = parameters[EMIT_DECLARED_OPTIONS]?.toBoolean() ?: output.emitDeclaredOptions
      output.emitAppliedOptions = parameters[EMIT_APPLIED_OPTIONS]?.toBoolean() ?: output.emitAppliedOptions
      output.buildersOnly = parameters[BUILDERS_ONLY]?.toBoolean() ?: output.buildersOnly
      return output.toTarget("")
    }
  }
}
