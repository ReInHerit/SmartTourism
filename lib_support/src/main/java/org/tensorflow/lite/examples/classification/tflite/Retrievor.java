package org.tensorflow.lite.examples.classification.tflite;



import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.PriorityQueue;


public class Retrievor {

    public static final String TAG = "Retrievor";
    private DatabaseAccess databaseAccess;

    public Retrievor(Context context){
        //Database
        databaseAccess = DatabaseAccess.getInstance(context);
        //databaseAccess.open();
        //Log.v("Retrievor","Database uploaded.");
        //List<String> elements = databaseAccess.getElements();
        //List<String> matrix = databaseAccess.getMatrix(1);
        //databaseAccess.close();

        //Log.v("Retrievor",elements.toString());
        //Log.v("Retrievor",matrix.toString());


    }

    public PriorityQueue getNearest(float[] features) {
        PriorityQueue pq = new PriorityQueue<Classifier.Recognition>();
        /* TODO
            Compute Distance
            Search Nearest Id
         */
        Log.v("Retrievor","Opening DB");
        databaseAccess.open();
        Log.v("Retrievor","DB Opened");
        List<String> featuresFromDB = databaseAccess.getFeatures();
        Log.v("Retrievor","Closing DB");
        databaseAccess.close();
        Log.v("Retrievor","DB Closed");

        for (String x:featuresFromDB) {
            Log.v("Retrievor",x);
        }

        return pq;
    }
}


