package com.example.maraudersmap

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runners.MethodSorters
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister


/**
 * This test class checks if the API methods can create a successfully server request.
 *
 * By creating a new test account, then testing all api methods and then deleting that
 * account, this test class works as long as the user "test_user" is not occupied or if it has
 * a different password than "test_password".
 *
 * This class does not test the variety of different input values and edge cases, this will
 * be done in the Android tests for each Activity itself!
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)  // needed to be able to time the sequence of the methods, deleting a account must be done after it got created for example
class UserControllerAPITest {

    private lateinit var controller: UserControllerAPI
    private lateinit var serializer: Serializer
    private lateinit var userID : String

    @get:Rule
    val testName = TestName()

    /**
     * Setup and collecting user data (userID & jsonWebToken) for methods that need it
     */
    @Before
    fun setup() = runBlocking {
        controller = UserControllerAPI()
        serializer = Persister()

        if (testName.methodName != "aCreateNewUserSuccessTest") {
            val response = controller.loginUser("test_user","test_password")
            val xmlBody = response.body!!.string()
            val userData = serializer.read(LoginActivity.ExtractData::class.java, xmlBody)
            userID = userData.id.toString()
            LoginActivity.jsonWebToken = response.headers.last().second
        }
    }

    /**
     * Creates a new user on the server and checks for a successful response
     * Method name starts with "a" to make sure it is run at the as the first test of this test class
     */
    @Test
    fun aCreateNewUserSuccessTest() = runBlocking {
        val username = "test_user"
        val password = "test_password"
        val description = "test_description"

        val response = controller.createNewUser(username, password, description)

        assertTrue(response.isSuccessful)
    }

    /**
     * Changes the data of a account on the server and checks for a successful response
     * Second request needed to change the password back to avoid bugs
     */
    @Test
    fun changeUserStoredServerData() = runBlocking{
        val response1 = controller.changeUserStoredServerData("new_password",10L,"first_updated_description",userID)
        assertTrue(response1.isSuccessful)
        val response2 = controller.changeUserStoredServerData("test_password",-1L,"second_updated_description",userID)
        assertTrue(response2.isSuccessful)
    }

    /**
     * Changes the gps position of the user on the server and checks for a successful response
     */
    @Test
    fun updateUserGpsPosition() = runBlocking {
        val response = controller.updateUserGpsPosition(10.0,10.0,userID)
        assertTrue(response.isSuccessful)
    }

    /**
     * Gets the locations within a radius of all visible users from the server and checks for a successful response
     */
    @Test
    fun getLocationsWithinRadiusSuccessTest() = runBlocking{
        val response = controller.getLocationsWithinRadius(5L,0.0,0.0)
        println(response.code)
        assertTrue(response.isSuccessful)
    }

    /**
     * Deletes a user on the server and checks for a successful response
     * Method name starts with "x" to make sure it is run at the end of this test class
     */
    @Test
    fun xDeleteUserSuccessTest() = runBlocking {

        val response = controller.deleteUser(userID)
        assertTrue(response.isSuccessful)
    }
}