package org.tensorflow.lite.examples.classification.tflite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;
    private static ArrayList<Element> listDB = new ArrayList<>();

    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public static ArrayList<Element> getListDB() {
        return listDB;
    }

    public static float[][] getMatrixDB() {
        int n = listDB.size();
        float [][] a = new float[n][];
        for (int i = 0; i<n; i++){
            a[i] = listDB.get(i).getMatrix();
        }

        return a;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /**
     * Read all quotes from the database.
     *
     * @return a List of quotes
     */

    public void updateDatabase(int k) {
        database.isOpen();
        listDB = new ArrayList<>();
        //i<k
        for (int i = 0; i<k; i++){
            Log.v("DatabaseAccess", "id from "+i+"/"+k+" to "+(i+1)+"/"+k);
            Cursor cursor = database.rawQuery("SELECT * FROM AllInOne " +
                    "WHERE rowid > "+i+" * (SELECT COUNT(*) FROM AllInOne)/"+k+" AND rowid <= ("+i+"+1) * (SELECT COUNT(*) FROM AllInOne)/"+k, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String style = cursor.getString(0);
                String color =cursor.getString(1);
                String matrix = cursor.getString(2);

                //Convert matrix string to Float
                String[] splitted = matrix.substring(1,matrix.length() - 1).split("\\s+");
                float[] listMatrix = new float[splitted.length];

                int z = 0;
                for (String s: splitted
                ) {
                    listMatrix[z] = Float.parseFloat(s);
                    z++;
                }

                //element with converted matrix
                Element e = new Element(style,color,listMatrix,-1);
                listDB.add(e);

                cursor.moveToNext();
            }
            cursor.close();
        }


    }



}