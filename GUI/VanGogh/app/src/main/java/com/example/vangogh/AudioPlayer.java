package com.example.vangogh;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import android.widget.SeekBar;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

/**
 * AudioPlayer for playing back chord audio from the user's selection
 */
public class AudioPlayer extends Fragment {

    // String used for debugging with LogCat utility
    private static  final String TAG = "AUDIO PLAYER";
    private static int id;
    private boolean idChecker = false;

    //GUI Elements for interacting with the audio file
    private Button play, stop,pause,forward,back;


    private View view;
    private MediaPlayer player;

    //The URI of the stored internal file to be played
    private Uri file;

    //A handler for the thread designated to update the seekbar
    private Handler handler;

    // SeekBar data for tracking Audio Playback progress
    private SeekBar seekbar;
    private static int one_time_only  = 0;
    private double start_time, final_time;
    private Context context;

    private PermissionManager permissionManager;

    public AudioPlayer(Uri file)
    {
        handler = new Handler();
        start_time = 0;
        final_time = 0;
        this.file = file;
    }

    public  AudioPlayer(Uri file, Context context)
    {
        this(file);
        this.context = context;

    }

    public AudioPlayer(int id, Context context) {
        this.id = id;
        this.context = context;
        this.idChecker = true;
    }



    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null. This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>A default View can be returned by calling {@link(int)} in your
     * constructor. Otherwise, this method returns null.
     *
     * <p>It is recommended to <strong>only</strong> inflate the layout in this method and move
     * logic that operates on the returned View to {@link #onViewCreated(View, Bundle)}.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Loads the base view XML file
        view = inflater.inflate(R.layout.audio_player_layout, container , false);

        handler = new Handler();
        // Bind Java Objects to XML Layout Views

        play = (Button) view.findViewById(R.id.play);
        pause = (Button) view.findViewById(R.id.pause);


        seekbar = (SeekBar)  view.findViewById(R.id.seekbar);
        seekbar.setClickable(false);

        permissionManager = new PermissionManager(this.context);
        PermissionManager.PermissionRequestListener listener = new PermissionManager.PermissionRequestListener()
        {
            @Override
            public void onNeedPermission() {
                requestPermissions(new String[]{ "READ_EXTERNAL_STORAGE"}, 1);
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                showPlayerRational();
            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskAgain() {
                dialogForSettings("Permission Denied", "Now you must allow storage access from settings.");
            }

            @Override
            public void onPermissionGranted() {
                init();
            }



        };





        return view;
    }

    private void init()
    {
        if(idChecker) {
            player = MediaPlayer.create(this.getActivity().getBaseContext(), id);
        } else {
            player = MediaPlayer.create(this.getActivity().getBaseContext(),file);
        }



        // Set callback listener for events on the update button
        play.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                try {
                    if(player != null)
                        player.start();
                    else {
                        Toast.makeText(getActivity().getBaseContext(), "Select A File Again!", Toast.LENGTH_SHORT).show();
                    }

                } catch(Exception e)
                {
                    e.printStackTrace();
                    Log.e(TAG,"Error while trying to start audio player");
                }

                final_time = player.getDuration();
                start_time = player.getCurrentPosition();

                if(one_time_only == 0)
                {
                    seekbar.setMax((int) final_time);
                    one_time_only = 1;
                }

                seekbar.setProgress((int) start_time);
                handler.postDelayed(UpdateSongTime, 100);
                play.setEnabled(false);

                pause.setEnabled(true);
                Toast.makeText(getActivity().getBaseContext(), "Starting audio playback", Toast.LENGTH_SHORT).show();

            }

        });


        pause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity().getBaseContext(), "Pausing audio playback", Toast.LENGTH_SHORT).show();
                player.pause();
                play.setEnabled(true);
                pause.setEnabled(false);
            }

        });
    }

    private void showPlayerRational() {
        new AlertDialog.Builder(this.context).setTitle("Permission Denied").setMessage("Without this permission this app is unable to open storage to take play Audio. Are you sure you want to deny this permission.")
                .setCancelable(false)
                .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       requestPermissions( new String[]{"READ_EXTERNAL_STORAGE"}, 1);
                        dialog.dismiss();
                    }
                }).show();

    }

    private void goToSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + this.context.getPackageName());
        intent.setData(uri);
        startActivity(intent);
    }

    private void dialogForSettings(String title, String msg) {
        new AlertDialog.Builder(this .context).setTitle(title).setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSettings();
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Separate Thread for updating the seekbar object in parallel to the AudioPlayer execution logic.
      */
    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            start_time = player.getCurrentPosition();
            seekbar.setProgress((int)start_time);
            handler.postDelayed(this, 100);
        }
    };

    /**
     * Method for cleaning up the AudioPlayer fragment once it is popped from the process stack.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();
        if (player != null) player.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    openCamera();
                    //Allow player to be used
                } else {
                    // Permission was denied.......
                    Toast.makeText(this.getActivity().getBaseContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

}
