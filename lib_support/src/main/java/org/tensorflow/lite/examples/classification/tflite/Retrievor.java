package org.tensorflow.lite.examples.classification.tflite;



import android.content.Context;
import java.util.ArrayList;


public class Retrievor {

    public static final String TAG = "Retrievor";
    private DatabaseAccess databaseAccess;

    public Retrievor(Context context){
        databaseAccess = DatabaseAccess.getInstance(context);
        databaseAccess.open();
        databaseAccess.updateDatabase();
        databaseAccess.close();
    }

    public ArrayList<Element> getNearest(float[] imgFeatures) {
        ArrayList<Element> list = new ArrayList<Element>();

        for (Element element:databaseAccess.getListDB()) {
            double distance = euclideanDistance(imgFeatures,element.getMatrix());

            Element e = new Element(element.getStyle(),element.getColor(),element.getMatrix(),distance);
            list.add(e);
        }
        return  list;

    }


    private double euclideanDistance(float[] a, ArrayList<Float> b) {
        double diff_square_sum = 0.0;
        for (int i = 0; i < b.size(); i++) {
            diff_square_sum += (a[i] - b.get(i)) * (a[i] - b.get(i));
        }
        return Math.sqrt(diff_square_sum);
    }
}


