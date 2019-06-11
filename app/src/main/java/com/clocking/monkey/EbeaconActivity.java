package com.clocking.monkey;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class EbeaconActivity extends Activity implements BeaconConsumer, RangeNotifier {

    // Para interactuar con los beacons desde una actividad
    private BeaconManager mBeaconManager;

    // Representa el criterio de campos con los que buscar beacons
    private org.altbeacon.beacon.Region mRegion;

    BluetoothAdapter mBluetoothAdapter;

    Button btnClockIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebeacon);

        //botón de fichar
        btnClockIn = findViewById(R.id.clockin_btn);
        btnClockIn.setEnabled(false);

        //instancio el manejador de beacons
        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        // Fijar un protocolo beacon, Eddystone en este caso
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        //Añado un rango de búsqueda, es decir, que buscará ebeacon que coincidan con estos identificadores

        Identifier myBeaconNamespaceId = Identifier.parse(Utils.NAMESPACE_ID);
        Identifier myBeaconInstanceId = Identifier.parse(Utils.INSTANCE_ID);

        mRegion = new org.altbeacon.beacon.Region(Utils.REGION_ID, myBeaconNamespaceId, myBeaconInstanceId, null);

        //Compruebo si hay permisos de localización

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Si los permisos de localización todavía no se han concedido, solicitarlos
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                askForLocationPermissions();

            } else { // Permisos de localización concedidos

                prepareDetection();
            }

        } else { // Versiones de Android < 6

            prepareDetection();
        }
    }

    /**
     * Activar localización y bluetooth para empezar a detectar beacons
     */
    private void prepareDetection() {

        if (!isLocationEnabled()) {

            askToTurnOnLocation();

        } else { // Localización activada, comprobemos el bluetooth

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {

                showToastMessage(getString(R.string.not_support_bluetooth_msg));

            } else if (mBluetoothAdapter.isEnabled()) {

                startDetectingBeacons();

            } else {

                // Pedir al usuario que active el bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Utils.REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Utils.REQUEST_ENABLE_BLUETOOTH) {

            // Usuario ha activado el bluetooth
            if (resultCode == RESULT_OK) {

                startDetectingBeacons();

            } else if (resultCode == RESULT_CANCELED) { // Usuario rechaza activar el bluetooth

                showToastMessage(getString(R.string.no_bluetooth_msg));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Empezar a detectar los beacons, ocultando o mostrando los botones correspondientes
     */
    private void startDetectingBeacons() {

        // Fijar un periodo de escaneo
        mBeaconManager.setForegroundScanPeriod(Utils.DEFAULT_SCAN_PERIOD_MS);

        // Enlazar al servicio de beacons. Obtiene un callback cuando esté listo para ser usado
        mBeaconManager.bind(this);

    }

    @Override
    public void onBeaconServiceConnect() {

        try {
            // Empezar a buscar los beacons que encajen con la region pasada
            mBeaconManager.startRangingBeaconsInRegion(mRegion);

            showToastMessage(getString(R.string.start_looking_for_beacons));

        } catch (RemoteException e) {
            Log.d(Utils.TAG, "Se ha producido una excepción al empezar a buscar beacons " + e.getMessage());
        }

        mBeaconManager.addRangeNotifier(this);
    }


    /**
     * Método llamado cada DEFAULT_SCAN_PERIOD_MS segundos con los beacons detectados durante ese
     * periodo
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        if (beacons.size() == 0) {
            showToastMessage(getString(R.string.no_beacons_detected));
            stopDetectingBeacons();

            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }

        } else {

            btnClockIn.setEnabled(true);

            stopDetectingBeacons();

            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        }
    }

    /**
     * Detener la búsqueda de beacons
     */

    private void stopDetectingBeacons() {

        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(mRegion);
            showToastMessage(getString(R.string.stop_looking_for_beacons));
        } catch (RemoteException e) {
            Log.d(Utils.TAG, "Se ha producido una excepción al parar de buscar beacons " + e.getMessage());
        }

        mBeaconManager.removeAllRangeNotifiers();

        // Desenlazar servicio de beacons
        mBeaconManager.unbind(this);

    }

    /**
     * Comprobar permisión de localización para Android >= M
     */
    private void askForLocationPermissions() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.location_access_needed);
        builder.setMessage(R.string.grant_location_access);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        Utils.PERMISSION_REQUEST_COARSE_LOCATION);
            }
        });
        builder.show();
    }

    /**
     * Obtiene el resultado de pedir los permisos al usuario
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            //Si ha proporcionado permisos de localizacion
            case Utils.PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareDetection();
                } else {
                    //Si no los ha aceptado
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.funcionality_limited);
                    builder.setMessage(getString(R.string.location_not_granted) +
                            getString(R.string.cannot_discover_beacons));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    /**
     * Comprobar si la localización está activada
     *
     * @return true si la localización esta activada, false en caso contrario
     */
    private boolean isLocationEnabled() {

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        boolean networkLocationEnabled = false;

        boolean gpsLocationEnabled = false;

        try {
            networkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            gpsLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch (Exception ex) {
            Log.d(Utils.TAG, "Excepción al obtener información de localización");
        }

        return networkLocationEnabled || gpsLocationEnabled;
    }

    /**
     * Abrir ajustes de localización para que el usuario pueda activar los servicios de localización
     */
    private void askToTurnOnLocation() {

        // Notificar al usuario
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.location_disabled);
        dialog.setPositiveButton(R.string.location_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.show();
    }


    /**
     * Mostrar mensaje
     *
     * @param message mensaje a enseñar
     */
    private void showToastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.unbind(this);
    }
}