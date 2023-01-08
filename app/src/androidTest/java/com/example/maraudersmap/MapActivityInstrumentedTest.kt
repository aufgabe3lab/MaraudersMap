package com.example.maraudersmap

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AndroidTest
 * @author Leo Kalmbach
 * @since 2023.01.08

 */
@RunWith(AndroidJUnit4::class)
class MapActivityInstrumentedTest {

    @get:Rule
    val rule = ActivityScenarioRule(MapActivity::class.java)

    @Test
    fun mapView() {
        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.number_tV)).check(matches(isDisplayed()))
        onView(withId(R.id.number_tV)).check(matches(withText("Displayed Users: 0")))
        onView(withId(R.id.center_btn)).check(matches(isDisplayed()))
        onView(withId(R.id.center_btn)).perform(click())
    }

    @Test
    fun settingsMenuItem() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText("Settings")).check(matches(isDisplayed()))
        onView(withText("Settings")).perform(click())
        onView(withId(R.id.saveSettings_button)).check(matches(isDisplayed()))
    }

    @Test
    fun logOutMenuItem() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText("Log out")).check(matches(isDisplayed()))
        onView(withText("Log out")).perform(click())
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
    }
}