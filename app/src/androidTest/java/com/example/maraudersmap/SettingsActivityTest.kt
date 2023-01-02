package com.example.maraudersmap

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.maraudersmap.SettingsActivityTest.MyObject.PASSWORD
import com.example.maraudersmap.SettingsActivityTest.MyObject.SECONDPASSWORD
import com.example.maraudersmap.SettingsActivityTest.MyObject.USERNAME
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import com.example.maraudersmap.SettingsActivityTest.MyObject.CHANGEDDESCRIPTION
import com.example.maraudersmap.SettingsActivityTest.MyObject.STARTDESCRIPTION
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.rules.TestName
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

class SettingsActivityTest {

    object MyObject {
        const val USERNAME = "Username116"
        const val PASSWORD = "password"
        const val SECONDPASSWORD = "password2"
        const val STARTDESCRIPTION = "Start Description"
        const val CHANGEDDESCRIPTION = "Changed Description"

    }
    private lateinit var controller: UserControllerAPI
    private lateinit var serializer: Serializer

    @get:Rule
    val rule = ActivityScenarioRule(SettingsActivity::class.java)
    @get:Rule
    val testName = TestName()

    @Before
    fun setUp() = runBlocking{

        controller = UserControllerAPI()
        serializer = Persister()

        //if (testName.methodName == methods that need information like userID or jsonWebToken to make a server request) {
            var response = controller.loginUser(USERNAME, PASSWORD)
            if(response.code!=200){
                response = controller.loginUser(USERNAME, SECONDPASSWORD)
            }
            val xmlBody = response.body!!.string()
            val userData = serializer.read(LoginActivity.ExtractData::class.java, xmlBody)
            userID = userData.id.toString()
            LoginActivity.jsonWebToken = response.headers.last().second
        //}
    }

    @Test
    fun changeAllServerStoredUserDataTest() {  // description, password, privacy radius

        // change server stored user information
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).perform(typeText("12")).perform(closeSoftKeyboard())
        Espresso.onView(withId(R.id.changeDescription_editText)).perform(typeText(CHANGEDDESCRIPTION))
        Espresso.onView(withText("Save")).perform(click())      // close AlertDialog
        Espresso.onView(withId(R.id.changePassword_editText)).perform(typeText(SECONDPASSWORD)).perform(closeSoftKeyboard())
        Espresso.onView(withText("SAVE")).perform(click())
        Espresso.onView(withText("Yes")).perform(click())       // close AlertDialog

        // checks if application handles the altered EditTexts properly
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("12 km")))
        Espresso.onView(withId(R.id.changeDescription_editText)).check(matches(withHint(CHANGEDDESCRIPTION)))
        Espresso.onView(withId(R.id.changePassword_editText)).check(matches(withHint("new password")))

        // reset to previous user information
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).perform(typeText("5")).perform(closeSoftKeyboard())
        Espresso.onView(withId(R.id.changeDescription_editText)).perform(typeText(STARTDESCRIPTION))
        Espresso.onView(withText("Save")).perform(click())      // close AlertDialog
        Espresso.onView(withId(R.id.changePassword_editText)).perform(typeText(PASSWORD)).perform(closeSoftKeyboard())
        Espresso.onView(withText("SAVE")).perform(click())
        Espresso.onView(withText("Yes")).perform(click())      // close AlertDialog
    }


    @Test
    fun changeUserDescriptionTest(): Unit = runBlocking {       // description

        // update privacy radius to make sure it is not 0 from the beginning
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).perform(typeText("20")).perform(closeSoftKeyboard())
        Espresso.onView(withText("SAVE")).perform(click())
        Espresso.onView(withText("Yes")).perform(click())       // close AlertDialog
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("20 km")))

        // change user description
        Espresso.onView(withId(R.id.changeDescription_editText)).perform(typeText(CHANGEDDESCRIPTION))
        Espresso.onView(withText("Save")).perform(click())      // close AlertDialog
        Espresso.onView(withText("SAVE")).perform(click())
        Espresso.onView(withText("Yes")).perform(click())       // close AlertDialog

        // checks if application handles the altered EditTexts properly
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("0 km"))) // should be 0 because of a backend bug
        Espresso.onView(withId(R.id.changeDescription_editText)).check(matches(withHint(CHANGEDDESCRIPTION)))

        // sends request to the server to get updated user information
        var response = controller.loginUser(USERNAME, PASSWORD)
        if(response.code!=200){
            response = controller.loginUser(USERNAME, SECONDPASSWORD)
        }
        val xmlBody = response.body!!.string()
        val userData = serializer.read(LoginActivity.ExtractData::class.java, xmlBody)
        assertTrue(userData.description == CHANGEDDESCRIPTION)
        assertTrue( "Expected userData.radius to be '0.0', but it was '${userData.radius}'",userData.radius == "0.0")      // checks if bug on server is still existent, if not this will turn out to be false (20.0 != 0.0)

        // reset to previous user description
        Espresso.onView(withId(R.id.changeDescription_editText)).perform(typeText(STARTDESCRIPTION))
        Espresso.onView(withText("Save")).perform(click())      // close AlertDialog
        Espresso.onView(withText("SAVE")).perform(click())
        Espresso.onView(withText("Yes")).perform(click())       // close AlertDialog
    }


    @Test
    fun asdf() {

    }
}