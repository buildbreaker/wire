/*
 * Copyright 2019 Square Inc.
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
package com.squareup.wire.internal

import com.squareup.wire.GrpcClient
import com.squareup.wire.GrpcMethod
import com.squareup.wire.GrpcResponse
import com.squareup.wire.GrpcStreamingCall
import com.squareup.wire.MessageSink
import com.squareup.wire.MessageSource
import com.squareup.wire.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import okio.Timeout
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import okhttp3.Callback
import okio.IOException

class RealGrpcStreamingCall<S : Any, R : Any>(
  private val grpcClient: GrpcClient,
  override val method: GrpcMethod<S, R>
) : GrpcStreamingCall<S, R> {
  private val requestBody = newDuplexRequestBody()
  /** Non-null once this is executed. */
  private var call: Call? = null
  private var canceled = false

  override val timeout: Timeout = LateInitTimeout()

  init {
    timeout.clearTimeout()
    timeout.clearDeadline()
  }

  override var requestMetadata: Map<String, String> = mapOf()

  override var responseMetadata: Map<String, String>? = null
    internal set

  override fun cancel() {
    canceled = true
    call?.cancel()
  }

  override fun isCanceled(): Boolean = canceled || call?.isCanceled() == true

  override fun execute(): Pair<SendChannel<S>, ReceiveChannel<R>> = executeIn(GlobalScope)

  override fun executeIn(scope: CoroutineScope): Pair<SendChannel<S>, ReceiveChannel<R>> {
    val requestChannel = Channel<S>(1)
    val responseChannel = Channel<R>(1)
    check(call == null) { "already executed" }
    val result = grpcClient.newCall(method, requestMetadata, requestBody)
    call = result
    if (canceled) result.cancel()
    (timeout as LateInitTimeout).init(result.timeout())
    val call = result

    responseChannel.invokeOnClose {
      if (responseChannel.isClosedForReceive) {
        // Short-circuit the request stream if it's still active.
        call.cancel()
        requestChannel.cancel()
      }
    }
    scope.launch(Dispatchers.IO) {
      requestChannel.writeToRequestBody(
        requestBody = requestBody,
        minMessageToCompress = grpcClient.minMessageToCompress,
        requestAdapter = method.requestAdapter,
        callForCancel = call
      )
    }
    call.enqueue(responseChannel.readFromResponseBodyCallback({ headers -> this.responseMetadata = headers.toMap() }, method.responseAdapter))

    return requestChannel to responseChannel
  }

  fun execute2(scope: CoroutineScope): Pair<SendChannel<S>, ReceiveChannel<R>> {
    val requestChannel = Channel<S>(1)
    val responseChannel = Channel<R>(1)
    check(call == null) { "already executed" }
    val result = grpcClient.newCall(method, requestMetadata, requestBody)
    call = result
    if (canceled) result.cancel()
    (timeout as LateInitTimeout).init(result.timeout())
    val call = result

    responseChannel.invokeOnClose {
      if (responseChannel.isClosedForReceive) {
        // Short-circuit the request stream if it's still active.
        call.cancel()
        requestChannel.cancel()
      }
    }
    scope.launch(Dispatchers.IO) {
      val requestWriter = requestBody.messageSink(minMessageToCompress = grpcClient.minMessageToCompress,
        requestAdapter = method.requestAdapter,
        callForCancel = call
      )
      try {
        requestWriter.use {
          var channelReadFailed = true
          try {
            requestChannel.consumeEach { message ->
              channelReadFailed = false
              requestWriter.write(message)
              channelReadFailed = true
            }
            channelReadFailed = false
          } finally {
            if (channelReadFailed) requestWriter.cancel()
          }
        }
      } catch (e: Throwable) {
        requestChannel.cancel(CancellationException("Could not write message", e))
        if (e !is IOException && e !is CancellationException) {
          throw e
        }
      }
    }
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        // Something broke. Kill the response channel.
        responseChannel.close(e)
      }

      override fun onResponse(call: Call, response: GrpcResponse) {
//        this.responseMetadata = response.headers.toMap()
        runBlocking {
          response.use {
            response.messageSource(method.responseAdapter).use { reader ->
              var exception: Exception? = null
              try {
                while (true) {
                  val message = reader.read() ?: break
                  responseChannel.send(message)
                }
                exception = response.grpcResponseToException()
              } catch (e: IOException) {
                exception = response.grpcResponseToException(e)
              } catch (e: Exception) {
                exception = e
              } finally {
                try {
                  responseChannel.close(exception)
                } catch (_: CancellationException) {
                  // If it's already canceled, there's nothing more to do.
                }
              }
            }
          }
        }
      }
    })

    return requestChannel to responseChannel
  }
  fun execute3(scope: CoroutineScope, method: GrpcMethod<S, R>, requestMetadata: Map<String, String>): Pair<SendChannel<S>, ReceiveChannel<R>> {
    val requestChannel = Channel<S>(1)
    val responseChannel = Channel<R>(1)
    val requestBody = newDuplexRequestBody()
    val call = grpcClient.newCall(method, requestMetadata, requestBody)

    responseChannel.invokeOnClose {
      if (responseChannel.isClosedForReceive) {
        // Short-circuit the request stream if it's still active.
        call.cancel()
        requestChannel.cancel()
      }
    }
    scope.launch(Dispatchers.IO) {
      val requestWriter = requestBody.messageSink(minMessageToCompress = grpcClient.minMessageToCompress,
        requestAdapter = method.requestAdapter,
        callForCancel = call
      )
      try {
        requestWriter.use {
          var channelReadFailed = true
          try {
            requestChannel.consumeEach { message ->
              channelReadFailed = false
              requestWriter.write(message)
              channelReadFailed = true
            }
            channelReadFailed = false
          } finally {
            if (channelReadFailed) requestWriter.cancel()
          }
        }
      } catch (e: Throwable) {
        requestChannel.cancel(CancellationException("Could not write message", e))
        if (e !is IOException && e !is CancellationException) {
          throw e
        }
      }
    }
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        // Something broke. Kill the response channel.
        responseChannel.close(e)
      }

      override fun onResponse(call: Call, response: GrpcResponse) {
        runBlocking {
          response.use {
            response.messageSource(method.responseAdapter).use { reader ->
              var exception: Exception? = null
              try {
                while (true) {
                  val message = reader.read() ?: break
                  responseChannel.send(message)
                }
                exception = response.grpcResponseToException()
              } catch (e: IOException) {
                exception = response.grpcResponseToException(e)
              } catch (e: Exception) {
                exception = e
              } finally {
                try {
                  responseChannel.close(exception)
                } catch (_: CancellationException) {
                  // If it's already canceled, there's nothing more to do.
                }
              }
            }
          }
        }
      }
    })

    return requestChannel to responseChannel
  }

  override fun executeBlocking(): Pair<MessageSink<S>, MessageSource<R>> {
    val call = initCall()
    val messageSource = BlockingMessageSource(this, method.responseAdapter, call)
    val messageSink = requestBody.messageSink(
      minMessageToCompress = grpcClient.minMessageToCompress,
      requestAdapter = method.requestAdapter,
      callForCancel = call
    )
    call.enqueue(messageSource.readFromResponseBodyCallback())

    return messageSink to messageSource
  }

  override fun isExecuted(): Boolean = call?.isExecuted() ?: false

  override fun clone(): GrpcStreamingCall<S, R> {
    val result = RealGrpcStreamingCall(grpcClient, method)
    val oldTimeout = this.timeout
    result.timeout.also { newTimeout ->
      newTimeout.timeout(oldTimeout.timeoutNanos(), TimeUnit.NANOSECONDS)
      if (oldTimeout.hasDeadline()) newTimeout.deadlineNanoTime(oldTimeout.deadlineNanoTime())
      else newTimeout.clearDeadline()
    }
    result.requestMetadata += this.requestMetadata
    return result
  }

  private fun initCall(): Call {
    check(this.call == null) { "already executed" }

    val result = grpcClient.newCall(method, requestMetadata, requestBody)
    this.call = result
    if (canceled) result.cancel()
    (timeout as LateInitTimeout).init(result.timeout())
    return result
  }
}
