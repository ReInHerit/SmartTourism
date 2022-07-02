package org.tensorflow.lite.examples.classification.tflite;



import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


public class Retrievor {

    public static final String TAG = "Retrievor";
    private DatabaseAccess databaseAccess;

    public Retrievor(Context context){
        databaseAccess = DatabaseAccess.getInstance(context);
    }

    public ArrayList<Element> getNearest(float[] features) {

        Log.v("Retrievor","Opening DB");
        databaseAccess.open();
        ArrayList<Element> featuresFromDB = databaseAccess.getFeatureDistance(features);
        databaseAccess.close();
        Log.v("Retrievor","DB Closed");

        return  featuresFromDB;

    }
}


