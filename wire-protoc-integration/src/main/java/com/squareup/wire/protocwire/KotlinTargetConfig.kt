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

import com.squareup.wire.schema.KotlinTarget
import com.squareup.wire.gradle.KotlinOutput

private const val ANDROID = "android"
private const val JAVA_INTEROP = "javaInterop"
private const val EMIT_DECLARED_OPTIONS = "emitDeclaredOptions"
private const val EMIT_APPLIED_OPTIONS = "emitAppliedOptions"
private const val RPC_CALL_STYLE = "rpcCallStyle"
private const val RPC_ROLE = "rpcRole"
private const val SINGLE_METHOD_SERVICES = "singleMethodServices"
private const val BOX_ONE_OFS_MIN_SIZE = "boxOneOfsMinSize"
private const val GRPC_SERVER_COMPATIBLE = "grpcServerCompatible"
private const val NAME_SUFFIX = "nameSuffix"
private const val BUILDERS_ONLY = "buildersOnly"

/**
 * Translates the protoc options into known options for the
 * KotlinTarget via the gradle plugin KotlinOutput.
 */
internal class KotlinTargetConfig {
  companion object {
    /**
     * See [ParameterParser] for more details
     * on how the parameters are parsed.
     *
     * @param parameters the map of string to string from the protoc options.
     * @return the translated KotlinTarget.
     */
    @JvmStatic
    internal fun parse(parameters: Map<String, String>): KotlinTarget {
      val output = KotlinOutput()
      // The internal values are initialized with defaults.
      // Only set when the value from parameters are null.
      output.android = parameters[ANDROID]?.toBoolean() ?: output.android
      output.javaInterop = parameters[JAVA_INTEROP]?.toBoolean() ?: output.javaInterop
      output.emitDeclaredOptions = parameters[EMIT_DECLARED_OPTIONS]?.toBoolean() ?: output.emitDeclaredOptions
      output.emitAppliedOptions = parameters[EMIT_APPLIED_OPTIONS]?.toBoolean() ?: output.emitAppliedOptions
      output.rpcCallStyle = parameters[RPC_CALL_STYLE] ?: output.rpcCallStyle
      output.rpcRole = parameters[RPC_ROLE] ?: output.rpcRole
      output.singleMethodServices = parameters[SINGLE_METHOD_SERVICES]?.toBoolean() ?: output.singleMethodServices
      output.boxOneOfsMinSize = parameters[BOX_ONE_OFS_MIN_SIZE]?.toInt() ?: output.boxOneOfsMinSize
      output.grpcServerCompatible = parameters[GRPC_SERVER_COMPATIBLE]?.toBoolean() ?: output.grpcServerCompatible
      output.nameSuffix = parameters[NAME_SUFFIX] ?: output.nameSuffix
      output.buildersOnly = parameters[BUILDERS_ONLY]?.toBoolean() ?: output.buildersOnly
      return output.toTarget("")
    }
  }
}
