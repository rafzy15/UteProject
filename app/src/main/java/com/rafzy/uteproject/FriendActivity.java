package com.rafzy.uteproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
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
    private List<String> checkedChild;
    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.friend_layout);
        List<FriendObject> friendsList =  new ArrayList<>();
        expandableListView = (ExpandableListView) findViewById(R.id.friendExpendableList);
        expandableListDetail = new HashMap<>();
        //TODO add elements to the list from database
        //TODO allow to add friends.
        friendsList.add(new FriendObject("adam ","5498234982"));
        friendsList.add(new FriendObject("Jan", "55323"));
        friendsList.add(new FriendObject("Johny", "5532335"));

        expandableListDetail.put("ZNAJOMI", friendsList);

        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        friendListAdapter = new FriendListAdapter(this, expandableListTitle, expandableListDetail);

        expandableListView.setAdapter(friendListAdapter);
        expandableListView.expandGroup(0);
        checkedChild = new LinkedList<>();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v,
                                        int groupPosition,
                                        int childPosition,
                                        long id) {
                Toast.makeText(FriendActivity.this,"das",Toast.LENGTH_SHORT).show();
                CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox_item);
                 cb = (CheckBox)v.findViewById(R.id.checkbox_item);

                cb.setChecked(!cb.isChecked());
                TextView friendNumber = (TextView) v.findViewById(R.id.expandedFriendNumber);
                if(cb.isChecked()){
                    checkedChild.add((String)friendNumber.getText());
                }else {
                    checkedChild.remove(friendNumber.getText());
                }
                System.out.println(checkedChild.toString());

                return true;
            }
        });
        FriendsDatabaseHelper friendsDatabaseHelper = new FriendsDatabaseHelper(this);
        SQLiteDatabase db = friendsDatabaseHelper.getReadableDatabase();
        checkButtonClick();
//        Cursor cursor =
//                db.query("FRIENDS", new String[]{"NAME", "NUMBER"}, null, null, null, null, null);
//        Log.i("Friend TAG","cursor " + cursor.getString(0));

    }
    private void checkButtonClick() {
        Button myButton = (Button) findViewById(R.id.findSelected);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                StringBuffer responseText = new StringBuffer();
                responseText.append("The following were selected...\n");

                friendListAdapter.isChildSelectable(0,0);
//                for(int i=0;i<numberOfFriend;i++){
//                    System.out.println(expandableListAdapter.getChild(0,0));
//                }

                Toast.makeText(getApplicationContext(),
                        responseText, Toast.LENGTH_LONG).show();

            }
        });

    }













    private final static String xmlResult = "<response><msisdn>48510123456</msisdn><terminal-availability>AVAILABLE</terminal-availability><terminal-page-result>OK</terminal-page-result><age-of-location>0</age-of-location><location-information><current-lac>58140</current-lac><current-cell-id>47025</current-cell-id></location-information></response>";

    private String myCellNumber;
    private Pair<Double,Double> myPosition;
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
    public void onClickShareWithFriend(View view) {
        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = telemamanger.getSimSerialNumber();

        setMyPositionAndCell(simSerialNumber);


        //// TODO: 12/28/16 send sms to the friends which are at the same cell about what is interesting near me

//        getFriendPosition();
//        Intent mapIntent = new Intent(this, MapsActivity.class);
//        startActivity(mapIntent);
    }
    private void setMyPositionAndCell(String number){
        this.myCellNumber = getCellNumber(number);
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

}
