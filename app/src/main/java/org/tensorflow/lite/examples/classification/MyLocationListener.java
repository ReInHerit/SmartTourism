package org.tensorflow.lite.examples.classification;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.opencv.android.CameraActivity;
import org.tensorflow.lite.examples.classification.tflite.DatabaseAccess;

import java.util.Random;

public class MyLocationListener implements LocationListener {

    private String TAG = "MyLocationListener";

    double latitude;
    double longitude;

    public long lastNotificationTime = System.currentTimeMillis();

    private Context contex;

    public MyLocationListener(Context context) {
        this.contex = context;
    }

    public double[] getCurrentLocation() {
        return new double[]{latitude, longitude};
    }

    @Override
    public void onLocationChanged(Location location) {
        // Handle location updates here
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        // Do something with latitude and longitude values
        Log.d("Location", "[UPDATE] Latitude: " + latitude + " Longitude: " + longitude);
        sendNotification();
    }

    private void sendNotification() {
        if (DatabaseAccess.getSharedPreferences().getBoolean("pref_key_notifications", false) //Check if notifications are enabled
                && System.currentTimeMillis() - lastNotificationTime >= MainActivity.NOTIFICATION_TIME){ //At least 1 minute between notifications
            // Define the target location
            String nearestMonument = DatabaseAccess.getNearestMonument(latitude, longitude, MainActivity.MAX_DISTANCE);
            Log.d(TAG, "onLocationChanged: " + nearestMonument);

            // Check proximity and send notification if within range and enough time has passed
            if (nearestMonument != null) {
                // Update the last notification time
                lastNotificationTime = System.currentTimeMillis();

                // Create an explicit intent for the app's main activity
                Intent intent = new Intent(contex, GuideActivity.class);
                intent.putExtra("monument_id", nearestMonument);
                intent.putExtra("language", MainActivity.language);
                intent.putExtra("user_id", MainActivity.uniqueID);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(contex, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                // Create and send notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(contex, "channel_id")
                        .setSmallIcon(R.drawable.done)
                        .setContentTitle("Nearby Monument")
                        .setContentText("You are near " + nearestMonument + "! Click here to learn more.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent) // Set the pending intent
                        .setAutoCancel(true) // Dismiss the notification when the user taps on it
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(contex);
                NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);

                if (ActivityCompat.checkSelfPermission(contex, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    int notificationId = new Random().nextInt();
                    notificationManager.notify(notificationId, builder.build());
                }
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Handle provider disabled
        Log.d(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Handle provider enabled
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status changed
        Log.d(TAG, "onStatusChanged: " + provider + " status: " + status);
    }
}
