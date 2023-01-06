package com.example.maraudersmap

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 * AndroidTest
 * @author Leo Kalmbach
 * @since 2023.01.06

 */
@RunWith(AndroidJUnit4::class)
class MapActivityInstrumentedTest {

    @get:Rule
    val rule = ActivityScenarioRule(MapActivity::class.java)

    @Test
    fun mapView() {
        Espresso.onView(withId(R.id.map)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.settings_btn)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.settings_btn)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveSettings_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}