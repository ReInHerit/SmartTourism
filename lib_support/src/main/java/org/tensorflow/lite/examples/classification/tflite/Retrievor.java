package org.tensorflow.lite.examples.classification.tflite;



import android.content.Context;
import android.util.Log;

import java.util.ArrayList;


public class Retrievor {

    public static final String TAG = "Retrievor";
    private static final double MAX_DISTANCE = 30.55;
    private DatabaseAccess databaseAccess;

    public Retrievor(Context context){
        databaseAccess = DatabaseAccess.getInstance(context);
        databaseAccess.open();
        databaseAccess.updateDatabase(5);
        databaseAccess.close();
    }

    public ArrayList<Element> getNearest(float[] imgFeatures) {
        ArrayList<Element> list = new ArrayList<Element>();

        for (Element element:databaseAccess.getListDB()) {
            double distance = euclideanDistance(imgFeatures,element.getMatrix());

            if(distance < MAX_DISTANCE){
                Element e = new Element(element.getStyle(),element.getColor(),element.getMatrix(),distance);
                list.add(e);
            }
        }
        return  list;

    }


    private double euclideanDistance(float[] x, float[] y) {
        return Math.sqrt(dot(x, x) - 2 * dot(x, y) + dot(y, y));
    }

    private double dot(float[] xlist, float[] ylist) {
        double result = 0.0;
        int size = Math.min(xlist.length, ylist.length);

        for (int i = 0; i < size; i++)
            result += xlist[i] * ylist[i];

        return result;
    }
}


