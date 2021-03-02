package io_devices;

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import com.example.database.MusicDataBase;
import com.example.vangogh.FileManager;
import utils.Recognition;
import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.wavFile.WavFileException;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import utils.Device;
//import utils.MapEntryComparator;

/**
 * Class for representing the IO Device of a Microphone for the AudioRecorder class.
 */
public class Microphone implements Device
{

    private final String TAG  = "MIC";
    private MediaRecorder recorder;
    private AudioRecord wav_recorder;
    private MusicDataBase mydb;

    private File  directory ;
    private Context context;
    private long recording_time = 0;
    private Timer timer;

    private int RECORDER_BITS = 16;
    private int RECORDER_SAMPLERATE = 16000;
    private int CONVERT_TO_MB = 1024 * 1024;


    private String recording_timeString="";

    private int RECORDER_CHANNELS = android.media.AudioFormat.CHANNEL_IN_STEREO;
    private int RECORDER_AUDIO_ENCODING = android.media.AudioFormat.ENCODING_PCM_16BIT;
    float audioData[][];//size of 2

    private  int buffer_size = 0;
    private Thread recorderThread;
    private boolean isRecording = false;

    private String output = "";
    private FileManager fileManager;


    /**
     * Credits to Vasanthkumar Velayudham for his tutorial:
     * Where I got classes incorporated here for WAV files
     * https://github.com/VVasanth/Android---Music-Genre-Classifier/
     */
    public Microphone(Context context)
    {
        try{
            this.context = context;
            // create a File object for the parent directory
            fileManager = new FileManager(this.context,false );
            File recorderDirectory = new File(fileManager.getLabelsDirectoryFilePath());//TODO: Dispose of this object, it's wasteful
            directory = recorderDirectory;
            // have the object build the directory structure, if needed.
            if(!directory.exists())
                directory.mkdirs();
        }catch (Exception e){
        e.printStackTrace();
    }

        if(directory.exists()){
            int count = 0;
            if(directory.listFiles() != null)
                 count = directory.listFiles().length;
//            output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecorder/recordings/recording"+count+".mp3";
            output = directory.getAbsolutePath() + "/recording"+count+".mp3";
        }else
        {
            directory.mkdirs();

            int count = 0;
            if(directory.listFiles() != null)
                count = directory.listFiles().length;
//            output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecorder/recordings/recording"+count+".mp3";
            output = directory.getAbsolutePath() + "/recording"+count+".mp3";
        }

        buffer_size = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING)*3;

        audioData =  new float[2][buffer_size];
        timer = new Timer();
    }


    /**
     * Establishes the connection with Mic hardware, sets the Audio format, Encoder and file storage path.
     *
     * @param file_path The filepath where the system will store the audio file created
     */
    public Microphone(String file_path)
    {

        Log.d(TAG, "Using file:"+file_path);
        this.recorder = new MediaRecorder();

        try{
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(file_path);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();
        }catch(Exception e){
            Log.e(TAG,"Error Encountered while setting up mic."+ e);

            e.printStackTrace();

        }
    }

    public boolean start_recording_wav()
    {
        this.wav_recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                buffer_size
        );

        int state = wav_recorder.getState();

        if(state == 1) wav_recorder.startRecording();

        isRecording = true;

        recorderThread  = new Thread()
        {
            @Override
            public void run()
            {
                writeAudioDataToFile();
            }

        };

        recorderThread.start();
        startTimer();

        return isRecording;
    }



    private void writeAudioDataToFile() {
        byte data[] = new byte[buffer_size];
        String filename = FileManager.getTempFilename();
        FileOutputStream os = null;

        try {
            os = new  FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error while opening OutputStream for file:"+filename);
            e.printStackTrace();
        }
        int read = 0;
        if (null != os) {
            while (isRecording) {
                read = wav_recorder.read(data, 0, buffer_size, AudioRecord.READ_BLOCKING);
                if (read > 0) {
                }
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean stop_recording_wav(Context context)
    {
        
        if (null != wav_recorder) {
            isRecording = false;
            int i = wav_recorder.getState();
            if (i == 1) wav_recorder.stop();
            wav_recorder.release();
            wav_recorder = null;
            recorderThread = null;//TODO: Send Termination signal to Thread!
        }
        stopTimer();
        resetTimer();

        String fileName = FileManager.getFilename();
        FileManager.copyWaveFile(FileManager.getTempFilename(), fileName,buffer_size);
        FileManager.deleteTempFile();
        initRecorder();



        //TODO Test checkBytes
        if(!checkBytes(fileName, context)) {
            classifyRecording(fileName, context);
            mydb = new MusicDataBase(context);
            mydb.insertData(fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")), 1);
        }


        return !isRecording;

    }


    private void  initRecorder() {
        recorder =new  MediaRecorder();

        if(directory.exists()){
            int count = 0;
            if(directory.listFiles() != null)
                count = directory.listFiles().length;
//            output = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecorder/recordings/recording"+count+".mp3";
            output = directory.getAbsolutePath() + "/recording"+count+".mp3";
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(output);
    }

    private void startTimer()
    {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                recording_time += 1;
            }
        },1000, 1000);
        updateDisplay();
    }

    private void stopTimer(){
        timer.cancel();
    }


    private void resetTimer() {
        timer.cancel();
        recording_time = 0;
        recording_timeString = "00:00";
    }

    private void updateDisplay(){
        int minutes = (int) recording_time / (60);
        int seconds = (int) recording_time % 60 ;
        String str = String.format("%d:%02d", minutes, seconds);
        recording_timeString = str;
    }

    private String getRecordingTime() {return recording_timeString;}


    public void classifyRecording(final String fileName, final Context context)
    {

        final Thread theed = new Thread();
        Runnable loadrun = new Runnable() {
            @Override
            public void run() {
                theed.setPriority(Thread.NORM_PRIORITY);
                // Creates a toast pop-up.
                // This is to know if this runnable is running on UI thread or not!
                try {


                    String audioFilePath = fileName;

                    try {

                        int defaultSampleRate =  -1; //-1 value implies the method to use default sample rate

                        int defaultAudioDuration = -1 ; //-1 value implies the method to process complete audio duration


                        JLibrosa jLibrosa = new JLibrosa();
                        float audioFeatureValues[] = jLibrosa.loadAndRead(audioFilePath, defaultSampleRate, defaultAudioDuration);
                        ArrayList<Float> audioFeatureValuesList = jLibrosa.loadAndReadAsList(
                                audioFilePath,
                                defaultSampleRate,
                                defaultAudioDuration
                        );


                        ArrayList<List<Float>> splitSongs  = splitSongs(audioFeatureValuesList, 0.5);
                        ArrayList<Float[]> temp = new ArrayList<Float[]>();

                        ArrayList<String> predictionList  = new ArrayList<String>();
                        for(int i=0; i <  splitSongs.size(); i++)
                        {

                            temp.add(splitSongs.get(i).toArray(new Float[splitSongs.get(i).size()]));

                           Float[] audioArr = temp.get(i);
                           float[] intermediate = new float[audioArr.length];

                           for(int j=0;  j < audioArr.length; j++)
                           {
                               intermediate[j] = audioArr[j].floatValue();
                           }

                            float melSpectrogram[][]=
                                    jLibrosa.generateMelSpectroGram(intermediate, 22050, 1024, 128, 256);

                            String prediction  = loadModelAndMakePredictions(melSpectrogram, context);
                            predictionList.add(prediction);

                        }
                        StringBuffer buff = new StringBuffer();
                        for(int i = 0; i < predictionList.size(); i++) {
                            buff.append(predictionList.get(i) + "\n\n");
                        }

                        FileManager.writeToLabelsFile(predictionList, fileManager.getLabelsFilePath() + fileName.substring(fileName.lastIndexOf("/"), fileName.lastIndexOf(".")) + ".txt");


                    }
                    catch(WavFileException e){
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //Start the new prediction thread here!
        Thread predictionThread = new  Thread(loadrun);
        predictionThread.start();

    }

    protected String loadModelAndMakePredictions(float meanMFCCValues[][] , Context context) throws IOException
    {

        String predictedResult = "unknown";


        Interpreter tflite;

        //load the TFLite model in 'MappedByteBuffer' format using TF Interpreter
            MappedByteBuffer tfliteModel  =  FileUtil.loadMappedFile(context, FileManager.getModelPath());
        /** Options for configuring the Interpreter.  */
        Interpreter.Options tfliteOptions = new  Interpreter.Options();
        tfliteOptions.setNumThreads(2);
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        //get the datatype and shape of the input tensor to be fed to tflite model
        int imageTensorIndex = 0;

        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        int imageDataShape[] = tflite.getInputTensor(imageTensorIndex).shape();

        //get the datatype and shape of the output prediction tensor from tflite model
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape();
        DataType probabilityDataType =
                tflite.getOutputTensor(probabilityTensorIndex).dataType();



            //The 4 at the end is the amount of bytes that a float occupies in Java
            // taken from the mfcc mean array
            ByteBuffer byteBuffer  = ByteBuffer.allocate(63984);
        for(int i= 0;i <  meanMFCCValues.length; i++){
            float[] valArray= meanMFCCValues[i];
            int[] inpShapeDim = {1,1,meanMFCCValues[0].length,1};
            TensorBuffer valInTnsrBuffer = TensorBuffer.createDynamic(imageDataType);
            valInTnsrBuffer.loadArray(valArray, inpShapeDim);
            ByteBuffer  valInBuffer = valInTnsrBuffer.getBuffer();
            byteBuffer.put(valInBuffer);
        }

        byteBuffer.rewind();

        TensorBuffer outputTensorBuffer =
                TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        //run the predictions with input and output buffer tensors to get probability values across the labels
        tflite.run(byteBuffer, outputTensorBuffer.getBuffer());


        //Code to transform the probability predictions into label values
        String ASSOCIATED_AXIS_LABELS = "labels.txt";
            List<String> associatedAxisLabels  = new ArrayList<>() ;
        try {
            associatedAxisLabels = FileUtil.loadLabels(context, ASSOCIATED_AXIS_LABELS);
        } catch ( IOException e) {
            Log.e("tfliteSupport", "Error reading label file", e);
        }

        //Tensor processor for processing the probability values and to sort them based on the descending order of probabilities
            TensorProcessor probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(0.0f, 255.0f)).build();
        if (null != associatedAxisLabels) {
            // Map of labels and their corresponding probability
            TensorLabel labels = new TensorLabel(
                    associatedAxisLabels,
                    probabilityProcessor.process(outputTensorBuffer)
            );

            // Create a map to access the result based on label
            Map<String, Float> floatMap = labels.getMapWithFloatValue();

            //function to retrieve the top K probability values, in this case 'k' value is 1.
            //retrieved values are storied in 'Recognition' object with label details.
            List<Recognition> resultPrediction  = getTopKProbability(floatMap);

            //get the top 1 prediction from the retrieved list of top predictions
            predictedResult = getPredictedValue(resultPrediction);

        }
        return predictedResult;

}

//    private String getModelPath() {
//        String res = "model.tflite";
//        return res;
//    }


    public String  getPredictedValue(List<Recognition> predictedList) {
        Recognition top1PredictedValue  = predictedList.get(0);
    return top1PredictedValue.getTitle();
}



    /** Gets the top-k results.  */
    protected List<Recognition> getTopKProbability(Map<String, Float> labelProb ) {
    // Find the best classifications.
    int MAX_RESULTS = 1;
    int current = 5;
    Map.Entry<String, Float> current_max = labelProb.entrySet().iterator().next();

    Stack<Map.Entry<String,Float>> recogs = new Stack<>();

    for (Map.Entry<String, Float> entry : labelProb.entrySet()) {

        if(entry.getValue() > current_max.getValue()) {
            recogs.push(entry);
            current_max = entry;
        }

    }

    ArrayList<Recognition> recognitions = new ArrayList();
    for(Map.Entry<String, Float>entry : recogs)
    {
        if(current > 1)
            recognitions.add(new Recognition(entry.getKey(), entry.getKey(), entry.getValue()));
        current--;
    }

    return recognitions;
}



    private ArrayList<List<Float>> splitSongs(ArrayList<Float> audioFeatureValuesList, double overlap) {
        int chunk = 3300;
        int offset =( int) (chunk * (1 - overlap)) ;
        int x_shape = audioFeatureValuesList.size();

        float x_max_index = x_shape / chunk;

        float x_max = (x_max_index * chunk) - chunk;

        ArrayList<List<Float>>splitSongValList = new ArrayList<List<Float>>();

        for( int i = 0; i <  x_max + 1; i+=offset){
            splitSongValList.add(audioFeatureValuesList.subList(i, i+chunk));
        }

        return splitSongValList;
    }

    public boolean release()
    {
        try {
            recorder.release();
        }catch(Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "Error releasing recorder object");
            return false;
        }
        return true;
    }


    /**
     * Initiates the process of storing data from the microphone into the internal file
     * @return boolean representing if it was successful or not
     */
    public boolean start()
    {
        try{
            recorder.start();
        }catch(Exception e)
        {
            Log.e(TAG, "Error while attempting to start recording:"+e);
            e.printStackTrace();

            return false;
        }
        finally {
            return true;
        }
    }

    /**
     * Halts he process of storing data from the microphone into the internal file
     * @return boolean representing if it was successful or not
     */
    public boolean stop()
    {
        try{
            recorder.stop();
            recorder.reset();
            recorder.release();
        }catch(Exception e)
        {
            Log.e(TAG, "Error while attempting to stop recording:"+e);
            e.printStackTrace();
            return false;
        }
        finally {
            return true;
        }
    }

    @Override
    public boolean reset() {

        try{
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(FileManager.getTempFilename()); //TODO: test this, it may cause errors since temp isn't deleted explicitly!
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();

        }catch(Exception e)
        {
            Log.e(TAG, "Error while resetting microphone:"+e);
            e.printStackTrace();

            return false;
        }
        finally {
            return true;
        }

    }

    /**
     * Resets the process of storing data from the microphone into the new internal file @param file_path
     * @return boolean representing if it was successful or not
     */
    public boolean reset(String file_path)
    {
        try{
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(file_path);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();

        }catch(Exception e)
        {
            Log.e(TAG, "Error while resetting microphone:"+e);
            e.printStackTrace();

            return false;
        }
        finally {
            return true;
        }
    }

    public boolean checkBytes(String filePath, Context context) {
        File stopThis = new File(filePath);
        long fileSize = stopThis.length();
        if(fileSize/CONVERT_TO_MB > 41) {
            Toast.makeText(context,"File Size Too Big!", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    public void fileLogic(String fileName, Context context) {

        FileManager.copyWaveFile(FileManager.getTempFilename(), fileName, buffer_size);
        FileManager.deleteTempFile();
        initRecorder();

        if(!checkBytes(fileName, context)) {
            classifyRecording(fileName, context);
            mydb = new MusicDataBase(context);
            mydb.insertData(fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(".")), 1);
        }
    }


}
