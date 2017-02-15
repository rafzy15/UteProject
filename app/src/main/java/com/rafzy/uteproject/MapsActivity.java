package com.rafzy.uteproject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    LocationListener locationListener;
    private static String TAG = "ACTIVITY_MAP";

    private JsonFromApiGetter umApiGetter;
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (locationManager != null) {
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception ex) {
                    Log.i("T", "fail to remove location listners, ignore", ex);
                }

        }
    }
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
        umApiGetter = new JsonFromApiGetter();
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Twoja pozycja").
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            allCulturalObjectNearMe(loc.getLatitude(), loc.getLongitude());
            mMap.animateCamera(cameraUpdate);

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
    public void allCulturalObjectNearMe(double latitude,double longitude) {
        String apiID = "e26218cb-61ec-4ccb-81cc-fd19a6fee0f8";
        Log.i(TAG, "latitude" + latitude
                + "long " + longitude);
        String urlToGetCulturalObject = Helper.getConfigValue(this, "um_url") + "wfsstore_get/?id=" +
                apiID + "&circle=" + longitude +
                "%2C" + latitude + "%2C1000&" +
                "apikey=" + Helper.getConfigValue(this, "api_key");

        Log.i(TAG, urlToGetCulturalObject);
        JsonFromApiGetter umApiGetter = new JsonFromApiGetter();
        umApiGetter.execute(urlToGetCulturalObject);
    }
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
                        getJSONArray("featureMemberCoordinates");
                JSONArray theatreProperties = result.getJSONObject("result").
                        getJSONArray("featureMemberProperties");
                List<String> theatreList = new ArrayList<>();
                for (int i = 0; i < theatreArray.length(); i++) {
                    Log.i(TAG, " " + theatreArray.get(i));
                     String theatreName = theatreProperties.getJSONObject(i).getString("OPIS");
                    double longtitude = Double.parseDouble(theatreArray.getJSONObject(i).getString("longitude"));
                    double latitude = Double.parseDouble(theatreArray.getJSONObject(i).getString("latitude"));
                    LatLng theatre = new LatLng(latitude,longtitude);
                    Log.i(TAG," " + theatre);
                    mMap.addMarker(new MarkerOptions().position(theatre).title(theatreName).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

}
