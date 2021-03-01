package com.example.vangogh;
import com.dqt.libs.chorddroid.classes.Chord;
import com.example.vangogh.ChordFragment;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.test.suitebuilder.annotation.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
//import androidx.fragment.app.testing.FragmentScenario;
//import androidx.test.filters.LargeTest;


import androidx.annotation.ContentView;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;


import junit.extensions.ActiveTestSuite;

import chords.ChordFactory;
import chords.ChordModel;
import chords.ChordValidator;
import utils.FragmentFactory;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class ChordFragmentTest {
    FragmentScenario<ChordFragment> chord_frag_in_container;
    ChordValidator validator;
    ChordFactory factory;

    @Rule
    public IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setupChordFragment()
    {
//        Intents.init();
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        chord_frag_in_container = FragmentScenario.launchInContainer(ChordFragment.class);
        factory = new ChordFactory();
        factory.createValidInternalChords();
        validator = new ChordValidator(factory.createChords());
    }


    @Test
    public void testSearchForFile() {
//        Bundle args = new Bundle();
//         FragmentScenario<ChordFragment> fragment_in_container = FragmentScenario.launchInContainer(ChordFragment.class);
    }

    @Test
    public void test_OnCreate_Put_ValidChord()
    {
        // init frag and move to Started state
       chord_frag_in_container.recreate();
       chord_frag_in_container.moveToState(Lifecycle.State.STARTED);

       for(ChordModel chord: factory.createChords())
       {
           //Check input exists, perform typing of KNOWN valid chord
           onView(withId(R.id.chord_input)).check(matches(isDisplayed())).perform(typeText(chord.toString()));

           // hit update view button
           onView(withId(R.id.update_chord)).check(matches(isDisplayed()));
           onView(withId(R.id.update_chord)).check(matches(isClickable())).perform(click());

           //Check image is generated
           onView(withId(R.id.chord_view)).check(matches(isDisplayed()));//Detected bug here! must toggle visibility of view

           //Check EditTextView defaults to empty
           onView(withId(R.id.chord_input)).check(matches(not(withText(chord.toString()))));
           onView(withId(R.id.chord_input)).check(matches(withHint("Enter Chord")));
       }

    }

    @Test
    public void test_OnCreate_Put_InvalidChord()
    {
        // init frag and move to Started state
        chord_frag_in_container.recreate();
        chord_frag_in_container.moveToState(Lifecycle.State.STARTED);

        //Check input exists, perform typing of KNOWN valid chord
        onView(withId(R.id.chord_input)).check(matches(isDisplayed())).perform(typeText("Z"));

        // hit update view button
        onView(withId(R.id.update_chord)).check(matches(isDisplayed()));
        onView(withId(R.id.update_chord)).check(matches(isClickable())).perform(click());

        // Check image is generated
        // onView(withId(R.id.chord_view)).check(matches(not(isDisplayed())));//Detected bug here! must toggle visibility of view

        //Check EditTextView defaults to empty
        onView(withId(R.id.chord_input)).check(matches(not(withText("Z"))));
        onView(withId(R.id.chord_input)).check(matches(withHint("Enter Chord")));
    }

    @Test
    public void test_OnReceiving_Tablature_Load_Intent()
    {
        Intent resultData = new Intent();
        String file_uri = "content://com.android.externalstorage.documents/document/primary%3AAudioRecorder%2F1068.txt";//TODO: IDK how to specify a dummy file
        resultData.putExtra("file", file_uri);

        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // init frag and move to Started state
        chord_frag_in_container.recreate();
        chord_frag_in_container.moveToState(Lifecycle.State.STARTED);


//        intending(allOf(hasData(file_uri))).respondWith(result);

        //Stubs the fragment to receive always the resultData
        intending(hasComponent(hasShortClassName(".FileManager"))).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK,resultData));

        onView(withId(R.id.load_chords)).check(matches(isClickable())).perform(click());

//        Matcher<Intent> expectedIntent = hasItem(FileManager.class);
        onView(withId(R.id.chord_view)).check(matches(isDisplayed()));//Must toggle visibility of Img View still!
        onView(withId(R.id.next_chord_button)).check(matches(isDisplayed()));
        onView(withId(R.id.next_chord_button)).check(matches(isClickable())).perform(click());
        onView(withId(R.id.chord_view)).check(matches(isDisplayed()));//Must toggle visibility of Img View still!


    }

    @After
    public void teardown()
    {
//        Intents.release();
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


}