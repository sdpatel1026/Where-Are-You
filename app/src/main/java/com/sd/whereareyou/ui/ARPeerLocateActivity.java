package com.sd.whereareyou.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.sd.whereareyou.R;
import com.sd.whereareyou.utils.PermissionHelper;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ARPeerLocateActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = ARPeerLocateActivity.class.getSimpleName();
    private static final String SELF = "self";
    private static final String TARGET = "target";
    private static SensorManager sensorManager;
    private SurfaceView surfaceView;
    private TextView tvDirection;
    private ImageView ivCompass;
    private BubbleTextView tvBubble;
    private TextView tvDistance;
    private TextView tvTargetDirection;
    private CameraSource cameraSource;
    private Sensor sensor;
    private LocationManager locationManager;
    private float curDegree = 0f;
    private float curDegreeForN = 0f;
    private double myLat;
    private double myLong;
    private Double targetLat = 22.2487;
    private Double targetLong = 70.7897;
    private Location myLocation;
    private Location targetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_peer_locate);
        PermissionHelper.requestLocationPermission(this);
        intialiseUIElements();
        setUPCamera();

        // Get the target device's location
     /*   Intent intent = getIntent();
        String lat = intent.getStringExtra(Constants.TARGET_LAT);
        String lon = intent.getStringExtra(Constants.TARGET_LONG);
        String username = intent.getStringExtra(Constants.FRIEND_USER_NAME);
        targetLat = Double.valueOf(lat);
        targetLong = Double.valueOf(lon);*/
        myLocation = new Location(SELF);
        targetLocation = new Location(TARGET);
        targetLocation.setLatitude(targetLat);
        targetLocation.setLongitude(targetLong);
        setUpLocationManager();
        setUpOrientationSensor();

    }

    private void setUpOrientationSensor() {

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }


    private void setUpLocationManager() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLat = location.getLatitude();
                myLong = location.getLongitude();
                myLocation = location;
                tvDistance.setText(calculateDistance());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                tvBubble.setVisibility(View.GONE);
                Toast.makeText(ARPeerLocateActivity.this, getString(R.string.location_provide_disabled), Toast.LENGTH_SHORT).show();

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionHelper.enableLocationPermission(this);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

    }

    private void setUpSurfaceView(TextRecognizer textRecognizer) {

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    PermissionHelper.requestCameraPermission(ARPeerLocateActivity.this);
                    return;
                }
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    Log.d(TAG, "surfaceCreated: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

            }
        });

    }

    private void setUPCamera() {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (textRecognizer.isOperational()) {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1920, 1080)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            setUpSurfaceView(textRecognizer);

        } else {
            Toast.makeText(this, "camera not start", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "setUPCamera: textRecognizer is not operational");
        }
    }

    private void intialiseUIElements() {


        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        tvDirection = findViewById(R.id.tvDirection);
        tvBubble = findViewById(R.id.tvBubble);
        ivCompass = findViewById(R.id.ivCompass);
        tvDistance = findViewById(R.id.tvDistance);
        tvTargetDirection = findViewById(R.id.tvTargetDirection);

    }


    public void OnRequestPermissionsResultCallback(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {

        if (requestCode == PermissionHelper.CAMERA_REQUEST_CODE && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                finish();
                return;
            } else {
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    Log.d(TAG, "OnRequestPermissionsResultCallback(): " + e.getMessage());
                    e.printStackTrace();
                }
            }


        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionHelper.enableLocationPermission(this);
        }


    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }


    // Calculates the distance using the Haversine function
    private String calculateDistance() {
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        myLat = Math.toRadians(myLat);
        myLong = Math.toRadians(myLong);
        targetLat = Math.toRadians(targetLat);
        targetLong = Math.toRadians(targetLong);

        // Haversine formula
        double dlon = targetLong - myLong;
        double dlat = targetLat - myLat;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(myLat) * Math.cos(targetLat)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        double distance = c * r;
        return String.format("%.2f", distance) + getString(R.string.kms);

    }


    // Calculates the distance using the Haversine function
    /*private String calculateDistance() {
        double diffLat = (myLat - targetLat) * (Math.PI / 180);
        double diffLong = (myLong - targetLat) * (Math.PI / 180);
        double a =
                Math.sin(diffLat / 2) * Math.sin(diffLat / 2)
                        + Math.cos(targetLat * (Math.PI / 180)) * Math.cos(myLat * (Math.PI / 180))
                        * Math.sin(diffLong / 2) * Math.sin(diffLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6371 * c * 1000;
        return String.format("%.2f", d) + getString(R.string.meteres);

    }
*/
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        // Get the angle between my device and north
        float degree = Math.round(sensorEvent.values[0]);
        float degreeForN = degree;

        // Calculate the angle between my device to the target device
        if (myLocation != null) {
            float bearing = myLocation.bearingTo(targetLocation);
            GeomagneticField geomagneticField =
                    new GeomagneticField(Double.valueOf(myLat).floatValue(),
                            Double.valueOf(myLong).floatValue(),
                            Double.valueOf(myLocation.getAltitude()).floatValue(),
                            System.currentTimeMillis());
            degree -= geomagneticField.getDeclination();
            if (bearing < 0) {
                bearing += 360;
            }
            degree = bearing - degree;
            if (degree < 0) {
                degree += 360;
            }
        }

        // If my device is pointing towards the target device, we have a indicator on camera
        // indicate the target device location
        if (degree <= 20 || degree >= 340) {
            tvBubble.setVisibility(View.VISIBLE);
            tvBubble.setY(400);
            if (degree > 0 && degree < 20) {
                tvBubble.setX((float) (103 * degree + 1940) / 4);
            }
            if (degree >= 340 && degree <= 360) {
                tvBubble.setX((float) (117 * degree - 40180) / 4);
            }
        } else {
            // If my device is not pointing towards the target device
            // we don't show the indicator
            tvBubble.setVisibility(View.GONE);
        }

       /* // Perform the arrow animation
        targetDirection.setText("Heading " + Float.toString(degree));
        RotateAnimation rotateAnimation = new RotateAnimation(curDegree, degree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);
        arrow.startAnimation(rotateAnimation);
*/
        RotateAnimation rotateAnimationCompass = new RotateAnimation(curDegreeForN, (float) -degreeForN,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimationCompass.setDuration(1000);
        rotateAnimationCompass.setFillAfter(true);
        ivCompass.startAnimation(rotateAnimationCompass);

        // Update the angle in degree
        curDegree = degree;
        curDegreeForN = -degreeForN;


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}