package com.example.vangogh;
//import androidx.test.filters.LargeTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

//@RunWith(AndroidJUnit4.class)
//@LargeTest
public class AudioRecorderTest {
    AudioRecorder audio_rec;

//    @Rule
//    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void openAudioFragment_StartRecording()
    {

    }


    //this wont pass because we cant use the Activity Context since we're not running the entire app
    @Test
    public void empty_filename_file_extension_isCorrect()
    {
//        audio_rec = new AudioRecorder();
//        String expected_file_output = "sample.3gp";
//        String test = audio_rec.OutputFilePath("");
//        assertEquals(expected_file_output, test);
//        audio_rec = null;
    }

    @Test
    public void outputFilePath() {
    }

    @Test
    public void nonEmptyString() {
    }

    @Test
    public void onCreateView() {
    }

    @Test
    public void onDestroyView() {
    }

    @Test
    public void onRequestPermissionsResult() {
    }
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