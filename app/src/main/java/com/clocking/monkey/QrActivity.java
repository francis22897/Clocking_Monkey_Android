package com.clocking.monkey;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clocking.monkey.Utils;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QrActivity extends AppCompatActivity implements LocationListener {

    private CameraSource cameraSource;
    private SurfaceView cameraView;
    private Button btnClockInQR;
    LocationManager locationManager;

    AssistsBDUtils assistsBDUtils;

    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initUI();
        initScan();
        CheckPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        assistsBDUtils.resetComment();
        assistsBDUtils.checkAssitance();

        getLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        btnClockInQR.setEnabled(false);

        locationManager.removeUpdates(this);
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void CheckPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }


    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider!" + provider,
                Toast.LENGTH_SHORT).show();
    }

    private void setLocation(){
        if(longitude != 0.0 && latitude != 0.0) {
            float metros = 10;
            float[] distance = new float[1];
            Location.distanceBetween(38.094259, -3.631208, latitude, longitude, distance);
            if (distance[0] / metros < 1) {
                btnClockInQR.setEnabled(true);
                if (!btnClockInQR.isEnabled()){
                    btnClockInQR.setEnabled(true);
                }
            }
        }
    }

    private void initUI(){
        cameraView = findViewById(R.id.camera_view);
        btnClockInQR = findViewById(R.id.button_scan_qr);
        btnClockInQR.setEnabled(false);
        assistsBDUtils = new AssistsBDUtils(this, this.getLayoutInflater().inflate(R.layout.qr_activity, null), btnClockInQR);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Debes detectar el NFC para poder habilitar el botón")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        // Create the AlertDialog object and return it
        builder.create();
        builder.show();

        btnClockInQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assistsBDUtils.checkUser();
            }
        });
    }

    private void initScan(){
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();


        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        Log.e("CAMERA SOURCE", e.getMessage());
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> code = detections.getDetectedItems();

                if (code.size() > 0){
                    String codeQr = code.valueAt(0).displayValue;
                    if(codeQr.equals(Utils.QR_PASSWORD)){
                        //Log.i("PRUEBA", "OK");
                        setLocation();
                    }

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_fragments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.back){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        return true;
    }
}



