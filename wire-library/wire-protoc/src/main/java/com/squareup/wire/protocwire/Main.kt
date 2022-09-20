package com.squareup.wire.protocwire

import buf.connect.demo.eliza.v1.ConverseRequest
import buf.connect.demo.eliza.v1.GrpcElizaServiceClient
import com.squareup.wire.Duration
import com.squareup.wire.GrpcClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol

class Main {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      println("starting...")
      garbage()
      println("...finished")
    }
    private fun garbage() {
      val target = "http://127.0.0.1:8080"
      val grpcClient = GrpcClient.Builder().baseUrl(target)
        .client(
          OkHttpClient.Builder().readTimeout(Duration.ofMinutes(1))
            .writeTimeout(Duration.ofMinutes(1))
            .callTimeout(Duration.ofMinutes(1))
            .protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE))
            .build()
        )
        .build()
      val serviceClient = GrpcElizaServiceClient(grpcClient)
      val streamingCoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
      val job = streamingCoroutineScope.launch {
        val (send, rec) = serviceClient.converse(this)
        val job = launch(newSingleThreadContext("response_channel")) {
          while (true) {
            if (rec.isClosedForReceive) break
            val response = rec.receive()
            println(response.sentence)
          }
        }
        send.send(
          ConverseRequest(
            sentence = "I feel happy"
          )
        )
        println("converse sent...")
        job.join()
      }
      runBlocking {
        job.join()
      }
    }
  }
}
