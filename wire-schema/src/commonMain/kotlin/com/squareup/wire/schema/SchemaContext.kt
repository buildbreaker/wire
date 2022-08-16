/*
 * Copyright 2022 Block Inc.
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
package com.squareup.wire.schema

import com.squareup.wire.WireLogger
import okio.Path

interface SchemaContext {
  /** Location on [fileSystem] where the [SchemaHandler] is to write files, if it needs to. */
  val outDirectory: Path
  /** Event-listener like logger with which [SchemaHandler] can notify handled artifacts. */
  val logger: WireLogger
  /**
   * Object to be used by the [SchemaHandler] to store errors. After all [SchemaHandler]s are
   * finished, Wire will throw an exception if any error are present inside the collector.
   */
  val errorCollector: ErrorCollector
  /**
   * Set of rules letting the [SchemaHandler] know what [ProtoType] to include or exclude in its
   * logic. This object represents the `includes` and `excludes` values which were associated
   * with its [Target].
   */
  val emittingRules: EmittingRules
  /**
   * If set, the [SchemaHandler] is to handle only types which are not claimed yet, and claim
   * itself types it has handled. If null, the [SchemaHandler] is to handle all types.
   */
  val claimedDefinitions: ClaimedDefinitions?
  /** If the [SchemaHandler] writes files, it is to claim [Path]s of files it created. */
  val claimedPaths: ClaimedPaths
  /**
   * A [Module] dictates how the loaded types are partitioned and how they are to be handled.
   * If null, there are no partition and all types are to be handled.
   */
  val module: SchemaHandler.Module?
  /**
   * Contains [Location.path] values of all `sourcePath` roots. The [SchemaHandler] is to ignore
   * [ProtoFile]s not part of this set; this verification can be executed via the [inSourcePath]
   * method.
   */
  val sourcePathPaths: Set<String>?
  /**
   * To be used by the [SchemaHandler] if it supports [Profile] files. Please note that this API
   * is unstable and can change at anytime.
   */
  val profileLoader: ProfileLoader?

  fun inSourcePath(protoFile: ProtoFile): Boolean
  fun inSourcePath(location: Location): Boolean

  fun createDirectories(dir: Path, mustCreate: Boolean = false)

  fun write(file: Path, str: String)
}
