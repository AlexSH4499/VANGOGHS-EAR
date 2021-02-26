package com.example.vangogh;
//import androidx.test.filters.LargeTest;

import android.provider.MediaStore;

import androidx.lifecycle.Lifecycle;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.fragment.app.testing.launchFragmentInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//import static android.support.test.espresso.Espresso.onData;
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.action.ViewActions.swipeLeft;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
//import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;


import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class AudioRecorderTest {

    AudioRecorder audio_rec;
    private String emptyOutputFilePath;
    FragmentScenario audioRecorder;
//    @Rule
//    public ActivityScenario<MainActivity> activityRule =  ActivityScenario.launch(MainActivity.class);

    @Before
    public void setUpActivityAndFragment()
    {
//        ActivityScenario.launch(MainActivity.class).onActivity(activity -> {activity.setFragment()})
       audioRecorder = FragmentScenario.launchInContainer(AudioRecorder.class);
    }

//    @Before
//    public void setEmptyOutputFilePath()
//    {
//        emptyOutputFilePath = "";
//    }


    @Test
    public void openAudioFragment_StartRecording()
    {
        audioRecorder = FragmentScenario.launchInContainer(AudioRecorder.class);
        audioRecorder.recreate();
//        FragmentScenario.launchInContainer(AudioRecorder.class);
        audioRecorder.moveToState(Lifecycle.State.STARTED);
        //Verify Mic Record Button is displayed
        onView(withId(R.id.microphone_button)).check(matches(isDisplayed()));

        //Verify button is clickable
        onView(withId(R.id.microphone_button)).check(matches(isClickable()));

        //Verify button has "Start" string
        onView(withId(R.id.microphone_button)).check(matches(withText("Start")));

//        Perform click here
        onView(withId(R.id.microphone_button)).perform(click());

        //Check Toasts!
        //TODO: Implement Activity Scenario to test this
        //Perform click here
        onView(withId(R.id.microphone_button)).perform(click());
    }

    @Test
    public void openAudioFragment_StopRecording()
    {
        FragmentScenario.launchInContainer(AudioRecorder.class);

        //Verify Mic Record Button is displayed
        onView(withId(R.id.microphone_button)).check(matches(isDisplayed()));

        //Verify button is clickable
        onView(withId(R.id.microphone_button)).check(matches(isClickable()));

//        //Verify button has "Start" string
//        onView(withId(R.id.microphone_button)).check(matches(withText("Start")));

        //Perform click here
        onView(withId(R.id.microphone_button)).perform(click());

        //Verify Mic Record Button is displayed
        onView(withId(R.id.microphone_button)).check(matches(isDisplayed()));

        //Verify button is clickable
        onView(withId(R.id.microphone_button)).check(matches(isClickable()));

//        //Verify button has "Start" string
//        onView(withId(R.id.microphone_button)).check(matches(withText("Stop")));

        //Perform click here
        onView(withId(R.id.microphone_button)).perform(click());

        //Check Toasts!
        //TODO: Implement Activity Scenario to test this
    }


    //this wont pass because we cant use the Activity Context since we're not running the entire app
//    @Test
//    public void empty_filename_file_extension_isCorrect()
//    {
//        //This fails because we cant instance a Fragment for the context
//        onView(withId(R.id.new_audio_fragment_container_view));
//        audio_rec = new AudioRecorder();
//        String expected_file_output = "sample.3gp";
//        String test = audio_rec.OutputFilePath("");
//        assertEquals(expected_file_output, test);
//        audio_rec = null;
//    }

//    @Test
//    public void nonEmptyString() {
//    }


//    @Test
//    public void onRequestPermissionsResult() {
//    }

//    @Test
//    public void outputFilePath() {
//    }
//
//    @Test
//    public void testNonEmptyString() {
//    }

    @Test
    public void testOnCreateView() {
    }

    @Test
    public void testOnDestroyView() {
    }

//    @Test
//    public void testOnRequestPermissionsResult() {
//    }
//
//    @Test
//    public void non_empty_filename_file_extension_isCorrect()
//    {
//        audio_rec = new AudioRecorder();
//        String expected_file_output = "a.3gp";
//        String test = audio_rec.OutputFilePath("a");
//        assertEquals(expected_file_output, test);
//        audio_rec = null;
//    }

//    @Test
//    public void non_empty_string_isCorrect()
//    {
//        audio_rec = new AudioRecorder();
//
//        assertTrue(audio_rec.nonEmptyString("a"));
//    }
//
//    @Test
//    public void empty_whitespace_string_isCorrect()
//    {
//        audio_rec = new AudioRecorder();
//
//        assertFalse(audio_rec.nonEmptyString(" "));
//    }
//
//    @Test
//    public void empty_string_isCorrect()
//    {
//        audio_rec = new AudioRecorder();
//
//        assertFalse(audio_rec.nonEmptyString(""));
//    }


}