package com.example.maraudersmap

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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
internal class LoginActivityInstrumentedTest{

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    companion object{
        const val USERNAME = "Username"
        const val PASSWORD = "Password"
    }

    @Before
    fun setUp() {
        onView(withId(R.id.loginUsername_editText)).perform(replaceText(USERNAME))
        onView(withId(R.id.loginPassword_editText)).perform(replaceText(PASSWORD))
    }

    @Test
    fun usernameTextFieldTest(){
        onView(withId(R.id.loginUsername_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.loginUsername_editText)).check(matches(isFocusable()))
        onView(withId(R.id.loginUsername_editText)).check(matches(withText(USERNAME)))
    }

    @Test
    fun passwordTextFieldTest(){
        onView(withId(R.id.loginPassword_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.loginPassword_editText)).check(matches(isFocusable()))
        onView(withId(R.id.loginPassword_editText)).check(matches(withText(PASSWORD)))
    }

    @Test
    fun loginButtonTest(){
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
        onView(withId(R.id.loginButton)).check(matches(isClickable()))


        onView(withId(R.id.loginUsername_editText)).perform(replaceText("l."))
        onView(withId(R.id.loginPassword_editText)).perform(replaceText("abc"))
        onView(withId(R.id.loginButton)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.map)).check(matches(isDisplayed()))
    }

    @Test
    fun registerLinkTest(){
        onView(withId(R.id.registerLink_textView)).check(matches(isDisplayed()))
        onView(withId(R.id.registerLink_textView)).check(matches(isClickable()))

        onView(withId(R.id.registerLink_textView)).perform(click())
        onView(withId(R.id.registerHeader)).check(matches(isDisplayed()))
    }




}