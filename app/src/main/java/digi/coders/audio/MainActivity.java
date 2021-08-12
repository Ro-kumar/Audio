package digi.coders.audio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import digi.coders.audio.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    DatabaseReference drAudio;
    StorageReference srAudio;



    private static final int REQUEST_RECORD_AUDIO = 0;
    String AUDIO_FILE_PATH ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityMainBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );


        srAudio = FirebaseStorage.getInstance().getReference();
        drAudio = FirebaseDatabase.getInstance().getReference().child( "Upload Audio" );


//
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setBackgroundDrawable(
//                    new ColorDrawable( ContextCompat.getColor( this, R.color.purple_200 ) ) );
//        }

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions( this, PERMISSIONS )) {
            ActivityCompat.requestPermissions( this, PERMISSIONS, PERMISSION_ALL );
        }


        binding.audioRecording.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/recorded_audio" + DateFormat.getDateTimeInstance().format( new Date() ) + ".wav";
                AndroidAudioRecorder.with( MainActivity.this )
                        .setFilePath( AUDIO_FILE_PATH )
                        .setColor( ContextCompat.getColor( getApplicationContext(), R.color.pink ) )
                        .setRequestCode( REQUEST_RECORD_AUDIO )
                        .setSource( AudioSource.MIC )
                        .setChannel( AudioChannel.STEREO )
                        .setSampleRate( AudioSampleRate.HZ_48000 )
                        .setAutoStart( false )
                        .setKeepDisplayOn( true )
                        .record();
            }
        } );

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (resultCode == RESULT_OK) {

                uploadAudio();

                Toast.makeText( this, "Audio recorded successfully!", Toast.LENGTH_SHORT ).show();

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText( this, "Audio was not recorded", Toast.LENGTH_SHORT ).show();
            }
        }

    }


    public void uploadAudio() {
        Uri uri = Uri.fromFile( new File( AUDIO_FILE_PATH ) );
        StorageReference filepath = srAudio.child( "Upload Audio" ).child( uri.getLastPathSegment() );
        filepath.putFile( uri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText( getApplicationContext(), "Success", Toast.LENGTH_SHORT ).show();
            }
        } );
    }

}