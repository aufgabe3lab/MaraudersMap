package com.example.maraudersmap

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.maraudersmap.SettingsActivityTest.MyObject.PASSWORD
import com.example.maraudersmap.SettingsActivityTest.MyObject.SECONDPASSWORD
import com.example.maraudersmap.SettingsActivityTest.MyObject.USERNAME
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import com.example.maraudersmap.LoginActivity.UserInformation.description
import com.example.maraudersmap.LoginActivity.UserInformation.privacyRadius
import com.example.maraudersmap.LoginActivity.UserInformation.userID
import okhttp3.internal.wait
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.rules.TestName
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

class SettingsActivityTest {

    object MyObject {
        const val USERNAME = "Username116"
        const val PASSWORD = "password"
        const val SECONDPASSWORD = "password2"

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

        if (testName.methodName == "changeServerStoredUserDataTest") {
            var response = controller.loginUser(USERNAME, PASSWORD)
            if(response.code!=200){
                response = controller.loginUser(USERNAME, SECONDPASSWORD)
            }
            val xmlBody = response.body!!.string()
            val userData = serializer.read(LoginActivity.ExtractData::class.java, xmlBody)
            userID = userData.id.toString()
            LoginActivity.jsonWebToken = response.headers.last().second
        }
    }

    @Test
    fun changeServerStoredUserDataTest() {

        // change server stored user information
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).perform(typeText("12")).perform(closeSoftKeyboard())
        Espresso.onView(withId(R.id.changeDescription_editText)).perform(typeText("Changed Description"))
        Espresso.onView(withText("Save")).perform(click())      // close AlertDialog
        Espresso.onView(withId(R.id.changePassword_editText)).perform(typeText(SECONDPASSWORD)).perform(closeSoftKeyboard())
        Espresso.onView(withText("SAVE")).perform(click())
        Espresso.onView(withText("Yes")).perform(click())       // close AlertDialog

        // checks if device handles the altered EditTexts correctly
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withHint("12 km")))
        Espresso.onView(withId(R.id.changeDescription_editText)).check(matches(withHint("Changed Description")))
        Espresso.onView(withId(R.id.changePassword_editText)).check(matches(withHint("new password")))

        // reset to previous user information
        Espresso.onView(withId(R.id.privacyRadius_editTextNumber)).perform(typeText("5")).perform(closeSoftKeyboard())
        Espresso.onView(withId(R.id.changeDescription_editText)).perform(typeText("Start Description"))
        Espresso.onView(withText("Save")).perform(click())      // close AlertDialog
        Espresso.onView(withId(R.id.changePassword_editText)).perform(typeText(PASSWORD)).perform(closeSoftKeyboard())
        Espresso.onView(withText("SAVE")).perform(click())
        Espresso.onView(withText("Yes")).perform(click())      // close AlertDialog

    }



    @Test
    fun asdf() {

    }
}