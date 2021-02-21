package com.example.vangogh;

import android.view.Gravity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.DrawerActions.open;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;



import static org.junit.Assert.*;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
public class MainActivityTest {

    @Test
    public void test_MainActivity_Drawer_Layout_Loads() {

        //Launches activity
        ActivityScenario scenario =  ActivityScenario.launch(MainActivity.class);

        //this id is the MainActivity parent layout
        //Verify activity loads its XML Layout
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void test_MainActivity_hasToolBar()
    {
        //Launches activity
        ActivityScenario scenario =  ActivityScenario.launch(MainActivity.class);

        //verify activity contains the toolbar
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
    }

    @Test
    public void test_MainActivity_hasNavDrawerClosed()
    {
        //Launches activity
        ActivityScenario scenario =  ActivityScenario.launch(MainActivity.class);

        //open Drawer
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)));// Left Drawer should be closed.

    }

    @Test
    public void test_MainActivity_hasNavDrawerOpen()
    {
        //Launches activity
        ActivityScenario scenario =  ActivityScenario.launch(MainActivity.class);

        //open Drawer
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed())) // Left Drawer should be closed.
                .perform(open(Gravity.LEFT)); // Open Drawer

        //verify activity contains the Navigation View
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));
    }

    @Test
    public void test_MainActivity_hasNavDrawerOpen_Try_ChordsFragment()
    {
        //Launches activity
        ActivityScenario scenario =  ActivityScenario.launch(MainActivity.class);

        //open Drawer
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed())) // Left Drawer should be closed.
                .perform(open(Gravity.LEFT)); // Open Drawer

        //verify activity contains the Navigation View
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()));

        // Start the screen of your activity.
        onView(withId(R.id.nvView))
                .perform(NavigationViewActions.navigateTo(R.id.nav_first_fragment));

        onView(withId(R.id.chord_input)).check(matches(isDisplayed()));
    }

}