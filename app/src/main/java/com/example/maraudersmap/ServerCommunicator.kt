package com.example.maraudersmap

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.StringWriter



class ServerCommunicator {

    /**
     * Generates a xml string out of an object
     *
     * @param input object with all its variables that needs to be converted to a xml string
     * @return xml string
     */
    private fun <T> generateXml(input: T) : String {

        val serializer: Serializer = Persister()

        val writer = StringWriter()
        serializer.write(input,writer)

        return writer.toString()
    }

    /**
     * Posts a request to the server
     *
     * @param url Server address that needs to be communicated with
     * @param xmlObject Xml object that needs to be converted to an xml String. It contains the information that needs to be posted
     * @param callback To put the response into (so program can continue without waiting for the servers response)
     * @return call with the request as response ? todo not so sure about this part but it works
     */
    fun <T> postRequest(url: String?, xmlObject: T, callback: Callback): Call {

        val xml = generateXml(xmlObject)

        val client = OkHttpClient()
        val mediaType : MediaType = "application/xml; charset=utf-8".toMediaType()
        val body: RequestBody = xml.toRequestBody(mediaType)

        val request : Request = Request.Builder().url(url!!).post(body).build()
        println(request)

        val call: Call = client.newCall(request)
        call.enqueue(callback)

        println("adsf")

        return call
    }
}