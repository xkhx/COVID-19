package me.xiaox.covid.utils

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder

/**
 * 来自网络
 */
object HttpUtils {
    private const val CONNECT_TIME_OUT = 3000
    private const val READ_TIME_OUT = 3000

    fun get(url: String, mapParam: Map<String, String>?): Map<String, String> {
        val urlParam = if (mapParam == null) url else "${url}?${converMap2String(mapParam)}"
        val connection = buildURLConnection(urlParam)
        connection.setContentType()
        connection.connect()
        val code = connection.responseCode
        val inStream = (if (code == 200) connection.inputStream else connection.errorStream)
            ?: return mapOf("code" to code.toString(), "result" to "")
        val result = inStream.bufferedReader().lineSequence().joinToString()
        connection.disconnect()
        return mapOf("code" to code.toString(), "result" to result)
    }

    fun post(url: String, param: Map<String, String>): Map<String, String> {
        val connection = buildURLConnection(url, "POST")
        connection.setContentType(1)
        connection.connect()
        connection.outputStream.let {
            it.write(converMap2String(param).toByteArray())
            it.flush()
            it.close()
        }
        val code = connection.responseCode
        val inStream = if (code == 200) connection.inputStream else connection.errorStream
        val result = inStream.bufferedReader().lineSequence().joinToString()
        connection.disconnect()
        return mapOf("code" to code.toString(), "result" to result)
    }

    private fun buildURLConnection(urlLink: String, requestMethod: String = "GET"): HttpURLConnection {
        val url = URL(urlLink)
        val connection = url.openConnection() as HttpURLConnection
        connection.let {
            it.requestMethod = requestMethod
            it.connectTimeout = CONNECT_TIME_OUT
            it.readTimeout = READ_TIME_OUT
            it.doInput = true
            it.doOutput = true
            it.useCaches = requestMethod == "GET"
            it.instanceFollowRedirects = true
        }
        return connection
    }

    private fun URLConnection.setContentType(type: Int = 0) {
        val typeString = when (type) {
            1 -> "application/x-www-form-urlencoded"
            2 -> "application/x-java-serialized-object"
            else -> "application/json;charset=UTF-8"
        }
        this.setRequestProperty("Content-Type", typeString)
    }

    private fun converMap2String(param: Map<String, String>, isEncode: Boolean = true): String {
        return param.keys.joinToString(separator = "&") { key ->
            val value = if (isEncode) URLEncoder.encode(param[key], "UTF-8") else param[key]
            "$key=$value"
        }
    }
}