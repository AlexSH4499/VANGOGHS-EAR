package com.example.vangogh;
import com.example.vangogh.ChordFragment;

import android.os.Bundle;
import android.test.suitebuilder.annotation.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
//import androidx.fragment.app.testing.FragmentScenario;
//import androidx.test.filters.LargeTest;

import androidx.annotation.ContentView;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;


import junit.extensions.ActiveTestSuite;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class ChordFragmentTest {

//    @Rule
//    public ActivityScenarioRule<MainActivity> activityRule
//            = new ActivityScenarioRule<>(MainActivity.class);

//    @Rule
//    public ActiveTestSuite mSuite = new ActiveTestSuite(MainActivity.class.);
    @Test
    public void testSearchForFile() {
        Bundle args = new Bundle();
//         FragmentScenario<ChordFragment> fragment_in_container = FragmentScenario.launchInContainer(ChordFragment.class);
    }

    @Test
    public void test_OnCreate_PutChord()
    {
        FragmentScenario<ChordFragment> fragment_in_container = FragmentScenario.launchInContainer(ChordFragment.class);
        fragment_in_container.moveToState(Lifecycle.State.STARTED);
        onView(withId(R.id.chord_input)).check(matches(isDisplayed())).perform(typeText("A"));
    }

//    @Test
//    public void testOnCreateView() {
//    }
//
//    @Test
//    public void testGetCurrentChord() {
//    }
//
//    @Test
//    public void testOnActivityResult() {
//    }
//
//    @Test
//    public void searchForFile() {
//    }
//
//    @Test
//    public void onCreateView() {
//    }
//
//    @Test
//    public void getCurrentChord() {
//    }
//
//    @Test
//    public void onActivityResult() {
//    }

    private ChordFragment startChordFragment()
    {
//        MainActivity activity = (MainActivity) activityRule.getScenario().
        ChordFragment chordFragment = new ChordFragment();

        return chordFragment;
    }

}