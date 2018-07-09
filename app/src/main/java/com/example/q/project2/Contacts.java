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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;

public class Contacts extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public Contacts(){ }
    private ListView lv = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String phone, name, contactName;
    private ListAdapter listAdapter;
    //private String TAG="Contacts";
    //private static final int REQUEST_CONTACT=1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts, container, false);

        if (ContextCompat.checkSelfPermission(getContext(),Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(),Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return view;
        }

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout_contacts);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        lv = view.findViewById(R.id.listview_contacts);

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
//            Log.d(TAG,"사용자 ID : " + cursor2.getString(0));
//            Log.d(TAG,"사용자 이름 : " + cursor2.getString(1));

            while (cursor2.moveToNext()){
                String num = cursor2.getString(numidx);
                switch(cursor2.getInt(typeidx)){
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        result.append("\t Mobile:"+num);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        result.append("\t Home:"+num);
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        result.append("\t Work:"+num);
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
        String[] str1=str.split("\n");
        for(int i=0;i<str1.length;i++)
            arr_list.add(str1[i]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arr_list){


            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Cast the list view each item as text view
                TextView item = (TextView) super.getView(position,convertView,parent);



                // Set the list view item's text color
                item.setTextColor(Color.parseColor("#000000")); // Black
                Typeface customFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Chewy.ttf");
                item.setTypeface(customFont);

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
                                result.append("\t Home:"+num);
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                result.append("\t Work:"+num);
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
                String[] str1=str.split("\n");
                for(int i=0;i<str1.length;i++)
                    arr_list.add(str1[i]);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arr_list){

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        // Cast the list view each item as text view
                        TextView item = (TextView) super.getView(position,convertView,parent);


                        // Set the list view item's text color
                        item.setTextColor(Color.parseColor("#000000")); // Black
                        Typeface customFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Chewy.ttf");
                        item.setTypeface(customFont);

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

                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }
}
