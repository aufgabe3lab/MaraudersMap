package com.example.maraudersmap

import com.example.maraudersmap.LoginActivity.Companion.jsonWebToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import ru.gildor.coroutines.okhttp.await
import java.io.StringWriter

/**
 * Provides methods to be able to communicate with a server
 * @author Julian Ertle
 * @since 2022.11.30
 */

class ServerCommunicator {

    private val client = OkHttpClient()

    /**
     * Generates a xml string out of an object
     *
     * @param input object with all its variables that needs to be converted to a xml string
     * @return xml string
     */
    private fun <T> generateXml(input: T) : String {

        val serializer : Serializer = Persister()

        val writer = StringWriter()
        serializer.write(input,writer)

        return writer.toString()
    }

    /**
     * Sends a posts request to the server and returns the response
     *
     * @param url Server address that needs to be communicated with
     * @param xmlObject Xml object that needs to be converted to an xml String. It contains the information that needs to be posted to the server
     * @return Response of the request
     */
    suspend fun <T> postRequest(url: String?, xmlObject: T): Response {

        val xml = generateXml(xmlObject)
        val request: Request
        val mediaType: MediaType = "application/xml; charset=utf-8".toMediaType()
        val body: RequestBody = xml.toRequestBody(mediaType)

        if(jsonWebToken==null){     // if user is not logged in
            request = Request.Builder().url(url!!).post(body).build()
        }
        else{                       // if user is logged in
            request = Request.Builder().url(url!!).post(body).addHeader("Authorization", jsonWebToken!!).build()
        }

        return client.newCall(request).await()
    }

    /**
     * Sends a put request to the server and returns the response
     *
     * @param url Server address that needs to be communicated with
     * @param xmlObject Xml object that needs to be converted to an xml String. It contains the information that needs to be posted to the server
     * @return Response of the request
     */
    suspend fun <T> putRequest(url: String?, xmlObject: T): Response{

        val xml = generateXml(xmlObject)

        val mediaType: MediaType = "application/xml; charset=utf-8".toMediaType()
        val body: RequestBody = xml.toRequestBody(mediaType)
        val request: Request = Request.Builder().url(url!!).put(body).addHeader("Authorization", jsonWebToken!!).build()

        return client.newCall(request).await()
    }

    /**
     * Sends a delete request to the server and returns the response
     *
     * @param url Server address that needs to be communicated with
     * @return Response of the request
     */
    suspend fun deleteRequest(url: String?): Response{

        val request: Request = Request.Builder().url(url!!).delete().addHeader("Authorization", jsonWebToken!!).build()

        return client.newCall(request).await()
    }

    /**
     * Send a get request to the server and returns the response
     *
     * @param url Server address that needs to be communicated with
     * @return Response of the request
     */
    suspend fun getRequest(url: String?): Response{

        val request: Request = Request.Builder().url(url!!).get().addHeader("Authorization", jsonWebToken!!).build()

        return client.newCall(request).await()
    }
}