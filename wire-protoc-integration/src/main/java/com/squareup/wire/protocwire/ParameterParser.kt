package com.squareup.wire.protocwire

class ParameterParser {
  companion object {
    fun parse(parameter: String): Map<String, String> {
      val parsedParameters = mutableMapOf<String, String>()
      val split = parameter.split(',')
      split.forEach { str -> str.trim() }
      for (elm in split) {
        if (!elm.contains('=')) {
          continue
        }
        val pair = elm.split('=')
        if (pair.size != 2) {
          continue
        }
        parsedParameters[pair.first()] = pair[1]
      }
      return parsedParameters
    }
  }
}
