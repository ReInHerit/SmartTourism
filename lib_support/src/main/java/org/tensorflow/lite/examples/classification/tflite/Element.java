package org.tensorflow.lite.examples.classification.tflite;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Element {
    private final String style;
    private final String color;
    private final double distance;
    private final float[] matrix;


    public Element (String style, String color,float[] matrix, double distance){
        this.color = color;
        this.style = style;
        this.distance = distance;
        this.matrix = matrix;
    }

    public double getDistance() {
        return distance;
    }

    public String getColor() {
        return color;
    }

    public String getStyle() {
        return style;
    }

    public float[] getMatrix(){ return matrix;}

    @NonNull
    @Override
    public String toString() {
        return "Element{" +
                "style='" + style + '\'' +
                ", color='" + color + '\'' +
                ", distance=" + distance +
                '}';
    }
}
