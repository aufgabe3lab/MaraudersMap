package com.example.maraudersmap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runners.MethodSorters
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UserControllerAPITest {

    private lateinit var controller: UserControllerAPI
    private lateinit var serializer: Serializer
    private lateinit var userID : String


    @get:Rule
    val testName = TestName()

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

    @Test
    fun aCreateNewUserSuccessTest() = runBlocking {
        val username = "test_user"
        val password = "test_password"
        val description = "test_description"

        val response = controller.createNewUser(username, password, description)

        assertTrue(response.isSuccessful)
    }


    @Test
    fun changeUserStoredServerData() = runBlocking{
        val response1 = controller.changeUserStoredServerData("new_password",10L,"first_updated_description",userID)
        assertTrue(response1.isSuccessful)
        val response2 = controller.changeUserStoredServerData("test_password",-1L,"second_updated_description",userID)
        assertTrue(response2.isSuccessful)
    }

    @Test
    fun updateUserGpsPosition() = runBlocking {
        val response = controller.updateUserGpsPosition(10.0,10.0,userID)
        assertTrue(response.isSuccessful)
    }

    @Test
    fun getLocationsWithinRadiusSuccessTest() = runBlocking{
        val response = controller.getLocationsWithinRadius(5L,0.0,0.0)
        println(response.code)
        assertTrue(response.isSuccessful)
    }

    @Test
    fun xDeleteUserSuccessTest() = runBlocking {

        val response = controller.deleteUser(userID)
        assertTrue(response.isSuccessful)
    }

}