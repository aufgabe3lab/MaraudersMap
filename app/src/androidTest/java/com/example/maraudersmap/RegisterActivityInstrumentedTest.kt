package com.example.maraudersmap

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * AndroidTest
 * @author Felix Kuhbier
 * @since 2023.01.01
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
internal class RegisterActivityInstrumentedTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(RegisterActivity::class.java)

    companion object{
        const val USERNAME = "Username"
        const val PASSWORD = "Password"
        const val PASSWORD_CONFIRM_CORRECT = "Password"
    }


    @Before
    fun setUp() {
        onView(withId(R.id.registerUsername_editText)).perform(replaceText(USERNAME))
        onView(withId(R.id.registerPassword_editText)).perform(replaceText(PASSWORD))
        onView(withId(R.id.confirmPassword_editText)).perform(replaceText(PASSWORD_CONFIRM_CORRECT))
    }

    @Test
    fun usernameTextFieldTest(){
        onView(withId(R.id.registerUsername_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.registerUsername_editText)).check(matches(isFocusable()))
        onView(withId(R.id.registerUsername_editText)).check(matches(withText(USERNAME)))
    }

    @Test
    fun passwordTextFieldTest(){
        onView(withId(R.id.registerPassword_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.confirmPassword_editText)).check(matches(isDisplayed()))

        onView(withId(R.id.registerPassword_editText)).check(matches(isFocusable()))
        onView(withId(R.id.confirmPassword_editText)).check(matches(isFocusable()))

        onView(withId(R.id.registerPassword_editText)).check(matches(withText(PASSWORD)))
        onView(withId(R.id.confirmPassword_editText)).check(matches(withText(PASSWORD_CONFIRM_CORRECT)))
    }

    @Test
    fun registerButtonTest(){
        onView(withId(R.id.registerButton)).check(matches(isDisplayed()))
        onView(withId(R.id.registerButton)).check(matches(isClickable()))

        //Test LoginActivity launch after successful registration
        onView(withId(R.id.registerUsername_editText)).perform(replaceText("A."))
        onView(withId(R.id.registerPassword_editText)).perform(replaceText("abc"))
        onView(withId(R.id.confirmPassword_editText)).perform(replaceText("abc"))
        onView(withId(R.id.registerButton)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.loginHeader)).check(matches(isDisplayed()))

        //Deletes the user registered before
        onView(withId(R.id.loginUsername_editText)).perform(replaceText("A."))
        onView(withId(R.id.loginPassword_editText)).perform(replaceText("abc"))
        onView(withId(R.id.loginButton)).perform(click())
        Thread.sleep(1000)
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText("Settings")).perform(click())
        onView(withId(R.id.deleteAccount_button)).perform(click())
        onView(withText(R.string.yes_dialogText)).perform(click())


    }

    @Test
    fun loginLinkTest(){
        onView(withId(R.id.loginLink_textView)).check(matches(isDisplayed()))
        onView(withId(R.id.loginLink_textView)).check(matches(isClickable()))

        onView(withId(R.id.loginLink_textView)).perform(click())
        onView(withId(R.id.loginHeader)).check(matches(isDisplayed()))

    }

}