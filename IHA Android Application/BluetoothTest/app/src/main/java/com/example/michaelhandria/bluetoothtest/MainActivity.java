package com.example.michaelhandria.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.List;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements BeaconConsumer, MonitorNotifier, RangeNotifier{

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    public static final String TAG = "BeaconsEverywhere";
    private BeaconManager beaconManager;
    private static  Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _context = getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

    }


    @Override
    public void didEnterRegion(Region region) {
        try {
            Log.d(TAG, "didEnterRegion");
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didExitRegion(Region region) {
        try {
            Log.d(TAG, "didExitRegion");
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for(Beacon oneBeacon : beacons) {
            //Toast.makeText(_context, Double.toString(oneBeacon.getDistance()), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getBluetoothName());
            List<Long> longList = oneBeacon.getExtraDataFields();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    beaconManager = BeaconManager.getInstanceForApplication(this);

                    beaconManager.getBeaconParsers().add(new BeaconParser()
                            .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
                    beaconManager.getBeaconParsers().add(new BeaconParser()
                            .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

                    beaconManager.getBeaconParsers().add(new BeaconParser()
                            .setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
                    beaconManager.bind(this);
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }


    @Override
    public void onBeaconServiceConnect() {
        //Identifier myBeaconNamespaceId = Identifier.parse("0x2F234454F4911BA9FFA6");
        //Identifier myBeaconInstanceId = Identifier.parse("0x000000000001");
        //final Region region = new Region("speaker 1", myBeaconNamespaceId, myBeaconInstanceId, null);
        final Region region = new Region("all-beacons-nearby", null, null, null);
        //beaconManager.addMonitorNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (Exception e) {
            e.printStackTrace();
        }
        beaconManager.addRangeNotifier(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        beaconManager.unbind(this);
    }

}

