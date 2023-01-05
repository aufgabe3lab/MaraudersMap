package com.example.maraudersmap

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.maraudersmap.SettingsActivityInstrumentedTest.MyObject.PASSWORD
import com.example.maraudersmap.SettingsActivityInstrumentedTest.MyObject.SECONDPASSWORD
import com.example.maraudersmap.SettingsActivityInstrumentedTest.MyObject.USERNAME
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import com.example.maraudersmap.SettingsActivity.SettingsCompanion.interval
import com.example.maraudersmap.SettingsActivity.SettingsCompanion.visibilityRadius
import com.example.maraudersmap.SettingsActivityInstrumentedTest.MyObject.CHANGEDDESCRIPTION
import com.example.maraudersmap.SettingsActivityInstrumentedTest.MyObject.STARTDESCRIPTION
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.rules.TestName
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

/**
 * AndroidTests to test the functionality of the SettingsActivity
 * @author Julian Ertle
 */
class SettingsActivityInstrumentedTest {

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

    /**
     * Sets up all important objects and data fields to be able
     * to test all methods. If all tests fail it might be because the test user
     * in the data field "USERNAME" got deleted and not recreated again.
     */
    @Before
    fun setUp() = runBlocking {

        controller = UserControllerAPI()
        serializer = Persister()

        var response = controller.loginUser(USERNAME, PASSWORD)
        if (response.code != 200) {
            response = controller.loginUser(USERNAME, SECONDPASSWORD)
        }
        val xmlBody = response.body!!.string()
        val userData = serializer.read(LoginActivity.ExtractData::class.java, xmlBody)
        userID = userData.id.toString()
        LoginActivity.jsonWebToken = response.headers.last().second
    }

    /**
     * Deletes the user from the field USERNAME and creates it again at the end
     * again to be able to run this test multiple times. If all tests fail
     * please register the test user manually again.
     */
    @Test
    fun deleteUserTest() = runBlocking {

        // deletes the user
        onView(withId(R.id.deleteAccount_button)).perform(click())
        onView(withText("YES")).perform(click())       // close AlertDialog ; info saved

        // try to login into deleted user -> response should not be 200
        var response = controller.loginUser(USERNAME, PASSWORD)
        if (response.code != 200) {
            response = controller.loginUser(USERNAME, SECONDPASSWORD)
        }
        assertTrue(response.code != 200)    // user deleted

        // create deleted user again for further test purposes
        val username = USERNAME
        val password = PASSWORD
        val description = STARTDESCRIPTION
        response = controller.createNewUser(username, password, description)
        assertTrue(response.isSuccessful)
    }

    /**
     * Changes all data fields on the server in one server request and checks if
     * the device shows the correct data and also checks if the server saved
     * the correct data as well. At the end the data gets reset to the previous data
     */
    @Test
    fun changeAllServerStoredUserDataTest() {  // description, password, privacy radius

        // change server stored user information
        onView(withId(R.id.privacyRadius_editTextNumber)).perform(replaceText("12"))
        onView(withId(R.id.changeDescription_editText)).perform(replaceText(CHANGEDDESCRIPTION))
        onView(withId(R.id.changePassword_editText)).perform(replaceText(SECONDPASSWORD))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved

        // checks if application handles the altered EditTexts properly
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("12 km")))
        onView(withId(R.id.changeDescription_editText)).check(matches(withHint(CHANGEDDESCRIPTION)))
        onView(withId(R.id.changePassword_editText)).check(matches(withHint("new password")))

        // reset to previous server user information
        onView(withId(R.id.privacyRadius_editTextNumber)).perform(replaceText("5"))
        onView(withId(R.id.changeDescription_editText)).perform(replaceText(STARTDESCRIPTION))
        onView(withId(R.id.changePassword_editText)).perform(replaceText(PASSWORD))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())      // close AlertDialog ; info saved
    }

    /**
     * Changes the user description on the server with one server request and checks if
     * the device shows the correct data and also checks if the server saved
     * the correct data as well. At the end the data gets reset to the previous data
     *
     * IMPORTANT! Due to a bug on the server, the privacy radius gets set to 0 if either
     * the password or the description changes (as long as privacy radius doesn't get changed too)!
     * Our application handles that bug by setting the privacy radius to 0 on our device as well
     * after changing the description or password to avoid any misunderstandings for the user.
     */
    @Test
    fun changeUserDescriptionTest(): Unit = runBlocking {       // description

        // update privacy radius to make sure it is not 0 from the beginning
        onView(withId(R.id.privacyRadius_editTextNumber)).perform(replaceText("20"))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("20 km")))

        // change user description
        onView(withId(R.id.changeDescription_editText)).perform(replaceText(CHANGEDDESCRIPTION))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved

        // checks if application handles the altered EditTexts properly
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("0 km"))) // should be 0 because of a backend bug
        onView(withId(R.id.changeDescription_editText)).check(matches(withHint(CHANGEDDESCRIPTION)))

        // sends request to the server to get updated user information
        var response = controller.loginUser(USERNAME, PASSWORD)
        if (response.code != 200) {
            response = controller.loginUser(USERNAME, SECONDPASSWORD)
        }
        val xmlBody = response.body!!.string()
        val userData = serializer.read(LoginActivity.ExtractData::class.java, xmlBody)
        assertTrue(userData.description == CHANGEDDESCRIPTION)
        assertTrue(
            "Expected userData.radius to be '0.0', but it was '${userData.radius}'",
            userData.radius == "0.0"
        )      // checks if bug on server is still existent, if not this will turn out to be false (20.0 != 0.0)

        // reset to previous user description
        onView(withId(R.id.changeDescription_editText)).perform(replaceText(STARTDESCRIPTION))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved
    }

    /**
     * Changes the users interval time on the device and checks if
     * it shows the correct data. No data reset is needed at the end since
     * the application loses this information after a restart.
     */
    @Test
    fun changeUserIntervalTest() {
        // change device stored user interval
        onView(withId(R.id.interval_editTextNumber)).perform(replaceText("10"))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved

        // checks if application handles the altered EditText properly
        onView(withId(R.id.interval_editTextNumber)).check(matches(withHint("10 seconds")))

        // check if device saved the input correctly
        assertTrue("Expected interval to be '10', but it was '${interval}'", interval == 10L)
    }

    /**
     * Changes the users radius on the device and checks if
     * it shows the correct data. No data reset is needed at the end since
     * the application loses this information after a restart.
     */
    @Test
    fun changeUserRadiusTest() {
        // change device stored user visibilityRadius
        onView(withId(R.id.changeRadius_editText)).perform(replaceText("10"))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved

        // checks if application handles the altered EditText properly
        onView(withId(R.id.changeRadius_editText)).check(matches(withHint("10 km")))

        // check if device saved the input correctly
        assertTrue("Expected  to be '10', but it was '${visibilityRadius}'", visibilityRadius == 10)
    }

    /**
     * Changes the user password on the server with one server request and checks if
     * the device shows the correct data and also checks if the server saved
     * the correct data as well. At the end the data gets reset to the previous data
     *
     * IMPORTANT! Due to a bug on the server, the privacy radius gets set to 0 if either
     * the password or the description changes (as long as privacy radius doesn't get changed too)!
     * Our application handles that bug by setting the privacy radius to 0 on our device as well
     * after changing the description or password to avoid any misunderstandings for the user.
     */
    @Test
    fun changeUserPasswordTest(): Unit = runBlocking {

        // change server stored user password
        onView(withId(R.id.changePassword_editText)).perform(replaceText(SECONDPASSWORD))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved

        // checks if application handles the altered EditText properly
        onView(withId(R.id.changePassword_editText)).check(matches(withHint("new password")))

        // login with new password
        val response = controller.loginUser(USERNAME, SECONDPASSWORD)
        assertTrue(response.code == 200)

        // reset to previous password
        onView(withId(R.id.changePassword_editText)).perform(replaceText(PASSWORD))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())      // close AlertDialog ; info saved
    }

    /**
     * Changes the users privacy radius on the server with one server request and checks if
     * the device shows the correct data and also checks if the server saved
     * the correct data as well. At the end the data gets reset to the previous data
     */
    @Test
    fun changeUserPrivacyRadiusTest(): Unit = runBlocking {

        // change server stored user privacy radius
        onView(withId(R.id.privacyRadius_editTextNumber)).perform(replaceText("19"))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved

        // checks if application handles the altered EditTexts properly
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("19 km")))

        // reset value to 0 to avoid test bugs
        onView(withId(R.id.privacyRadius_editTextNumber)).perform(replaceText("0"))
        onView(withText("SAVE")).perform(click())
        onView(withText("Yes")).perform(click())       // close AlertDialog ; info saved

        // checks if application handles the altered EditTexts properly
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("0 km")))
    }

    /**
     * Checks all displayed Elements
     */
    @Test
    fun displayElementsTest() {
        onView(withId(R.id.interval_editTextNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.interval_editTextNumber)).check(matches((isFocusable())))
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches((isFocusable())))
        onView(withId(R.id.changeRadius_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.changeRadius_editText)).check(matches((isFocusable())))
        onView(withId(R.id.changeDescription_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.changeDescription_editText)).check(matches((isNotFocusable())))
        onView(withId(R.id.changePassword_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.changePassword_editText)).check(matches((isFocusable())))
        onView(withId(R.id.deleteAccount_button)).check(matches(isDisplayed()))
        onView(withId(R.id.deleteAccount_button)).check(matches(isClickable()))
        onView(withId(R.id.saveSettings_button)).check(matches(isDisplayed()))
        onView(withId(R.id.saveSettings_button)).check(matches(isClickable()))
    }
}