package com.example.vangogh;

/**
 * Fix used for dependencies:
 * https://stackoverflow.com/questions/56558775/launchfragmentincontainer-unable-to-resolve-activity-in-android
 */


import androidx.lifecycle.Lifecycle;
import androidx.test.filters.SmallTest;
import androidx.fragment.app.testing.FragmentScenario;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4ClassRunner.class)
@SmallTest
public class AudioRecorderTest {

    FragmentScenario audioRecorder;
//    @Rule
//    public ActivityScenario<MainActivity> activityRule =  ActivityScenario.launch(MainActivity.class);

    @Before
    public void setUpActivityAndFragment()
    {
       audioRecorder = FragmentScenario.launchInContainer(AudioRecorder.class);
    }



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

        //Perform click here
        onView(withId(R.id.microphone_button)).perform(click());

        //Check Toasts!
        //TODO: Implement Activity Scenario to test this
        //Perform click here
        onView(withId(R.id.microphone_button)).perform(click());
    }

    @Test
    public void openAudioFragment_StopRecording()
    {
        audioRecorder = FragmentScenario.launchInContainer(AudioRecorder.class);
        audioRecorder.moveToState(Lifecycle.State.STARTED);
        //Verify Mic Record Button is displayed
        onView(withId(R.id.microphone_button)).check(matches(isDisplayed()));

        //Verify button is clickable
        onView(withId(R.id.microphone_button)).check(matches(isClickable()));

        //Verify button has "Start" string
        onView(withId(R.id.microphone_button)).check(matches(withText("Start")));

        //Perform click here
        onView(withId(R.id.microphone_button)).perform(click());

        //Verify Mic Record Button is displayed
        onView(withId(R.id.microphone_button)).check(matches(isDisplayed()));

        //Verify button is clickable
        onView(withId(R.id.microphone_button)).check(matches(isClickable()));

        //Verify button has "Start" string
        onView(withId(R.id.microphone_button)).check(matches(withText("Stop")));

        //Perform click here
        onView(withId(R.id.microphone_button)).perform(click());

        //Check Toasts!
        //TODO: Implement Activity Scenario to test this
    }

//    @Test
//    public void onRequestPermissionsResult() {
//    }

//    @Test
//    public void testOnCreateView() {
//    }
//
//    @Test
//    public void testOnDestroyView() {
//    }
//
//    @Test
//    public void testOnRequestPermissionsResult() {
//    }


}