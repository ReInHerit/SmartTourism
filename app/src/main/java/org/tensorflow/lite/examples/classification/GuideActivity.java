package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GuideActivity extends AppCompatActivity {

    String TAG = "GuideActivity";

    String text;
    Bitmap img;
    String audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        String monumentId = getIntent().getStringExtra("monument_id");
        String language = getIntent().getStringExtra("language");

        Log.v(TAG,"Language: "+language);

        loadGuidefromFile("guides/"+monumentId+"/testo.txt");
        loadImageGuide("guides/"+monumentId+"/img.jpg");

        TextView titleTextView = findViewById(R.id.titleGuide);
        titleTextView.setText(monumentId);

        TextView textTextView = findViewById(R.id.textGuide);
        textTextView.setText(text);

        ImageView imageImageView = findViewById(R.id.imgGuide);
        imageImageView.setImageBitmap(img);
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
}