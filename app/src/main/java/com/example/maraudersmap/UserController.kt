package com.example.maraudersmap

import okhttp3.Response

class UserController {

    /**
     * Creates a new User from the given parameters, converts its information
     * with the help of the ServerCommunicator class to a xml String and posts it to
     * the server.
     *
     * @param username new username
     * @param password new password for the username
     * @param description small description, needed but not sure why
     * @return Response of the request
     */
     suspend fun createNewUser(username : String, password : String, description : String): Response {

        val userXTO = UserXTO()
        userXTO.username = username
        userXTO.password = password
        userXTO.description = description


            /*val userXml = """
             <userXTO>
                <username>${userXTO.username}</username>
                <password>${userXTO.password}</password>
                <description>${userXTO.description}</description>
             </userXTO>
            """.trimIndent()*/


        val server = ServerCommunicator()
        val response = server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user", userXTO)
        return response

    }

    /**
     * Sends a login request to the server
     *
     * @param username username
     * @param password password for the username
     * @return Response of the request
     */
    suspend fun loginUser(username: String, password: String): Response {
        val userXTO = UserXTO()
        userXTO.username = username
        userXTO.password = password

        /*val userXml = """
             <userXTO>
                <username>${userXTO.username}</username>
                <password>${userXTO.password}</password> 
             </userXTO>
            """.trimIndent()*/

        val server = ServerCommunicator()

        val response = server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/login", userXTO)

        return response
    }
}