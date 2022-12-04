package com.example.maraudersmap

import okhttp3.Response

/**
 * Provides methods to let the client communicate with the backend
 * @author Julian Ertle
 * @since 2022.11.30
 */

class UserController {

    private val server : ServerCommunicator = ServerCommunicator()

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
    suspend fun createNewUser(username: String, password: String, description: String): Response {

        val userXTO = UserXTO()
        userXTO.username = username
        userXTO.password = password
        userXTO.description = description

        return server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user", userXTO)
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

        return server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/login", userXTO)
    }

    suspend fun changeUserPassword(newPassword: String, userID: String): Response{
        val userXTO = UserXTO()
        userXTO.password = newPassword
        return server.putRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID", userXTO)
    }

    suspend fun changeUserPrivacyRadius(privacyRadius: Long, userID: String): Response{
        val userXTO = UserXTO()
        userXTO.privacyRadius = privacyRadius
        return server.putRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID", userXTO)
    }

    suspend fun changeUserDescription(description: String, userID: String): Response{
        val userXTO = UserXTO()
        userXTO.description = description
        return server.putRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID", userXTO)
    }

    suspend fun deleteUser(userID: String): Response{

        return server.deleteRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID")
    }

    suspend fun updateUserGpsPosition(latitude: Long, longitude: Long, userID: String): Response{
        val locationXTO = LocationXTO()
        locationXTO.latitude = latitude
        locationXTO.longitude = longitude
        return server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID/location", locationXTO)
    }
}