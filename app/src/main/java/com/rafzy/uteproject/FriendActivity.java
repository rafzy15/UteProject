package com.rafzy.uteproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by root on 2/8/17.
 */
public class FriendActivity extends ActionBarActivity {
    private ExpandableListView expandableListView;
    private FriendListAdapter friendListAdapter;
    private List<String> expandableListTitle;
    private HashMap<String, List<FriendObject>> expandableListDetail;
    private List<String> checkedNumbers;
    private final static String FRIEND_TAG = "Friend_tag";

    private String longtitude = "";
    private final static int PICK_CONTACT = 1;

    private FriendsDatabaseHelper friendsDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.friend_layout);
        List<FriendObject> friendsList = getFriendsFromDatabase();
        expandableListView = (ExpandableListView) findViewById(R.id.friendExpendableList);
        expandableListDetail = new HashMap<>();

        expandableListDetail.put("ZNAJOMI", friendsList);

        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        friendListAdapter = new FriendListAdapter(this, expandableListTitle, expandableListDetail);

        expandableListView.setAdapter(friendListAdapter);
        expandableListView.expandGroup(0);
        checkedNumbers = new LinkedList<>();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v,
                                        int groupPosition,
                                        int childPosition,
                                        long id) {
                CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox_item);
                cb = (CheckBox) v.findViewById(R.id.checkbox_item);

                cb.setChecked(!cb.isChecked());
                TextView friendNumber = (TextView) v.findViewById(R.id.expandedFriendNumber);
                if (cb.isChecked()) {
                    checkedNumbers.add((String) friendNumber.getText());
                } else {
                    checkedNumbers.remove(friendNumber.getText());
                }
                return true;
            }
        });
    }

    private List<FriendObject> getFriendsFromDatabase() {
        friendsDatabaseHelper = new FriendsDatabaseHelper(this);
        SQLiteDatabase db = friendsDatabaseHelper.getReadableDatabase();
        Cursor cursor =
                db.query("FRIENDS", new String[]{"NAME", "NUMBER"}, null, null, null, null, null);
        List<FriendObject> friendsList = new LinkedList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(
                    0);
            String number = cursor.getString(
                    1);
            FriendObject friendObject = new FriendObject(name, number);
            friendsList.add(friendObject);

        }
        cursor.close();
        return friendsList;
    }

    public void onClickAddFriend(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);

    }

    public void onClickShowMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {

                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            String nameContact = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                            SQLiteDatabase db = friendsDatabaseHelper.getWritableDatabase();
                            friendsDatabaseHelper.insertFriend(db, nameContact, cNumber);

                            List<FriendObject> friendsList = getFriendsFromDatabase();
                            expandableListDetail.put("ZNAJOMI", friendsList);
                            friendListAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getApplicationContext(), "Wybrany użytkownik nie ma numeru", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }


    private final static String xmlResult = "<response><msisdn>48510123456</msisdn><terminal-availability>AVAILABLE</terminal-availability><terminal-page-result>OK</terminal-page-result><age-of-location>0</age-of-location><location-information><current-lac>58140</current-lac><current-cell-id>47025</current-cell-id></location-information></response>";

    private String myCellNumber;
    private Pair<Double, Double> myPosition;

    private String getCellNumber(String number) {
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
            System.out.println("Root element :" + currentCell.getTextContent() + "," + lac.getTextContent());
        } catch (ParserConfigurationException pe) {
            pe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return currentCell.getTextContent();
    }


    public void onClickShareWithFriend(View view) {
        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = telemamanger.getSimSerialNumber();

        SMSSender umApiGetter = new SMSSender();
        umApiGetter.execute(simSerialNumber);
//        getFriendPosition();
//
    }

    public class SMSSender extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(FriendActivity.this, "Wysyłanie sms", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            StringBuilder result = new StringBuilder();;
            try {
                URL apiUrlObject = null;
                List<String> numberFriends = checkedNumbers;
                String urlToSend = "";
//                String senderNumber = arg0[0];
                String senderNumber = "517567255";
                for (String number : numberFriends) {
                    System.out.println(number);
                    if (senderNumber.length() > 11) {
                        senderNumber = senderNumber.substring(senderNumber.length() - 11);
                    }else if(senderNumber.length() == 9){
                        senderNumber = "48" + senderNumber;
                    }
                    if (number.length() > 11) {
                        number = number.substring(number.length() - 11);
                    }else if(number.length() == 9){
                        number = "48" + number;
                    }
                    urlToSend = "https://apitest.orange.pl/Messaging/v1/SMSOnnet?from=" + senderNumber + "&to=" + number
                            + "&msg=test&deliverystatus=true&apikey" +
                            "=" + Helper.getConfigValue(FriendActivity.this, "api_orange_key");

                    apiUrlObject = new URL(urlToSend);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    apiUrlObject.openStream()));
                    String inputLine = "";

                    while ((inputLine = in.readLine()) != null) {
                        result.append(inputLine + "  " + number + "\n");
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        protected void onPostExecute(String result) {
            Toast.makeText(FriendActivity.this, result, Toast.LENGTH_LONG).show();
        }

    }

}
