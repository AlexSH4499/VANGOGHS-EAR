package com.example.vangogh;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.*;


import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chords.ChordToTab;
import io_devices.Microphone;
import utils.FragmentFactory;


/**
 * Class for the Main View of the system and where the user will mainly interact
 */
public class MainActivity extends AppCompatActivity implements AppBarConfiguration.OnNavigateUpListener
{

    private PermissionManager permissionManager;
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    Map<String, Integer> permissions;

    AudioRecorder audio_fragment;
    ToggleButton toggle_frags ;
    Button dbview_button;
    Toolbar toolbar;

    private Uri selected_recording;


    private final String TAG = "MAIN";
    private final int REQUEST_READ_STORAGE = 0;
    private final int REQUEST_WRITE_STORAGE = 1;
    private final int REQUEST_RECORD_AUDIO = 2;
    private final int REQUEST_ACCESS_MEDIA = 3;
    private final int ALL_REQ_PERMS = 2020;
    private int clicks = 0;

    private static final String[] PERMISSIONS = {
            "RECORD_AUDIO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "ACCESS_MEDIA_LOCATION"
    };


    private void preparePermissions()
    {
        int  i = 0;
        permissions = new HashMap<>();
        for(String permission : PERMISSIONS)
        {
            permissions.put(permission ,i);
            i+=1;
        }
    }

    /**
     * When the object is created, it finds the Main View for the instance life cycle
     * @param savedInstanceState the state of the parent that called the object if it wants to know anything
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        // Setup toggle to display hamburger icon with nice animation

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);


        preparePermissions();
        requestPermissions();
    }

    private ActionBarDrawerToggle setupDrawerToggle()
    {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView)
    {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }});
    }

    public void selectDrawerItem(MenuItem menuItem) {

        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;

        switch(menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = ChordFragment.class;
                break;
            case R.id.nav_second_fragment:
                fragmentClass = TablatureFragment.class;
                break;
            case R.id.nav_third_fragment:
                fragmentClass = AudioRecorder.class;
                break;
            default:
                fragmentClass = AudioRecorder.class;
                break;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);

        // Set action bar title
        setTitle(menuItem.getTitle());

        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);
        return true;
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }


    /**
     * Generates an intent for the FileManager activity and awaits a result with code 1234 for a file URI.
     */
    public void searchForFile(boolean tabRequest, boolean wavFileRequest)
    {
        if(wavFileRequest)
        {
            Intent intent = new Intent(this, FileManager.class);
            startActivityForResult(intent,3333);
            return;
        }

        if(tabRequest)
        {
            Intent intent = new Intent(this, FileManager.class);
            startActivityForResult(intent,5678);
            return;
        }

        else {
            // Asks FileManager to be initialized and awaits the result of selected file
            Intent intent = new Intent(this, FileManager.class);
            startActivityForResult(intent, 1234);
        }

    }


    /**
     * Asks the user for IO device permissions such as accessing storage and the microphone
     */
    private void requestPermissions()
    {
        requestPermissions((String[])permissions.keySet().toArray(new String[permissions.keySet().size()]),ALL_REQ_PERMS);
    }

    /**
     * When activity requires a permission
     * @param requestCode an int that identifies the type of permission that is being asked
     * @param permissions string array of the permissions being asked
     * @param grantResults int array with numbers if it has a 0, it is denied the result, else anything different,
     * it is granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[] , int grantResults[])
    {
        switch(requestCode)
        {
            case REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Granted Read Storage Permission");
                }
                return ;

            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Granted Write Storage Permission");
                }
                return ;

            case REQUEST_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Granted Record Audio Permission");
                    return;
                }

                return ;

            case REQUEST_ACCESS_MEDIA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Granted Access Media Permission");
                    return;
                }

                return ;

            case ALL_REQ_PERMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Granted All Permissions");
                    return;
                }
                return ;

            default:
                Log.d(TAG,"Unknown permission requested:["+requestCode+"]\n");
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
//            case 3333:
//                if(resultCode == Activity.RESULT_OK)
//                {
//                    String result=data.getStringExtra("file");
//                    Uri uri = Uri.parse(result);
//                    selected_recording = uri;
//                    Log.d(TAG, "Saved URI of selected recording:"+uri);
//
//                    Microphone mic = new Microphone();
//                    mic.classifyRecording(selected_recording.getPath(), this.getApplication().getApplicationContext());
//
//                    Log.e(TAG, "Creating Labels txt file!");
//                    Toast.makeText(this, "Processing WAV File!", Toast.LENGTH_SHORT).show();
//
//
//                }
//                break;

            //Receives the URI of selected file from FileManager class
            //Plays the file received from the FileManager
            case 1234:
                if(resultCode == Activity.RESULT_OK)
                {
//                    String result=data.getStringExtra("file");
//                    Uri uri = Uri.parse(result);
//                    selected_recording = uri;
//                    Log.d(TAG, "Saved URI of selected recording:"+uri);
//                    FragmentManager man = this.getSupportFragmentManager();
//                    AudioPlayer audio_player = new AudioPlayer(selected_recording);
//                    removeAllFragments();
//                    man.beginTransaction().add(R.id.fragment_container_view, audio_player, "AUDIO PLAYER").commit();
                }
                break;

            case 3333:
                if(resultCode == Activity.RESULT_OK)
                {
                    String result=data.getStringExtra("file");
                    Log.d(TAG, "Received Intent URI:"+ result);
                    Uri uri = Uri.parse(result);
                    selected_recording = uri;

                    Log.d(TAG, "Saved PATH of selected recording:"+result);

                    Microphone mic = new Microphone();
                    mic.classifyRecording(uri.getPath(), this);
                    FragmentManager man = this.getSupportFragmentManager();
                    FileManager fm = new FileManager(this);
                    Toast.makeText(this, "Processing File", Toast.LENGTH_SHORT).show();
//                try {
//                    ArrayList<String> predicted_chords =
//                            fm.readFromLabelsFile(uri);
////                String predicted_tablature = ChordToTab.totalTablature(ChordToTab.constructTab(predicted_chords.toArray(new String[predicted_chords.size()])));
//                    String predicted_tablature = ChordToTab.convertStringChords(predicted_chords);
//                    TablatureFragment tab_frag = new TablatureFragment(predicted_tablature);
//
//                    removeAllFragments();
//
//                    man.beginTransaction().add(R.id.fragment_container_view, tab_frag, "TABLATURE").commit();
//                }catch(Exception e) {
//                    e.printStackTrace();
//                }
                }

                break;

            case 5678:

                if(resultCode == Activity.RESULT_OK)
                {
                    String result=data.getStringExtra("file");
                    Log.d(TAG, "Received Intent URI:"+ result);
                    Uri uri = Uri.parse(result);

                    Log.d(TAG, "Saved PATH of selected recording:"+result);

                    FragmentManager man = this.getSupportFragmentManager();
                    FileManager fm = new FileManager(this);
                    try
                    {
                        ArrayList<String> predicted_chords = fm.readFromLabelsFile(uri);
                        String predicted_tablature = ChordToTab.convertStringChords(predicted_chords);
                        TablatureFragment tab_frag = new TablatureFragment(predicted_tablature);

//                    removeAllFragments();
//                    man.beginTransaction().add(R.id.fragment_container_view, tab_frag, "TABLATURE").commit();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                break;


            default:
                break;

        }







    }

    /**
     * Shows messages on a separate window
     * @param title the title of the message that is demonstrated on the window
     * @param msg the message demonstrated on the window
     */
    public void showMessage(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.show();
    }



}

