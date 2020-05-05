package com.example.monitoringandfeedback9;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private MediaServiceConnection mediaServiceConnection = null;
    private MediaService.MediaBinder mediaBinder;
    private TextToSpeech textToSpeech;
    private Button ding, say;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ding = findViewById(R.id.ding);
        ding.setOnClickListener(view -> {
            if (mediaBinder == null) return;
            mediaBinder.play(R.raw.ding);
        });
        ding.setEnabled(false);

        final EditText sayThis = findViewById(R.id.say_this);
        say = findViewById(R.id.say);
        say.setEnabled(false);
        say.setOnClickListener(view -> {
            String toSay = sayThis.getText().toString();
            textToSpeech.speak(
                    toSay,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    UUID.randomUUID().toString());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        textToSpeech = new TextToSpeech(this, status -> {
            say.setEnabled(true);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        textToSpeech.shutdown();
        say.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaServiceConnection == null) {
            bindService(
                    new Intent(this, MediaService.class),
                    mediaServiceConnection = new MediaServiceConnection(),
                    BIND_AUTO_CREATE
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaServiceConnection != null) {
            unbindService(mediaServiceConnection);
            mediaServiceConnection = null;
        }
    }

    private final class MediaServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaBinder = (MediaService.MediaBinder) service;
            ding.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaBinder = null;
            ding.setEnabled(false);
        }
    }
}
