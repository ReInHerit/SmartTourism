package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
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


        //NestedScrollView mainScrollView = findViewById(R.id.scrollNestedView);
        //mainScrollView.fullScroll(NestedScrollView.FOCUS_UP);
        //mainScrollView.smoothScrollTo(0,0);


        String monumentId = getIntent().getStringExtra("monument_id");
        String language = getIntent().getStringExtra("language");

        Toolbar toolbar = (Toolbar) findViewById(R.id.topAppBar);
        toolbar.setTitle(monumentId);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                stopAudio();
                finish();
            }
        });


        loadImageGuide("guides/"+monumentId+"/img.jpg");
        loadGuidefromFile("guides/"+monumentId+"/"+language+"/testo.txt");


        String pathVideo = "android.resource://" + getPackageName() + "/";
        String pathAudio = "guides/"+monumentId+"/"+language+"/audio.mp3";


        switch (monumentId){
            case "Cattedrale Duomo":
                if(language.equals("English"))
                    pathVideo += R.raw.duomo_english;
                else
                    pathVideo += R.raw.duomo_italian;
                break;
            case "Campanile Giotto":
                if(language.equals("English"))
                    pathVideo += R.raw.giotto_english;
                else
                    pathVideo += R.raw.giotto_italian;
                break;
            case "Battistero SanGiovanni":
                if(language.equals("English"))
                    pathVideo += R.raw.battistero_english;
                else
                    pathVideo += R.raw.battistero_italian;
                break;
            case "Loggia Bigallo":
                if(language.equals("English"))
                    pathVideo += R.raw.loggia_english;
                else
                    pathVideo += R.raw.loggia_italian;
                break;
            case "Palazzo Vecchio":
                if(language.equals("English"))
                    pathVideo += R.raw.palazzo_english;
                else
                    pathVideo += R.raw.palazzo_italian;
                break;
            default:
                break;
        }

        TextView textTextView = findViewById(R.id.textGuide);
        textTextView.setText(text);

        ImageView imageImageView = findViewById(R.id.imgGuide);
        imageImageView.setImageBitmap(img);

        VideoView videoView = findViewById(R.id.videoView);
        Uri uriVideo = Uri.parse(pathVideo);
        videoView.setVideoURI(uriVideo);

        MediaController mediaController = new MediaController(this);
        //mediaController.setMediaPlayer(videoView);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.seekTo(5);
        //videoView.start();

        Button playBtn = findViewById(R.id.idBtnPlay);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(pathAudio);
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

    private void playAudio(String pathAudio) {

        if (mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                AssetFileDescriptor afd = getAssets().openFd(pathAudio);
                mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
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