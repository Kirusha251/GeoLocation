package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import Cryptology.CryptographySTB;
import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.GPSTracker;
import Modules.Route;
import Modules.SQLiteHelper;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static java.security.AccessController.getContext;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {
    public static double latitude,longitude,altitude,latitude_s,longitude_s,latitude_f,longitude_f,latitude_bd,longitude_bd;
    public static float speed;
    static boolean fl=true;
    public static boolean flag=true;
    public static long time;LatLng location1,location2,location3;
    private GoogleMap mMap;       NewThread nw;
    public static String adr,adr_s,adr_f;
    private Button btnFindPath;
    static String city="г. Минск";
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);

        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });


    }

    public void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Введите точку отправления!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Введите точку назначения!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        fl=true;
    }

  public void Location(View view)
  {      gps = new GPSTracker(MapsActivity.this);
      if(gps.canGetLocation()){
          latitude = gps.getLatitude();
          longitude = gps.getLongitude();
          time=gps.getTime();
          Date moment = new Date(time);
          speed=gps.getSpeed();
          altitude=gps.getAltitude();
          try{ Geocoder geocoder;
              List<android.location.Address> addresses;
              geocoder = new Geocoder(this, Locale.getDefault());
              addresses = geocoder.getFromLocation(latitude, longitude, 1);
              String address = addresses.get(0).getAddressLine(0);
          adr=address;}
          catch (Exception IO){}
          String str = "";
          Toast.makeText(getApplicationContext(),str = latitude+"/" + longitude,Toast.LENGTH_LONG).show();
          File file = new File("coords.txt");
          FileOutputStream fop = null;
          try {
               fop = new FileOutputStream(file,false);
              if (!file.exists()){
                  file.createNewFile();
              }
              fop.write(str.getBytes());
              fop.flush();
              fop.close();
          } catch (IOException e) {
              e.printStackTrace();
          }


//          OutputStreamWriter outputStreamWriter = null;
//          try {
//              outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", getApplicationContext().MODE_ENABLE_WRITE_AHEAD_LOGGING));
//          } catch (FileNotFoundException e) {
//              e.printStackTrace();
//          }
//
//          try {
//              outputStreamWriter.write(str);
//              outputStreamWriter.close();
//          } catch (IOException e) {
//              e.printStackTrace();
//          }
          String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                  Settings.Secure.ANDROID_ID);
          RequestParams params = new RequestParams();
          params.put("device_id",android_id);
          params.put("coords", str);
          params.put("is_hash", "true");
          params.put("hashing_algorithm","stb-31");
          params.put("hash", CryptographySTB.getHash("coords.txt"));
          params.setUseJsonStreamer(true);
          AsyncHttpClient httpClient = new AsyncHttpClient();
          httpClient.post(getApplicationContext(),"http://188.166.93.46:3000/coords",params, new AsyncHttpResponseHandler() {
              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                  System.out.println("status code:" + statusCode);
              }

              @Override
              public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                  System.out.println("status code:" + statusCode);
              }
          });
          Toast.makeText(getApplicationContext(),"Hash send to server", Toast.LENGTH_LONG).show();
      }else{
          gps.showSettingsAlert();
      }
      Intent renew=new Intent(this,MapsActivity.class);
      startActivity(renew);
      this.finish();
  }

    public void Start_my_way(View view)
    {gps = new GPSTracker(MapsActivity.this);
        latitude_s = gps.getLatitude();
        fl=false;
        longitude_s = gps.getLongitude();
        try {
            Geocoder geocoder;
            List<android.location.Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude_s, longitude_s, 1);
            String address = addresses.get(0).getAddressLine(0);
            adr_s = address;

        }
        catch (Exception IO){}
        flag=true;
       nw=new NewThread();
        nw.doInBackground();
    }
    public void Finish_my_way(View view)
    {
        flag = false;

        gps = new GPSTracker(MapsActivity.this);

        latitude_f = gps.getLatitude();
        longitude_f = gps.getLongitude();
        nw.onPostExecute(gps.getLocation());
        try {
            Geocoder geocoder;
            List<android.location.Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude_f, longitude_f, 1);
            String address = addresses.get(0).getAddressLine(0);
            adr_f = address;

        }
        catch (Exception IO){}
        try {String origin = adr_s;
            String destination = adr_f;
        new DirectionFinder(this, origin, destination).execute();
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location = new LatLng(latitude,longitude);
//        mMap.setOnMarkerDragListener(this);
        mMap.addMarker(new MarkerOptions().position(location).title("My Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
         LatLng st= new LatLng(latitude_s,longitude_s);
        LatLng fn= new LatLng(latitude_f,longitude_f);
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
         if(fl==true) {
             originMarkers.add(mMap.addMarker(new MarkerOptions()
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                     .title(route.startAddress)
                     .position(route.startLocation)));
             destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                     .title(route.endAddress)
                     .position(route.endLocation)));
         }
            else
         {
             originMarkers.add(mMap.addMarker(new MarkerOptions()
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                     .title(route.startAddress)
                     .position(st)));
             destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                     .title(route.endAddress)
                     .position(fn)));
         }
            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);
           if(fl==true)
           { for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));}
            else{

                   polylineOptions.add(st);
              // polylineOptions.add(location1);
               //polylineOptions.add(location2);
               //polylineOptions.add(location3);
               polylineOptions.add(fn);
           }

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
    public class NewThread extends AsyncTask<String,Void,Location> {
        Location location;
        SQLiteHelper sqLiteHelper = new SQLiteHelper(MapsActivity.this, "Location", null, 7);
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        @Override
        protected void onPostExecute(Location result) {


        }

        @Override
        protected Location doInBackground(String... arg) {

            for(int i=0;i<3;i++) {

                gps = new GPSTracker(MapsActivity.this);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                latitude_bd = gps.getLatitude();
                longitude_bd = gps.getLongitude();
                if(i==0) {location1 = new LatLng(latitude_bd,longitude_bd);}
                else
                {
                    if(i==1){location2 = new LatLng(latitude_bd,longitude_bd);}
                    else
                    {
                        if(i==2){location3 = new LatLng(latitude_bd,longitude_bd);}
                    }
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put("lat", latitude);
                contentValues.put("longtitude", longitude);
                db.insert("Location", null, contentValues);
                Log.d("Lab_gps: ", "Latitude: " + latitude + " longtitude: " + longitude);
            }
            return location;


        }


    }
}

