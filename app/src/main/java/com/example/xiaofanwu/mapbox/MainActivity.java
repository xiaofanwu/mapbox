package com.example.xiaofanwu.mapbox;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.content.DialogInterface;
import android.app.AlertDialog;
import 	android.os.Build;
import 	java.util.ArrayList;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import android.graphics.Color;
import android.widget.Toast;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;


import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DirectionsActivity";

    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    public Position position;
    public Position position1;
    private Location lastLocation;
    TextView textView;
    Button startButton, sendButton, clearButton, stopButton;
    private LatLng[] manLocation;
    private DirectionsRoute allDirectInfo;
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    private FloatingActionButton floatingActionButton;
    private LocationServices locationServices;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private static final int PERMISSIONS_LOCATION = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView = (TextView) findViewById(R.id.textViewId);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);




        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        MapboxAccountManager.start(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_mas_directions);
//        askForPermission();


        locationServices = LocationServices.getLocationServices(MainActivity.this);
            Log.d(TAG,"permission not granted");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);

//        else{
//            Log.d(TAG,"location is alreay enabled");
//            enableLocation(true);
//        }



        // Set up autocomplete widget
        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.query);
        autocomplete.setAccessToken(MapboxAccountManager.getInstance().getAccessToken());
        autocomplete.setType(GeocodingCriteria.TYPE_POI);
        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick (CarmenFeature feature) {
                Log.d(TAG,"came to here at all??");
                position = feature.asPosition();
                Log.d(TAG,"p1 ******" + position.getLatitude()+ " , " + position.getLongitude());

            }

        });

        GeocoderAutoCompleteView autocomplete1 = (GeocoderAutoCompleteView) findViewById(R.id.query1);
        autocomplete1.setAccessToken(MapboxAccountManager.getInstance().getAccessToken());
        autocomplete1.setType(GeocodingCriteria.TYPE_POI);
        autocomplete1.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick (CarmenFeature feature) {
                position1 = feature.asPosition();
                Log.d(TAG,"p22 ******" + position1.getLatitude()+ " , " + position1.getLongitude());
            }

        });


        // Setup the MapView
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });

    }




    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                Log.d(TAG,data + "came to usbreadcallback");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    Toast.makeText(
                            MainActivity.this,
                            "granted broadcast here yayay",
                            Toast.LENGTH_SHORT).show();

                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.



                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            Log.d(TAG, "Serial Connection Opened!\n");
                            Toast.makeText(
                                    MainActivity.this,
                                    "Port is opened *******",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Log.d(TAG,"came to able to start ");
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);

            }
        }

        ;
    };

    private void getRoute(Position origin, Position destination) throws ServicesException {
        Log.d(TAG,"came to get route");

        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_WALKING)
                .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
                .setSteps(true)
                .setOverview("full")
                .build();

        Log.d(TAG,"came to before response");

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }

                // Print some info about the route
                Log.d(TAG, "responde string*****" + response.toString());
                Log.d(TAG, "raw response " + response.raw());
                Log.d(TAG, "tostring  " + response.raw().toString());
                Log.d(TAG, "getroute" + response.body().getRoutes().toString());
                Log.d(TAG, "wayypoints " + response.body().getWaypoints().toString());
                Log.d(TAG, "respose ***********" + response.body());
                Log.d(TAG, "test*******" + response.body().getRoutes().toArray().toString());
                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "geometry############" + currentRoute.getGeometry());
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(
                        MainActivity.this,
                        "Route is " + currentRoute.getDistance() + " meters long.",
                        Toast.LENGTH_SHORT).show();

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double computeDistance(LatLng from, LatLng to) {
        Log.d(TAG,"came to compute distance");

        Toast.makeText(MainActivity.this, "distance lat and lng are :" +to.getLatitude() + ", " + from.getLatitude() + "lng " + to.getLongitude() + " ," +from.getLongitude()  , Toast.LENGTH_SHORT).show();

        double dLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double dLon = Math.toRadians(to.getLongitude() - from.getLongitude());
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());

        double a = Math.pow(Math.sin(dLat/2), 2) + Math.pow(Math.sin(dLon/2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double R = 3960;

        double returnDistance = R * c;
        return returnDistance;
    }



    private void drawRoute(DirectionsRoute route) {

//        Log.d(TAG, "getlegs" + route.getLegs().get(0).getSteps().get(0).getManeuver().getInstruction());
        // Convert LineString coordinates into LatLng[]
        Log.d(TAG, "size" + route.getLegs().get(0).getSteps().size());
        int sizeOfSteps = route.getLegs().get(0).getSteps().size();


        manLocation = new LatLng[sizeOfSteps];
        allDirectInfo = route;
        for (int i = 0; i <sizeOfSteps;i++){
            Log.d(TAG, i + "location lat lng " + route.getLegs().get(0).getSteps().get(i).getManeuver().getLocation());
            Log.d(TAG, i + " " + route.getLegs().get(0).getSteps().get(i).getManeuver().getModifier());
            Log.d(TAG, i + " " + route.getLegs().get(0).getSteps().get(i).getManeuver().getLocation()[0]);
            Log.d(TAG, i + " " + route.getLegs().get(0).getSteps().get(i).getManeuver().getLocation()[1]);
            Log.d(TAG, i + " " + route.getLegs().get(0).getSteps().get(i).getManeuver().getType());
            Log.d(TAG, i + " " + route.getLegs().get(0).getSteps().get(i).getManeuver().getInstruction());
            manLocation[i] = new LatLng(
                    route.getLegs().get(0).getSteps().get(i).getManeuver().getLocation()[1],
                    route.getLegs().get(0).getSteps().get(i).getManeuver().getLocation()[0]);


        }
        Log.d(TAG,manLocation.toString());

        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void selfDestruct(View view) {


        Log.d(TAG, "never came here?????????");
        Log.d(TAG, "p1came to here ******" + position.getLatitude() + " , " + position.getLongitude());
        Log.d(TAG,"p2came to here ******" + position1.getLatitude()+ " , " + position1.getLongitude());

        try {
            getRoute(position, position1);
        } catch (ServicesException servicesException) {
            servicesException.printStackTrace();
        }
        // Add origin and destination to the map
        map.addMarker(new MarkerOptions()
                .position(new LatLng(position.getLatitude(), position.getLongitude()))
                .title("Origin")
                .snippet("Alhambra"));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(position1.getLatitude(), position1.getLongitude()))
                .title("Destination")
                .snippet("Plaza del Triunfo"));


        HashMap usbDevices = usbManager.getDeviceList();

        Toast.makeText(
                MainActivity.this,
                "USB DECVICE IS empty" +usbDevices.isEmpty() ,
                Toast.LENGTH_SHORT).show();


        if (!usbDevices.isEmpty()) {
            Log.d(TAG, "came to usbdevice is not empty");
            boolean keep = true;
            Toast.makeText(
                    MainActivity.this,
                    "USB DECVICE IS Detected",
                    Toast.LENGTH_SHORT).show();

            for (Object key : usbDevices.keySet()) {
                device = (UsbDevice) usbDevices.get(key);
                Log.d(TAG,"came to the for loop");
                int deviceVID = device.getVendorId();

                Toast.makeText(
                        MainActivity.this,
                        "Device Id is" + deviceVID,
                        Toast.LENGTH_SHORT).show();

                if (deviceVID == 1027)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }

        // Kabloey
    }

    public void onClickStart(View view) {

        HashMap usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            Log.d(TAG, "came to usbdevice is not empty");
            boolean keep = true;

            for (Object key : usbDevices.keySet()) {
                device = (UsbDevice) usbDevices.get(key);
                Log.d(TAG, "came to the for loop");
                int deviceVID = device.getVendorId();
                if (deviceVID == 1027)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }
    public void onClickSend(View view) {
        String string = "sharp left";
        serialPort.write(string.getBytes());
        Toast.makeText(
                MainActivity.this,
                "Data sent *******" + string,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG,"Data Sent : " + "string" + "\n");

    }
    public void left(View view) {
        String string = "left";
        serialPort.write(string.getBytes());
        Toast.makeText(
                MainActivity.this,
                "Data sent *******" + string,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG,"Data Sent : " + "string" + "\n");

    }

    public void slightLeft(View view) {
        String string = "slight left";
        serialPort.write(string.getBytes());
        Toast.makeText(
                MainActivity.this,
                "Data sent *******" + string,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG,"Data Sent : " + "string" + "\n");

    }

    public void arrived(View view) {
        String string = "arrived";
        serialPort.write(string.getBytes());
        Toast.makeText(
                MainActivity.this,
                "Data sent *******" + string,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Data Sent : " + "string" + "\n");
    }
    public void approaching(View view) {
        String string = "approaching";
        serialPort.write(string.getBytes());
        Toast.makeText(
                MainActivity.this,
                "Data sent *******" + string,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Data Sent : " + "string" + "\n");
    }
    public void complete(View view) {
        String string = "action complete";
        serialPort.write(string.getBytes());
        Toast.makeText(
                MainActivity.this,
                "Data sent *******" + string,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Data Sent : " + "string" + "\n");
    }

    public void wrongWay(View view) {
        String string = "wrong";
        serialPort.write(string.getBytes());
        Toast.makeText(
                MainActivity.this,
                "Data sent *******" + string,
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Data Sent : " + "string" + "\n");
    }
    public void onClickStop(View view) {
        serialPort.close();
        Log.d(TAG, "Serial Connection Closed");

    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
            }
        }
    }
    private void enableLocation(boolean enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            Location lastLocation = locationServices.getLastLocation();
            if (lastLocation != null) {
                Log.d(TAG,"last location not null");
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
            }

            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        map.setMyLocationEnabled(true);
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        if (manLocation!=null && manLocation.length != 0 ){
                            Log.d(TAG,"came here in location changed??");
                            LatLng nextAction = new LatLng(
                                    allDirectInfo.getLegs().get(0).getSteps().get(0).getManeuver().getLocation()[1],
                                    allDirectInfo.getLegs().get(0).getSteps().get(0).getManeuver().getLocation()[0]);
                            double threshold = computeDistance(new LatLng(location), nextAction);

                            Toast.makeText(MainActivity.this, "location changed ********"  + String.valueOf(threshold), Toast.LENGTH_SHORT).show();




                            if (threshold < 0.0189394) {
                                Log.d(TAG,"direction changed");
                                //can go on to next Step
                                //remove what is already in the first step, then display the next step
                                Toast.makeText(MainActivity.this, allDirectInfo.getLegs().get(0).getSteps().get(0).getManeuver().getInstruction(), Toast.LENGTH_SHORT).show();



                                String type = allDirectInfo.getLegs().get(0).getSteps().get(0).getManeuver().getType();
                                String modifier = allDirectInfo.getLegs().get(0).getSteps().get(0).getManeuver().getModifier();
                                String instruction = allDirectInfo.getLegs().get(0).getSteps().get(0).getManeuver().getInstruction();
                                Toast.makeText(MainActivity.this, "Route is " + type + modifier, Toast.LENGTH_SHORT).show();
                                if (type == "turn"){

                                    //here is where we should do all the actions right vibration
                                    //modifier are right, left, slight left, sharp right, etc
//                                    serialPort.write(modifier.getBytes());
                                }
                            }
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                            allDirectInfo.getLegs().get(0).getSteps().remove(0);

                        }

                    }


                }
            });

        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }



}

