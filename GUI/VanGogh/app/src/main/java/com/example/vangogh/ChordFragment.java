package com.example.vangogh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dqt.libs.chorddroid.classes.Chord;
import com.dqt.libs.chorddroid.helper.DrawHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import chords.ChordFactory;
import chords.ChordModel;
import chords.ChordToTab;
import chords.ChordValidator;

/**
 * Class for displaying the Chord Diagram View to the user.
 */
public class ChordFragment extends Fragment
{
    int fret_position = 0; // from 0 to 12
    private String selected_chords="";
    private final String TAG = "CHORD FRAG";
    private final int DIAGRAM_WIDTH = 200;
    private final int DIAGRAM_HEIGHT = 200;

    private String current_chord;
    private ArrayList<String> current_chords;
    private DatabaseView dbview;
    private AudioPlayer ap;

    private ChordModel chord;
    private ChordFactory chord_factory;
    private ChordValidator chord_validator;

    //GUI Components for binding the Frontend
    Button update_btn, load_btn,next_chord_btn;
    View view;
    ImageView chord_view;
    EditText editText;

    Context context;

    public ChordFragment(String chord)
    {
        if(this.validateChord(chord)) {
            this.current_chord = chord.toLowerCase();
            this.current_chords = new ArrayList<>();
//            this.current_chords.add(this.current_chord);
        }
        else{
            this.current_chord = "";
        }
    }

    public ChordFragment(ArrayList<String> chords)
    {
        for(String chord : chords) {
            if(this.validateChord(chord)) {
                this.current_chord = chord.toLowerCase();
//                this.current_chords = new ArrayList<>();
                this.current_chords.add(this.current_chord);
            }

        }

        if(this.current_chords.size() == 0)
            this.current_chord = "";
        else
        {
            this.current_chord = current_chords.get(0);
        }



    }


    public ChordFragment() {
        this.current_chords =new ArrayList<>() ;current_chord = "";//Default to empty
    }

    /**
     * Generates an intent for the FileManager activity and awaits a result with code 1234 for a file URI.
     */
    public void searchForFile(boolean tabRequest)
    {
        if(tabRequest)
        {
            Intent intent = new Intent(this.getActivity(), FileManager.class);

            startActivityForResult(intent,5678);
        }

        else {
            // Asks FileManager to be initialized and awaits the result of selected file
            Intent intent = new Intent(this.getActivity(), FileManager.class);

            startActivityForResult(intent, 1234);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        context = this.getActivity().getApplicationContext();
        // Loads the base view XML file
        view = inflater.inflate(R.layout.chord_fragment, container , false);

        // Bind Java Objects to XML Layout Views
        editText = (EditText) view.findViewById(R.id.chord_input);
        update_btn = (Button) view.findViewById(R.id.update_chord);
        load_btn = (Button) view.findViewById(R.id.load_chords);
        chord_view = (ImageView) view.findViewById(R.id.chord_view);

        next_chord_btn = (Button) view.findViewById(R.id.next_chord_button);


        final FragmentManager man = this.getActivity().getSupportFragmentManager();

        next_chord_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
               //update the current chord
                if(current_chords.size() > 0)
                {
                    current_chord = current_chords.get(0);
                    current_chords.remove(0);
                }

                if (validateChord(current_chord)) {
                    Log.d(TAG, "Current chord received: " + current_chord);
//                    current_chord = editText.getText().toString();
                    drawChords(current_chord);
                    dbview = new DatabaseView();
                    try {
                        //getChordsmap() expects a lowercase key!
                        ap = new AudioPlayer(dbview.getChordsmap().get(current_chord.toLowerCase()), context);
//                        Log.d(TAG, "audioplayer received: " + Uri.fromFile(chord));
                        if (ap != null && man.findFragmentByTag("AUDIO PLAYER IN CHORDS") == null)
                            man.beginTransaction().add(R.id.new_audio_fragment_container_view, ap, "AUDIO PLAYER IN CHORDS").commit();/**/
                    } catch (Exception e) {
                        Log.e(TAG, "Current Chord stored:" + current_chord);
                        e.printStackTrace();
                    }

                }
                editText.setText("");
            }
        });


        load_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                searchForFile(true);
            }

        });

        if(current_chord.isEmpty()) {
            // Set callback listener for events on the update button
            update_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (validateChord(editText.getText().toString())) {
                        Log.d(TAG, "editText received: " + editText.getText().toString());
                        current_chord = editText.getText().toString();
                        drawChords(current_chord);
                        dbview = new DatabaseView();
                        try {
                          //getChordsmap() expects a lowercase key!
                            ap = new AudioPlayer(dbview.getChordsmap().get(current_chord.toLowerCase()), context);
//                        Log.d(TAG, "audioplayer received: " + Uri.fromFile(chord));
                            if (ap != null && man.findFragmentByTag("AUDIO PLAYER IN CHORDS") == null)
                                man.beginTransaction().add(R.id.new_audio_fragment_container_view, ap, "AUDIO PLAYER IN CHORDS").commit();/**/
                        } catch (Exception e) {
                            Log.e(TAG, "Current Chord stored:" + current_chord);
                            e.printStackTrace();
                        }

                    }
                    editText.setText("");
                }

            });
        }

        else{
            //We were called with a chord as an argument
            if(validateChord(current_chord))
            {
                try{
                    drawChords(current_chord);
                    ap = new AudioPlayer(dbview.getChordsmap().get(current_chord.toLowerCase()), context);
                    if (ap != null && man.findFragmentByTag("AUDIO PLAYER IN CHORDS") == null)
                        man.beginTransaction().add(R.id.new_audio_fragment_container_view, ap, "AUDIO PLAYER IN CHORDS").commit();

                }catch(Exception e)
                {
                    Log.e(TAG, "Current Chord stored:" + current_chord);
                    e.printStackTrace();
                }
            }else{
                Log.e(TAG, "Invalid Chord:" + current_chord);
            }
        }

        return view;
    }

    private Uri getChordFileUri()
    {
        FileManager fileManager = new FileManager(this.getActivity().getBaseContext().getApplicationContext());
        return fileManager.getChordsFilePathURI();
    }

    private File getChordFile() throws Exception
    {
        FileManager fileManager = new FileManager(this.getActivity().getBaseContext().getApplicationContext());

        return fileManager.getChordFile("g");//defaults to G chord as we know it works
    }


    private File getChordFile(String chord_filename) throws Exception
    {
        FileManager fileManager = new FileManager(this.getActivity().getBaseContext().getApplicationContext());

       return fileManager.getChordFile(chord_filename);

    }


    /**
     * Verifies if the provided @param input_chord is a valid representation of a chord.
     * @param input_chord chord to be validated
     * @return boolean representing if the @param input_chord is valid.
     */
    private boolean validateChord(String input_chord)
    {
        chord_factory = new ChordFactory();
        ArrayList<ChordModel> valid_chords = chord_factory.createValidInternalChords();

        chord_validator = new ChordValidator(valid_chords);

        return (chord_validator.isValidChord(input_chord));

    }


    /**
     * Draws to the Chord Fragment View the chord given by @param chordName
     * @param chordName String representation of the chord to be drawn
     * @param width int representation of diagram width
     * @param height int representation of diagram height
     */
    private void drawChords(String chordName, int width, int height)
    {
        Log.d(TAG, "Processing chord:"+chordName);
        this.chord_factory = new ChordFactory();
        this.chord_validator = new ChordValidator(this.chord_factory.createValidInternalChords());
        ChordModel ch_model = this.chord_validator.extractChord(chordName);
        if(validateChord(ch_model.toString())) {
            // Prepare data
            Resources resources = this.getResources();
            int transpose = 0; // transpose distance (-12 to 12)

            // Draw chord
            try {
                BitmapDrawable chord = DrawHelper.getBitmapDrawable(
                        resources, width, height, chordName, fret_position, transpose);

                // Display chord to image view
                this.chord_view.setImageDrawable(chord);
            }catch(Exception e)
            {
                e.printStackTrace();
                Log.e(TAG,"Error drawing chord:"+chordName);
                Toast.makeText(getActivity(), "Sorry, invalid chord provided!", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getActivity(), "Sorry, invalid chord provided!", Toast.LENGTH_SHORT).show();
        }
    }

    /*
        Draws the Chord Diagram onto the View for the user.
     */
    private void drawChords(String chordName)
    {
        this.drawChords(chordName, DIAGRAM_WIDTH, DIAGRAM_HEIGHT);
    }

    public String getCurrentChord() {return this.current_chord;}


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //Receives the URI of selected file from FileManager class
        if (requestCode == 1234) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("file");
                Uri uri = Uri.parse(result);
//                selected_chords = uri;
                Log.d(TAG, "Saved URI of selected recording:"+uri);



            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // there's no result
            }
        }

        if (requestCode == 5678) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("file");
                Log.d(TAG, "Received Intent URI:"+ result);
                Uri uri = Uri.parse(result);
                selected_chords = result;

                Log.d(TAG, "Saved PATH of selected recording:"+result);

                FileManager fm = new FileManager(this.getActivity());
                try {
                    ArrayList<String> predicted_chords = fm.readFromLabelsFile(uri);
                    if(predicted_chords.size() > 0) {
                        //first we need to empty the current list
                        this.current_chords.clear();//TODO: Implement this with streams/generators instead
                        for(String chord : predicted_chords)
                        {
                            if(validateChord(chord))
                                this.current_chords.add(chord);
                        }
//                        Log.e(TAG,"Passing in Draw:"+predicted_chords.get(0));
                        this.drawChords(this.current_chords.get(0));
                    }

                }catch(Exception e) {
                    e.printStackTrace();
                }
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //TODO: Handle Activity Cancelled Code
            }
        }
    }

}
