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
    suspend fun createNewUser(username: String?, password: String?, description: String?): Response {

        val userXTO = UserXTO()
        userXTO.username = username
        userXTO.password = password
        userXTO.description = description

        return server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user", userXTO)    //todo replace link with base link variable
    }

    /**
     * Sends a login request to the server
     *
     * @param username username
     * @param password password for the username
     * @return Response of the request
     */
    suspend fun loginUser(username: String?, password: String?): Response {
        val userXTO = UserXTO()
        userXTO.username = username
        userXTO.password = password

        return server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/login", userXTO)
    }

    /**
     * Sends a request to the server to change the users password
     *
     * @param userID id of user (received after a successful login)
     * @param newPassword new password for the user
     * @return Response of the request
     */
    suspend fun changeUserPassword(newPassword: String?, userID: String?): Response{
        val userXTO = UserXTO()
        userXTO.password = newPassword
        return server.putRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID", userXTO)
    }

    /**
     * Sends a request to the server to change the users privacy radius
     *
     * @param userID id of user (received after a successful login)
     * @param privacyRadius new privacy radius of the user
     * @return Response of the request
     */
    suspend fun changeUserPrivacyRadius(privacyRadius: Long?, userID: String?): Response{
        val userXTO = UserXTO()
        userXTO.privacyRadius = privacyRadius
        return server.putRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID", userXTO)
    }

    /**
     * Sends a request to the server to change the users description
     *
     * @param userID id of user (received after a successful login)
     * @param description new description of the user
     * @return Response of the request
     */
    suspend fun changeUserDescription(description: String?, userID: String?): Response{
        val userXTO = UserXTO()
        userXTO.description = description
        return server.putRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID", userXTO)
    }

    /**
     * Sends a delete request to the server to remove the users account (and all the users information on the server)
     *
     * @param userID id of user (received after a successful login)
     * @return Response of the request
     */
    suspend fun deleteUser(userID: String?): Response{
        return server.deleteRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID")
    }

    /**
     * Sends a request to the server to change the users position
     *
     * @param userID id of user (received after a successful login)
     * @param latitude updated latitude of the user
     * @param longitude updated longitude of the user
     * @return Response of the request
     */
    suspend fun updateUserGpsPosition(latitude: Long?, longitude: Long?, userID: String?): Response{
        val locationXTO = LocationXTO()
        locationXTO.latitude = latitude
        locationXTO.longitude = longitude
        return server.postRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/user/$userID/location", locationXTO)
    }

    /**
     * Sends a request to the server to load all users position within a radius at a position
     *
     * @param radius radius in which we want to load all users
     * @param latitude position where we want to load the users from
     * @param longitude position where we want to load the users from
     * @return Response of the request
     */
    suspend fun getLocationsWithinRadius(radius: Long?, latitude: Long?, longitude: Long?): Response{
        return server.getRequest("https://maraudersmap-ext.hhn.dev/api/v0.2/location/$radius/$latitude/$longitude")
    }
}