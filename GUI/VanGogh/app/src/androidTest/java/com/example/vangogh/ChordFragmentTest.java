package com.example.vangogh;
import com.example.vangogh.ChordFragment;

import android.os.Bundle;
import android.test.suitebuilder.annotation.LargeTest;

//import androidx.fragment.app.testing.FragmentScenario;
//import androidx.test.filters.LargeTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;


import junit.extensions.ActiveTestSuite;

import static org.junit.Assert.assertEquals;

//@RunWith(AndroidJUnit4.class)
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
//         FragmentScenario<ChordFragment> fragment_in_container = launchFragment<ChordFragment>();
    }

    @Test
    public void testOnCreateView() {
    }

    @Test
    public void testGetCurrentChord() {
    }

    @Test
    public void testOnActivityResult() {
    }

    @Test
    public void searchForFile() {
    }

    @Test
    public void onCreateView() {
    }

    @Test
    public void getCurrentChord() {
    }

    @Test
    public void onActivityResult() {
    }

    private ChordFragment startChordFragment()
    {
//        MainActivity activity = (MainActivity) activityRule.getScenario().
        ChordFragment chordFragment = new ChordFragment();

        return chordFragment;
    }

}