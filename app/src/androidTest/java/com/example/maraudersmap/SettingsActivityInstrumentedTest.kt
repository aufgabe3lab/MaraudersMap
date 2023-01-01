package com.example.maraudersmap

import androidx.test.espresso.Espresso.onView
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
internal class SettingsActivityInstrumentedTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(SettingsActivity::class.java)

    companion object{
        const val INTERVAL = 5L
        const val PRIVACY_RADIUS = 10L
        const val RADIUS = 4L
        const val DESCRIPTION = "Hello World"
        const val PASSWORD = "Password"
    }



    @Before
    fun setUp() {

        onView(withId(R.id.interval_editTextNumber)).perform(replaceText(INTERVAL.toString()))
        onView(withId(R.id.privacyRadius_editTextNumber)).perform(replaceText(PRIVACY_RADIUS.toString()))
        onView(withId(R.id.radiusVisibilty_editTextNumber)).perform(replaceText(RADIUS.toString()))
        onView(withId(R.id.changeDescription_editText)).perform(replaceText(DESCRIPTION))
        onView(withId(R.id.changePassword_editText)).perform(replaceText(PASSWORD))


    }

    @Test
    fun intervalTest(){
        onView(withId(R.id.interval_editTextNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.interval_editTextNumber)).check(matches((isFocusable())))
        onView(withId(R.id.interval_editTextNumber)).check(matches(withText(INTERVAL.toString())))
    }

    @Test
    fun privacyRadiusTest(){
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches((isFocusable())))
        onView(withId(R.id.privacyRadius_editTextNumber)).check(matches(withText(PRIVACY_RADIUS.toString())))
    }

    @Test
    fun radiusTest(){
        onView(withId(R.id.radiusVisibilty_editTextNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.radiusVisibilty_editTextNumber)).check(matches((isFocusable())))
        onView(withId(R.id.radiusVisibilty_editTextNumber)).check(matches(withText(RADIUS.toString())))
    }

    @Test
    fun descriptionTest(){
        onView(withId(R.id.changeDescription_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.changeDescription_editText)).check(matches((isNotFocusable())))
        onView(withId(R.id.changeDescription_editText)).check(matches(withText(DESCRIPTION)))
    }

    @Test
    fun passwordTest(){
        onView(withId(R.id.changePassword_editText)).check(matches(isDisplayed()))
        onView(withId(R.id.changePassword_editText)).check(matches((isFocusable())))
        onView(withId(R.id.changePassword_editText)).check(matches(withText(PASSWORD)))
    }

    @Test
    fun deleteButtonTest(){
        onView(withId(R.id.deleteAccount_button)).check(matches(isDisplayed()))
        onView(withId(R.id.deleteAccount_button)).check(matches(isClickable()))
    }

    @Test
    fun saveButtonTest(){
        onView(withId(R.id.saveSettings_button)).check(matches(isDisplayed()))
        onView(withId(R.id.saveSettings_button)).check(matches(isClickable()))

    }

}