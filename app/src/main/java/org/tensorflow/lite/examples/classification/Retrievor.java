package org.tensorflow.lite.examples.classification;



import android.content.Context;
import android.util.Log;

import java.util.List;


public class Retrievor {

    public static final String TAG = "Retrievor";
    private DatabaseAccess databaseAccess;

    public Retrievor(Context context){
        //Database
        databaseAccess = DatabaseAccess.getInstance(context);
        databaseAccess.open();
        Log.v("Retrievor","Database uploaded.");
        List<String> elements = databaseAccess.getElements();
        List<String> matrix = databaseAccess.getMatrix(1);
        databaseAccess.close();

        Log.v("Retrievor",elements.toString());
        Log.v("Retrievor",matrix.toString());

        /* TODO
            Compute Distance
            Search Nearest Id
         */
    }

}


