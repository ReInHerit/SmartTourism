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

        System.loadLibrary("faiss");
    }

    public ArrayList<Element> getNearest(float[] imgFeatures, int k) {
        ArrayList<Element> list = new ArrayList<Element>();

        for (Element element: DatabaseAccess.getListDB()) {
            double distance = euclideanDistance(imgFeatures,element.getMatrix());

            if(distance < MAX_DISTANCE){
                Element e = new Element(element.getStyle(),element.getColor(),element.getMatrix(),distance);
                list.add(e);
            }
        }
        return  list;

    }

    public ArrayList<Element> faissSearch(float[] imgFeatures, int k) {
        ArrayList<Element> resultList = new ArrayList<Element>();
        String result = stringFromJNI(imgFeatures, DatabaseAccess.getMatrixDB(),k);

        String[] splitted = result.split("\\s+");

        ArrayList<Element> DbList = DatabaseAccess.getListDB();

        for (int z=0; z<k*2; z=z+2) {
            int index = Integer.parseInt(splitted[z]);
            double squaredDistance = Double.parseDouble(splitted[z+1]);

            Element oldElement = DbList.get(index);
            Element e = new Element(oldElement.getStyle(),oldElement.getColor(), oldElement.getMatrix(), Math.sqrt(squaredDistance));

            resultList.add(e);
        }

        return resultList;
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


    public static native String stringFromJNI(float[] imgFeatures,float[][] data,int k);
}


