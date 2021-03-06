package com.example.q.project2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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

    public static String accountUID; // facebook unique id

    public static final String baseURL = "http://52.162.211.235:8080";

    LoginButton loginButton;
    CallbackManager callbackManager;

    public static boolean deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));

        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);

        Cursor cursor = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }



        return false;
    }

    public void insertContact(Context ctx, String tophone, String toname){
        try {
            // insert part
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            if (tophone != null) {
                ops.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, tophone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }
            if (toname != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                toname).build());
            }
            getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }


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
            //Log.d("Contacts Id : ", String.valueOf(ididx));
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
        for(int i=0;i<str1.length;i++) {
            arr_list.add(str1[i]);
        }
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

        //Log.i("getContacts()",result.toString());
        return result.toString();

    }

    public static String download(String username, String source){
        String result = "";
        HttpURLConnection con = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try{
            URL url = new URL(baseURL + source + username);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(2500);
            con.setReadTimeout(2500);
            isr = new InputStreamReader(con.getInputStream());
            br = new BufferedReader(isr);
            String str = null;
            while ((str = br.readLine()) != null) {
                result += str + "\n";
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(con != null){ try{con.disconnect();}catch(Exception e){} }
            if(isr != null){ try{isr.close();}catch(Exception e){} }
            if(br != null){ try{br.close();}catch(Exception e){} }
        }
        return result;
    }


    private String JSONForm(String from, String uniqueUserID){ // 전체 주소록을 parsing
        String[] arrayofContacts = from.split("\n");
        String result = "{\"hash\":" + uniqueUserID + ",";
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

    private boolean isLoggedIn(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(Profile.getCurrentProfile()==null){ return false;}
        return accessToken != null && !accessToken.isExpired();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void validate(){
        if(isLoggedIn()){

            accountUID = Profile.getCurrentProfile().getId();
            String FBname = Profile.getCurrentProfile().getName();

            String register_info = "{\"hash\":\"" + accountUID + "\",\"name\":\"" + FBname + "\"" + "}";
            Log.i("POST /REGISTER","String <" + register_info + "> to " + "/register");
            post_string(register_info,"/register");

            new Thread(){
                public void run(){
                    download(accountUID,"/");
                }
            }.start();
            //download(accountUID,"/");
        }
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

        // Already logged in when fragment starts :: Login Validation & Registration
        validate();

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                validate();
            }
            @Override
            public void onCancel() { }
            @Override
            public void onError(FacebookException exception) { }
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

        Button btn_download = (Button) view.findViewById(R.id.download_pno);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AccessToken token = AccessToken.getCurrentAccessToken();
                if(token==null) {
                    Toast.makeText(getContext(),"You are not logged in. Please sign in with Facebook to continue.",LENGTH_LONG).show();
                    return;
                }

                new Thread(){
                    public void run(){
                        if(!isLoggedIn()) return;


                        String response_result = download(accountUID,"/contact?hash="); // "test" -> accountUID
                        //String response_result = "{\"request\":\"success\",\"contacts\":[{\"id\":2,\"user_id\":1,\"contact_name\":\"jjong\",\"phone\":\"010-1231-1234\",\"description\":null},{\"id\":4,\"user_id\":8,\"contact_name\":\"jjong9\",\"phone\":\"010-1331-1234\",\"description\":null}]}";
                        String[] splicee1 = response_result.split("\"contacts\":");
                        if(splicee1.length==1) return;
                        String actual_res = splicee1[1];
                        actual_res = actual_res.substring(1,actual_res.length()-2);
                        String[] contacts_ = actual_res.split("\\},\\{"); // array of {"id":?, "user_id":?, "contact_name":"<name>","phone":"<>", ... }
                        for(String line:contacts_){
                            if(line.startsWith("\\{")) line = line.substring(1);
                            if(line.endsWith("\\}")) line = line.substring(0,line.length()-1);
                        }

                        for(String line : contacts_){
                            Log.i("lines",line);
                            String ContactName = ( (line.split(",")[2]).split(":\"")[1]);
                            ContactName = ContactName.substring(0,ContactName.length()-1);
                            String pNo = ((line.split(",")[3]).split(":\"")[1]);
                            pNo = pNo.substring(0,pNo.length()-1);
                            Log.i("newString",ContactName + "\t" + pNo);

                            // Add these on Listview
                            deleteContact(getContext(), pNo,ContactName);  // delete original contact if exists

                            insertContact(getContext(), pNo, ContactName);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getContacts(lv);
                                }
                            });
                        }
                    }
                }.start();
            }
        });


        Button btn_upload = (Button) view.findViewById(R.id.upload_pno);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessToken token = AccessToken.getCurrentAccessToken();
                if(token==null) {
                    Toast.makeText(getContext(),"You are not logged in. Please sign in with Facebook to continue.",LENGTH_LONG).show();
                    return;
                }

                String contact_str = JSONForm(getContacts(lv),"\"" + accountUID+ "\""); // the JSON String (String form)
                post_string(contact_str,"/contactHandler");
            }
        });

        return view;
    }

    public static void post_string(String sent, final String destination){
        try {
            final JSONObject contact_jsonobj = new JSONObject(sent); // The actual JSON Object to be sent
            new Thread(){
                public void run(){
                    try {
                        // SEND REQUEST TO POST METHOD CODE
                        // 52.162.211.235:7714 / contact?hash=213412341 :받아오기(GET)
                        // /contactHandle    (POST) hash : 132412341, contacts : [{ "name" : "asdfh"
                        URL url = new URL(baseURL + destination);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setRequestProperty("Content-Type","application/json");
                        httpURLConnection.setRequestProperty("Accept","application/json");

                        DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                        wr.write(contact_jsonobj.toString().getBytes());
                        Integer responsecode = httpURLConnection.getResponseCode();

                        BufferedReader bufferedReader;

                        if(responsecode>199 && responsecode < 300){
                            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        } else {
                            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                        }
                        // get response part
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line=bufferedReader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        bufferedReader.close();

                        Log.i("response",content.toString());
                        /// POST TO SERVER
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
