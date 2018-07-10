package com.example.q.project2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static android.widget.Toast.LENGTH_LONG;

public class Contacts extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public Contacts(){ } // constructor

    private ListView lv = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String phone, name, contactName;
    private ListAdapter listAdapter;

    private String accountUID; // facebook unique id

    LoginButton loginButton;
    CallbackManager callbackManager;

    private String getContacts(ListView lv){
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        int ididx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int nameidx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
//        Log.d(TAG,"Contact id is " + ContactsContract.Contacts._ID);
//        Log.d(TAG,"Contact display name is " + ContactsContract.Contacts.DISPLAY_NAME);

        StringBuilder result = new StringBuilder();
        while(cursor.moveToNext())
        {
            result.append(cursor.getString(nameidx) + ": ");
            Log.d("Contacts Id : ", String.valueOf(ididx));
            String id = cursor.getString(ididx);
            Cursor cursor2 = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[]{id},null);
            //Log.d(TAG,"Contact_id is " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            int typeidx = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            int numidx = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//            Log.d(TAG,"사용자 ID : " +   cursor2.getString(0));
//            Log.d(TAG,"사용자 이름 : " + cursor2.getString(1));

            while (cursor2.moveToNext()){
                String num = cursor2.getString(numidx);
                switch(cursor2.getInt(typeidx)){
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        result.append("\t Mobile:"+num);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
//                        result.append("\t Home:"+num);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        //                       result.append("\t Work:"+num);
                        break;
                }
            }
            cursor2.close();
            result.append("\n");

        }
        cursor.close();


//        Log.i("addrList",result.toString());
        //inflate was here
        String str= result.toString();
        ArrayList<String>arr_list = new ArrayList<>();
        String[] str1=str.split("\n");
        for(int i=0;i<str1.length;i++)
            arr_list.add(str1[i]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arr_list){


            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Cast the list view each item as text view
                TextView item = (TextView) super.getView(position,convertView,parent);

                // return the view
                return item;
            }

        };

        // Assign adapter to ListView
        adapter.sort(new Comparator<String>(){

            @Override
            public int compare(String arg1,String arg0){
                return arg1.compareTo(arg0);
            }
        });
        lv.setAdapter(adapter);


        ListViewExampleClickListener listViewExampleClickListener = new ListViewExampleClickListener();
        lv.setOnItemClickListener(listViewExampleClickListener);

        Log.i("getContacts()",result.toString());
        return result.toString();

    }

    private String JSONForm(String from, String uniqueUserID){ // 전체 주소록을 parsing
        String[] arrayofContacts = from.split("\n");
        String result = "{\"userid\":" + uniqueUserID + ",";
        String CtList = "";
        result += "contacts=[";
        if(arrayofContacts.length == 0){
            result += "]}";
            Log.i("JSonParser(Empty)",result);
            return result;
        }

        for(String line : arrayofContacts){
            String[] tmp = line.split(":");
            String item = "{\"name\":";
            item += ("\"" + tmp[0] + "\"");
            item += ",";
            item += "\"phone\":"
            ;
            item += ("\"" + tmp[2] + "\"");
            item += "}";
            CtList += (item + ",");
        }
        CtList = CtList.substring(0,CtList.length()-1);
        result += CtList;
        result += "]}";
        Log.i("JSONParser",result);
        return result;
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts, container, false);
        FacebookSdk.sdkInitialize(getContext());
        if (ContextCompat.checkSelfPermission(getContext(),Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return view;
        }

        // facebook login check
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        //

        // Already logged in when fragment starts
        if(isLoggedIn) accountUID = Profile.getCurrentProfile().getId();

        Toast.makeText(getContext(),"You are logged in as " + accountUID+ ".", LENGTH_LONG).show();

        callbackManager = CallbackManager.Factory.create();


        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accountUID = Profile.getCurrentProfile().getId();
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout_contacts);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        lv = view.findViewById(R.id.listview_contacts);

        Button btn_load = (Button) view.findViewById(R.id.load_pno);
        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContacts(lv);
            }
        });
        //getContacts(lv); :

        Button btn_upload = (Button) view.findViewById(R.id.upload_pno);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sent = JSONForm(getContacts(lv),accountUID); // the JSON String (String form)
                try {
                    JSONObject contact_jsonobj = new JSONObject(sent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        return view;
    }


    private class ListViewExampleClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            String rawString = lv.getItemAtPosition(position).toString();
            String cmpPhone = "Mobile:";
            String cmpName=":";

            int a2 = rawString.indexOf(cmpName);
            int a1 = rawString.indexOf(cmpPhone)+7;

            if(a1==6){
                phone="";
            } else{
                phone = rawString.substring(a1);
            }
            name = rawString.substring(0, a2);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(name);
            builder.setMessage(phone);

            builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if(phone==""){
                        String contactID = null;
                        ContentResolver contentResolver = getActivity().getContentResolver();
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, Uri.encode(phone));
                        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null,null,null);

                        if(cursor!=null){
                            while(cursor.moveToNext()){
                                //contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                                contactID= cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                            }
                            cursor.close();
                        }

                        Intent intent_contacts=new Intent(Intent.ACTION_EDIT, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, Uri.encode(contactID)));
                        startActivity(intent_contacts);

                    }else{
                        String contactID = null;
                        ContentResolver contentResolver = getActivity().getContentResolver();

                        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));

                        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null,null,null);

                        if(cursor!=null){
                            while(cursor.moveToNext()){
                                //contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                                contactID= cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                            }
                            cursor.close();
                        }

                        Intent intent_contacts=new Intent(Intent.ACTION_EDIT, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, Uri.encode(contactID)));
                        startActivity(intent_contacts);
                    }

                }
            });
            builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String tel = "tel:"+ phone;
                    Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                    try{
                        startActivity(intent);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String tel = "smsto:"+ phone;
                    Intent intent=new Intent(Intent.ACTION_SENDTO, Uri.parse(tel));
                    try{
                        startActivity(intent);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            builder.show();
        }
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                /*
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

                int ididx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int nameidx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
//        Log.d(TAG,"Contact id is " + ContactsContract.Contacts._ID);
//        Log.d(TAG,"Contact display name is " + ContactsContract.Contacts.DISPLAY_NAME);

                StringBuilder result = new StringBuilder();
                while(cursor.moveToNext())
                {
                    result.append(cursor.getString(nameidx) +  ": ");

                    String id = cursor.getString(ididx);
                    Cursor cursor2 = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[]{id},null);
//Log.d(TAG,"Contact_id is " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                    int typeidx = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int numidx = cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//            Log.d(TAG,"사용자 ID : " + cursor2.getString(0));
//            Log.d(TAG,"사용자 이름 : " + cursor2.getString(1));

                    while (cursor2.moveToNext()){
                        String num = cursor2.getString(numidx);
                        switch(cursor2.getInt(typeidx)){
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                result.append("\t Mobile:"+num);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
//                                result.append("\t Home:"+num);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
 //                               result.append("\t Work:"+num);
                                break;
                        }
                    }
                    cursor2.close();
                    result.append("\n");

                }
                cursor.close();

                //inflate was here
                String str= result.toString();
                ArrayList<String>arr_list = new ArrayList<>();
                String[] str1=str.split(""); \n
                for(int i=0;i<str1.length;i++)
                    arr_list.add(str1[i]);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arr_list){

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        // Cast the list view each item as text view
                        TextView item = (TextView) super.getView(position,convertView,parent);

                        // return the view
                        return item;
                    }

                };

                // Assign adapter to ListView
                adapter.sort(new Comparator<String>(){
                    @Override
                    public int compare(String arg1,String arg0){
                        return arg1.compareTo(arg0);
                    }
                });
                lv.setAdapter(adapter);

                */
                String resultString = getContacts(lv);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }
}
