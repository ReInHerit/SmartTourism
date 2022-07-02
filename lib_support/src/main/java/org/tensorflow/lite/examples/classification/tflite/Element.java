package org.tensorflow.lite.examples.classification.tflite;

public class Element {
    private String style;
    private  String color;
    private double distance;


    public Element (String style, String color, double distance){
        this.color = color;
        this.style = style;
        this.distance = distance;
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
}
