package com.rafzy.uteproject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 0;
    private static final float MIN_DISTANCE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch(Exception ex) {ex.printStackTrace();}
        if(gps_enabled) {
            LocationListener locationListener = new MapLocationListener();
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        }else{
            Toast.makeText(this,"nie włączono gps",Toast.LENGTH_SHORT).show();
        }


    }
    private class MapLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location loc) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            mMap.addMarker(new MarkerOptions().position(latLng).title("Twoja pozycja"));
            mMap.animateCamera(cameraUpdate);
            mMap.clear();
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     */
    private class JsonFromApiGetter extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MapsActivity.this, "Pobieranie informacji o teatrach w okolicy", Toast.LENGTH_LONG).show();
        }

        @Override
        protected JSONObject doInBackground(String... arg0) {
            URL apiUrlObject = null;
            JSONObject jsonObject = null;
            try {
                apiUrlObject = new URL(arg0[0]);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                apiUrlObject.openStream()));

                String inputLine = "";
                StringBuilder stringJson = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    stringJson.append(inputLine);
                }


                jsonObject = new JSONObject(stringJson.toString());

                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;

        }
        protected void onPostExecute(JSONObject result) {
            try {
                JSONArray theatreArray = result.getJSONObject("result").
                        getJSONArray("featureMemberProperties");
                List<String> theatreList = new ArrayList<>();
                for (int i = 0; i < theatreArray.length(); i++) {
                    long longtitude =  Long.parseLong(theatreArray.getJSONObject(i).getString("WWW"));
                    long latititude =  Long.parseLong(theatreArray.getJSONObject(i).getString("WWW"));

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng warsaw = new LatLng(52.2296756, 21.012228999999934);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(warsaw));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));
    }

}
