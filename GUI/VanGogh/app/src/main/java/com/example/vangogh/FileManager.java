package com.example.vangogh;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;



import com.example.database.MusicDataBase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import io_devices.IODeviceManager;
import utils.Controller;
import utils.Device;

/**
 * Class designated with the management of File logic in the system.
 * Queries for file URI's on the device, creates new files and deletes them.
 */
public class FileManager extends Activity implements IODeviceManager
{
    private boolean isTablatureReq = false;
    private String CHORD_DIR = "chords";

    /*Data originally from Microphone class*/
    private static final String AUDIO_RECORDER_FOLDER = "/audiorecorder/recordings";
    private static final File  recordings_directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_RECORDER_FOLDER);
    private static final int RECORDER_BITS = 16;
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int CONVERT_TO_MB = 1024 * 1024;
    private static final String AUDIO_FILE_FORMAT = ".wav";
    private static final String TEMP_FILE_FORMAT = "temp_rec.raw";
    
    private static final int FILE_SELECTED_CODE = 0;
    private static final String TAG = "FILE MANAGER";
    private static final int REQUEST_CHOOSER = 1234;
    private static final int REQUEST_TAB = 5678;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    private ArrayList<String> files = new ArrayList<>();

    private Uri selected_file;

    private Context context;

    public FileManager(Context context, boolean isTablatureReq)
    {
        this(context);
        this.isTablatureReq = isTablatureReq;
    }


    public FileManager(Context context)
    {
        this.context  = context;
    }

    public FileManager()
    {
        this.context = this.getBaseContext();
    }


    /**
     * Generates the Output File Path for the Audio Recorder to store recorded audio.
     *
     * @param filename the name of the file to be created for storing the recorded audio.
     * @param format the file format that the data will be stored as.
     * @return String representation of the Output File Path with specified format
     */
    private String OutputFilePath(String filename, String format)
    {
        String res;
        if(filename != null && filename != " ") {
            res = this.getExternalFilesDir(null).getAbsolutePath() + "/" + filename + "." +format;
        }
        else{
            res = this.getExternalFilesDir(null).getAbsolutePath() + "/" + "sample" + "."+format;
        }
        return res;
    }

    public String getAbsoluteChordsDirPath()
    {
        return getAbsoluteProjectPath() + '/' + CHORD_DIR;
    }


    public static String getModelPath()
    {
        return "model.tflite";
    }

    public String getLabelsFilePath()
    {
        return recordings_directory.getAbsolutePath().substring(0, getLabelsDirectoryFilePath().lastIndexOf("/recordings"));
    }

    public String getLabelsDirectoryFilePath() {
        return recordings_directory.getAbsolutePath();
    }
    
    public String getAbsoluteProjectPath()
    {
        String path_str = this.getExternalFilesDir(null).getAbsolutePath().toString();
        return path_str;
    }


    public Uri getAbsoluteProjectPathURI()
    {
        Uri path = Uri.parse(getAbsoluteProjectPath());
        return path;
    }

    public File getChordsLabelsFile(){
        String res = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecorder/recordings/labels.txt";
        File labels = new File(res);
        return labels;
    }

    //TODO: Fix Read bug where the InputStreams reads even commas as one single line
    // Implement something to split the line by "," like Python's split(",") method.
    public  ArrayList<String> readFromLabelsFile(Uri filepath) throws FileNotFoundException
    {
        ArrayList<String> data = new ArrayList<>();
        try {
            InputStream in = this.context.getContentResolver().openInputStream(filepath);


            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            for (String line; (line = r.readLine()) != null; ) {
               String[] split_str =  line.split(",");
                data.addAll(Arrays.asList(split_str));
            }

        }catch (Exception e) {
                e.printStackTrace();
        }

        Log.d(TAG, "Read data:" + Arrays.toString(data.toArray(new String[data.size()])));
        return data;
    }

    public static String getFilename()
    {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if(!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath().toString() + "/" + SystemClock.currentThreadTimeMillis() + AUDIO_FILE_FORMAT ;
    }

    public static String getTempFilename()
    {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(file.getAbsolutePath(), TEMP_FILE_FORMAT);

//        if(!tempFile.exists())
//            tempFile.mkdir();
//       if (tempFile.exists()) tempFile.delete();
//       tempFile = new File(filepath, TEMP_FILE_FORMAT);
//       return file.getAbsolutePath() + "/" + TEMP_FILE_FORMAT;
        return tempFile.getAbsolutePath();
    }

    public static void deleteTempFile()
    {
        File file = new File(FileManager.getTempFilename());
        file.delete();
    }

    public static boolean writeToLabelsFile(List<String> predictions, String filepath)
    {
        Log.d(TAG, "Labels file with path:"+filepath+"\n received!");
        Log.d(TAG, "Received predictions:" + Arrays.toString(predictions.toArray(new String[predictions.size()])));
        File labels = new File(filepath);
        FileWriter fr ;
        BufferedWriter br;

        if(labels.exists())
        {
            Log.e(TAG, "Labels file with path:"+filepath+"\n already exists!");
            labels.delete();
            return false;
        }

        try {
            fr = new FileWriter(labels);
            br = new BufferedWriter(fr);

            for (String str : predictions) {
                try {
                    br.write(str);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            br.close();
            fr.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }


        return true;
    }

    public static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, long channels,
                                     long byteRate)
    {
        byte header [] = new byte[44];
        header[0] =(byte) 'R';// RIFF/WAVE header
        header[1] =(byte) 'I';
        header[2] =(byte) 'F';
        header[3] = (byte)'F';
        header[4] = (byte)(totalDataLen & 0xff);
        header[5] =(byte) (totalDataLen >> 8 & 0xff);
        header[6] = (byte) (totalDataLen >> 16 & 0xff);
        header[7] = (byte)(totalDataLen >> 24 & 0xff);
        header[8] = (byte)'W';
        header[9] = (byte)'A';
        header[10] =(byte) 'V';
        header[11] = (byte)'E';
        header[12] =(byte) 'f'; // 'fmt ' chunk
        header[13] =(byte) 'm';
        header[14] = (byte)'t';
        header[15] = (byte)' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1 ;// format = 1
        header[21] = 0;
        header[22] = (byte)channels;
        header[23] = 0;
        header[24] = (byte)(longSampleRate & 0xff);
        header[25] = (byte)(longSampleRate >> 8 & 0xff);
        header[26] =(byte) (longSampleRate >> 16 & 0xff);
        header[27] = (byte)(longSampleRate >> 24 & 0xff);
        header[28] = (byte)(byteRate & 0xff);
        header[29] = (byte)(byteRate >> 8 & 0xff);
        header[30] =(byte) (byteRate >> 16 & 0xff);
        header[31] = (byte)(byteRate >> 24 & 0xff);
        header[32] = (byte)(2 * 16 / 8); // block align
        header[33] = (byte)0;
        header[34] = (byte)RECORDER_BITS ;// bits per sample
        header[35] =(byte) 0;
        header[36] = (byte)'d';
        header[37] = (byte)'a';
        header[38] =(byte) 't';
        header[39] = (byte)'a';
        header[40] =(byte) (totalAudioLen & 0xff);
        header[41] =(byte) (totalAudioLen >> 8 & 0xff);
        header[42] = (byte)(totalAudioLen >> 16 & 0xff);
        header[43] = (byte)(totalAudioLen >> 24 & 0xff);

        try{
            out.write(header, 0, 44);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void copyWaveFile(String input, String output, int buffer_size)
    {
        FileInputStream f_in;
        FileOutputStream f_out;
        long total_audio_len = 0;
        long total_length = total_audio_len + 36; //for headers
        long sample_rate = (long) RECORDER_SAMPLERATE;
        int channels = 2;

        long byte_rate = (long) RECORDER_BITS * RECORDER_SAMPLERATE *channels / 8;

        byte data[]  = new byte[buffer_size];

        try{

            f_in = new FileInputStream(input);
            f_out = new FileOutputStream(output);

            total_audio_len = f_in.getChannel().size();
            total_length = total_audio_len + 36;//TODO: MAY BE A BUG HERE
            FileManager.WriteWaveFileHeader(f_out, total_audio_len, total_length, sample_rate, channels, byte_rate);

            while(f_in.read(data) != -1)
            {
                f_out.write(data);
            }

            f_in.close();
            f_out.close();

        }catch(IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, "Error while opening input file:"+ input);
            Log.e(TAG, "Error while opening output file:"+ output);
        }

    }


    /**
     * When the object is created, it finds the File View for the instance life cycle
     * @param savedInstanceState the state of the parent that called the object if it wants to know anything
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {
            //If not ask the user for the permission
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        }
        else {
            Log.d(TAG, "PERMISSION GRANTED");
        }

        if (checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
             requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else {
            Log.d(TAG, "PERMISSION GRANTED");
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, REQUEST_CHOOSER);

    }

    
    /**
     * Allows the user to choose the files that they are seeing
     */
    public Uri returnChosenFile()
    {
        return this.selected_file;
    }


    private void showFilePicker()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECTED_CODE
            );
        } catch(android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(this, "No File Manager found, please install one!", Toast.LENGTH_SHORT).show();
        }
    }

    
    /**
     * When activity initiates a call for an action
     * @param requestCode an int that identifies the type of permission that is being asked
     * @param resultCode an int that identifies the result of the requestCode that was emitted
     * @param data data that is passed from one activity to the other if any
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_TAB:
                Uri tab_uri = data.getData();

                Intent tab_files_d = new Intent();

                tab_files_d.putExtra("file", tab_uri);
                setResult(Activity.RESULT_OK, tab_files_d);
                finish();
                super.onActivityResult(REQUEST_TAB, resultCode,tab_files_d);

                break;

            case 3333:
                Uri wav_uri = data.getData();
                Log.d(TAG, "File URI:" +  wav_uri.toString());
                selected_file=wav_uri;
                String wav_path = wav_uri.getPath();
                Log.d(TAG, "File Path: "+ wav_path);
                //process the file or pass it to data

                Intent wav_files_d = new Intent();
                wav_files_d.putExtra("file", wav_uri.toString());
                setResult(Activity.RESULT_OK, wav_files_d);
                finish();

                super.onActivityResult(3333, resultCode,wav_files_d);
                break;

            case REQUEST_CHOOSER:

                Uri uri = data.getData();
                Log.d(TAG, "File URI:" +  uri.toString());
                selected_file=uri;
                String path = uri.getPath();
                Log.d(TAG, "File Path: "+ path);
                //process the file or pass it to data
                Intent files_d = new Intent();
                files_d.putExtra("file", uri.toString());
                setResult(Activity.RESULT_OK, files_d);
                finish();

                super.onActivityResult(REQUEST_CHOOSER, resultCode,files_d);

                break;

        }

    }


    /**
     * Generates the Output File Path for the Audio Recorder to store recorded audio.
     *
     * @param filename the name of the file to be created for storing the recorded audio.
     * @param format the file format that the data will be stored as.
     * @return String representation of the Output File Path with specified format
     */
    private String InputFilePath(String filename, String format)
    {
        String res;
        if(filename != null && filename != " ") {

            res = this.getExternalFilesDir(null).getAbsolutePath() + "/" + filename + "." +format;
        }
        else{
            res = this.getExternalFilesDir(null).getAbsolutePath() + "/" + "sample" + "."+format;
        }
        return res;
    }

    public Uri getChordsFilePathURI()
    {
        return Uri.fromFile(this.context.getFilesDir());
    }


    public File getChordFile(String chord_filename) throws Exception
    {
        Log.e(TAG, "Current stored context" + this.context.toString());
        Log.e(TAG, "Current stored context directory" + this.context.getFilesDir().getPath().toString());
        File a_file = new File(this.context.getFilesDir(), chord_filename);

        Log.d(TAG, "Created File:"+a_file.toString());
        if(a_file.exists())
            return a_file;
        else{
            throw new Exception("Error while trying to open file:"+a_file.getPath());
        }
    }

    @Override
    public List<Device> devices() {
        return null;
    }

    @Override
    public boolean addDevice(Device device) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean removeDevice(Device device) throws IllegalArgumentException {
        return false;
    }

    @Override
    public Controller getDevice(Device device) throws IllegalArgumentException {
        return null;
    }

    @Override
    public boolean addController(Controller control) {
        return false;
    }

    @Override
    public boolean isValid(Controller control) {
        return false;
    }

}
