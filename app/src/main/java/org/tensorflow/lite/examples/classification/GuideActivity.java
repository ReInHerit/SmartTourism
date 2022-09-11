package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GuideActivity extends AppCompatActivity {

    String TAG = "GuideActivity";

    String text;
    Bitmap img;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        ScrollView mainScrollView = (ScrollView)findViewById(R.id.idScrollView);
        mainScrollView.fullScroll(ScrollView.FOCUS_UP);
        mainScrollView.smoothScrollTo(0,0);

        String monumentId = getIntent().getStringExtra("monument_id");
        String language = getIntent().getStringExtra("language");

        Log.v(TAG,"Language: "+language);

        loadGuidefromFile("guides/"+monumentId+"/testo.txt");
        loadImageGuide("guides/"+monumentId+"/img.jpg");

        String path = "android.resource://" + getPackageName() + "/";

        switch (monumentId){
            case "Cattedrale Duomo":
                path += R.raw.duomo;
                break;
            case "Campanile Giotto":
                path += R.raw.duomo;
                break;
            case "Battistero SanGiovanni":
                path += R.raw.duomo;
                break;
            case "Loggia Bigallo":
                path += R.raw.duomo;
                break;
            case "Palazzo Vecchio":
                path += R.raw.duomo;
                break;
            default:
                break;
        }


        TextView titleTextView = findViewById(R.id.titleGuide);
        titleTextView.setText(monumentId);

        TextView textTextView = findViewById(R.id.textGuide);
        textTextView.setText(text);

        ImageView imageImageView = findViewById(R.id.imgGuide);
        imageImageView.setImageBitmap(img);

        VideoView videoView = findViewById(R.id.videoView);
        Uri uri = Uri.parse(path);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
        videoView.seekTo(1);
        //videoView.start();

        Button playBtn = findViewById(R.id.idBtnPlay);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling method to play audio.
                playAudio("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
            }
        });
    }

    private void loadGuidefromFile(String fileName){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(fileName)));

            // do reading, usually loop until end of file reading
            String mLine;
            //while ((mLine = reader.readLine()) != null) {
                //process line
            //    Log.v(TAG, mLine);
            //}

            text = reader.readLine();
        } catch (IOException e) {
            //log the exception
            e.printStackTrace();
            text = "Error: Text guide not found!";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    private void loadImageGuide(String fileName){
        AssetManager am = getAssets();
        InputStream is = null;
        try{
            is = am.open(fileName);
        }catch(IOException e){
            e.printStackTrace();
        }

        img = BitmapFactory.decodeStream(is);
    }

    private void playAudio(String audioUrl) {

        if (mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(audioUrl);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(GuideActivity.this, "Audio has been paused", Toast.LENGTH_SHORT).show();
        } else {
            mediaPlayer.start();
            Toast.makeText(GuideActivity.this, "Audio started playing..", Toast.LENGTH_SHORT).show();
        }

    }

    private void stopAudio(){
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer=null;
    }
}