package com.rafzy.uteproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpRequestHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends ActionBarActivity {
    private final static String APIKEY = "e5144dd5-5924-452a-a520-1f0966d71636";
    private final static String xmlResult = "<response><msisdn>48510123456</msisdn><terminal-availability>AVAILABLE</terminal-availability><terminal-page-result>OK</terminal-page-result><age-of-location>0</age-of-location><location-information><current-lac>58140</current-lac><current-cell-id>47025</current-cell-id></location-information></response>";
    private String myCellNumber;
    private Pair<Double,Double> myPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new BihapiGetter().execute("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClickShareWithFriend(View view) {
        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = telemamanger.getSimSerialNumber();

        setMyPositionAndCell(simSerialNumber);

        getAllCulturalObjectNearMe(simSerialNumber);
        //// TODO: 12/28/16 send sms to the friends which are at the same cell about what is interesting near me

        getFriendPosition();
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivity(mapIntent);
    }
    private void setMyPositionAndCell(String number){
        this.myCellNumber = getCellNumber(number);
    }

    public List<String> getAllCulturalObjectNearMe(String myNumber) {
        //// TODO: 12/28/16 get position and object near me (dane po warszawsku)
        String urlToGetPosition = "https://api.bihapi.pl/orange/oracle/cellid?msisdn=+" + myNumber;
        //// TODO: 12/28/16 parse result json
        return new ArrayList<String>();
    }
    private String getCellNumber(String number){
        Element currentCell = null;
        String urlToGetPosition = "https://api.bihapi.pl/orange/oracle/cellid?msisdn=+" + number;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlResult));
            Document doc = dBuilder.parse(is);
            currentCell = (Element) doc.getElementsByTagName("current-cell-id").item(0);
            Element lac = (Element) doc.getElementsByTagName("current-lac").item(0);
            System.out.println("Root element :" + currentCell.getTextContent() + "," +lac.getTextContent());
        } catch (ParserConfigurationException pe) {
            pe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return currentCell.getTextContent();
    }
    private String getPosition(String number){
        return "";
    }


    public List<String> getFriendPosition() {
        List<String> friendNumbers = getContact();
        //https://api.bihapi.pl/orange/oracle/cellid?msisdn=48510123456

        for (String number : friendNumbers) {
            String friendCell = getCellNumber(number);
            //// TODO: 12/28/16 find friends which allow to use bihapi and are near me
            System.out.println("friend " + friendCell + " moj " + myCellNumber);
            if(friendCell.equals(myCellNumber)){
                System.out.println("Ten sam numer ");
            }else {
                System.out.println("inny ");
            }

        }
        return friendNumbers;
    }

    private class BihapiGetter extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Json Data is downloading", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            URL apiUM  = null;
            long a = 10;
            try {
                apiUM = new URL("https://api.um.warszawa.pl/api/action/datastore_search?resource_id=0b1af81f-247d-4266-9823-693858ad5b5d&limit=5");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                apiUM.openStream()));

                String inputLine;

                while ((inputLine = in.readLine()) != null)
                    System.out.println(inputLine);

                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "sda";
        }
    }

    public List getContact() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        List<String> contactList = new ArrayList<>();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactList.add(phoneNumber);
        }
        phones.close();
        return contactList;
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
