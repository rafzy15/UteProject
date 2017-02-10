package com.rafzy.uteproject;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CulturalObjectActivity extends ActionBarActivity {

    private static final String TAG = "Cultural_Activity";
    private static final String EXPANDABLE_LIST_KEY = "TEATRY BLISKO CIEBIE";
//    private final static String xmlResult = "<response><msisdn>48510123456</msisdn><terminal-availability>AVAILABLE</terminal-availability><terminal-page-result>OK</terminal-page-result><age-of-location>0</age-of-location><location-information><current-lac>58140</current-lac><current-cell-id>47025</current-cell-id></location-information></response>";

    private LocationManager locationManager;

    private ExpandableListView expandableListView;
    private CulturalObjectListAdapter culturalObjectListAdapter;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerToLocationManager();
        expandableListView = (ExpandableListView) findViewById(R.id.ExpandableList);
        expandableListDetail = new HashMap<>();

        expandableListDetail.put(EXPANDABLE_LIST_KEY, new ArrayList<String>());
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        culturalObjectListAdapter = new CulturalObjectListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(culturalObjectListAdapter);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int grpPos) {
                getAllCulturalObjectNearMe("");
            }


        });


        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v,
                                        int groupPosition,
                                        int childPosition,
                                        long id) {
                Intent intent = new Intent(CulturalObjectActivity.this, FriendActivity.class);
                startActivity(intent);
                return false;
            }


        });

    }

    private void addListenerToLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                System.out.println("location" + location.getLatitude() + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void getAllCulturalObjectNearMe(String myNumber) {
        String apiID = "e26218cb-61ec-4ccb-81cc-fd19a6fee0f8";
        double latitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
        double longitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        Log.i(TAG, "latitude" + latitude
                + "long " + longitude);
        String urlToGetCulturalObject = Helper.getConfigValue(this, "um_url") + "wfsstore_get/?id=" +
                apiID + "&circle=" + longitude +
                "%2C" + latitude + "%2C700&" +
                "apikey=" + Helper.getConfigValue(this, "api_key");

        Log.i(TAG, urlToGetCulturalObject);
        JsonFromApiGetter umApiGetter = new JsonFromApiGetter();
        umApiGetter.execute(urlToGetCulturalObject);
    }

    private class JsonFromApiGetter extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(CulturalObjectActivity.this, "Pobieranie informacji o teatrach w okolicy", Toast.LENGTH_LONG).show();
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
                    String theatreUrl = theatreArray.getJSONObject(i).getString("WWW");
                    String theatreName = theatreArray.getJSONObject(i).getString("OPIS");
                    String street = theatreArray.getJSONObject(i).getString("ULICA");
                    String number = theatreArray.getJSONObject(i).getString("NUMER");
                    theatreList.add(theatreName + "\n" + theatreUrl + "\n" + street + " " + number);
                }
                expandableListDetail.put(EXPANDABLE_LIST_KEY, theatreList);
                expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                Log.i(TAG," " +theatreList);
                culturalObjectListAdapter.setExpandableLists(expandableListDetail, expandableListTitle);
                culturalObjectListAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
