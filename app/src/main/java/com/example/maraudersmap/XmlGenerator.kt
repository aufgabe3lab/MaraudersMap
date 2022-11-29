package com.example.maraudersmap

import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.StringWriter

class XmlGenerator {

    /**
     * Generates a xml string out of a object
     *
     * @param input object with all its variables that needs to be converted to a xml string
     * @return xml string
     */
    fun <T> generateXml(input: T) : String {

        val serializer: Serializer = Persister()

        val writer = StringWriter()
        serializer.write(input,writer)

        return writer.toString()
    }
}