package com.example.q.project2;

//import com.example.q.project2.GridViewImageAdapter;
//import com.example.q.project2.AppConstant;
//import com.example.q.project2.Utils;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GridViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Utils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;
    private ArrayList<HashMap<String,String>> data;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public GridViewFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_grid_view, container, false);
        data = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return view;
        }

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout_gridview);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        gridView = (GridView) view.findViewById(R.id.grid_view);

        utils = new Utils(getContext());

        Button btn_online = (Button) view.findViewById(R.id.btn_fromweb);
        btn_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog online_gal= new Dialog(getContext());
                online_gal.setContentView(R.layout.multitab_tmp);

                //
                final ListView ogList = (ListView) online_gal.findViewById(R.id.og_list);
                Button btn_close = (Button) online_gal.findViewById(R.id.btn_close_og);
                final ImageView imgView = (ImageView) online_gal.findViewById(R.id.imgpreview);

                btn_close.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        online_gal.dismiss();
                    }
                });

                try {
                    new Thread() {
                        public void run() {
                            String og_request = Contacts.download(Contacts.accountUID, "/gallery?hash=");
                            //String og_request = "{\"result\":\"success\",\"galleries\":[{\"path\":\"<path>\",\"url\":\"<url>\"},{\"path\":\"<path>\",\"url\":\"<url>\"},{\"path\":\"<path>\",\"url\":\"<url>\"}]}";
                            String spliced1 = og_request.split("\"galleries\":")[1];
                            spliced1 = spliced1.substring(1, spliced1.length() - 2);
                            String cutter = "\\},\\{";
                            Log.i("cutter", cutter);
                            //Log.i("spliced1",spliced1);
                            String[] pictures = spliced1.split(cutter);

                            data.clear();

                            for (String lines : pictures) {
                                if (lines.startsWith("{")) lines = lines.substring(1);
                                if (lines.endsWith("}"))
                                    lines = lines.substring(0, lines.length() - 1);

                                String pathItem = lines.split(":\"")[1].split("\",")[0];
                                String urlItem = lines.split(":\"")[2].split("\"")[0];

                                Log.i("Each Item path", pathItem);
                                Log.i("Each Item url", urlItem.replace("\n", ""));

                                HashMap<String, String> element = new HashMap<>();
                                element.put("path", pathItem);
                                element.put("base64", urlItem.replace("\\n", ""));

                                data.add(element);
                                final SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), data, android.R.layout.simple_list_item_1,
                                        new String[]{"path"}, new int[]{android.R.id.text1});


                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ogList.setAdapter(simpleAdapter);
                                        ogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                String imageB64 = data.get(i).get("base64");
                                                // TODO: decode imageB64, show in bitmap
                                                byte[] decodedString = Base64.decode(imageB64, Base64.DEFAULT);
                                                Log.i("decodedStr", String.valueOf(decodedString.length));
                                                Bitmap decodedbyte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                                imgView.setImageBitmap(decodedbyte);
                                                simpleAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }.start();

                    online_gal.show();
                    // 서버와 연결해서 JSONObject를 가져온 후
                    // ArrayList<HashMap<String,String>> data에 "path","base64"로 삽입
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        // Initilizing Grid View
        InitilizeGridLayout();

        // loading all image paths from SD card
        imagePaths = utils.getFilePaths();

        // Gridview adapter
        adapter = new GridViewImageAdapter(getActivity(), imagePaths,
                columnWidth);

        // setting grid view adapter
        gridView.setAdapter(adapter);

        return view;
    }

    private void InitilizeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConstant.GRID_PADDING, r.getDisplayMetrics());

        columnWidth = (int) ((utils.getScreenWidth() - ((AppConstant.NUM_OF_COLUMNS + 1) * padding)) / AppConstant.NUM_OF_COLUMNS);

        gridView.setNumColumns(AppConstant.NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);

    }

    @Override
    public void onRefresh() {

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){

                utils = new Utils(getContext());

                // Initilizing Grid View
                InitilizeGridLayout();

                // loading all image paths from SD card
                imagePaths = utils.getFilePaths();


                // Gridview adapter
                adapter = new GridViewImageAdapter(getActivity(), imagePaths,
                        columnWidth);

                // setting grid view adapter
                gridView.setAdapter(adapter);

                mSwipeRefreshLayout.setRefreshing(false);
            }
        },500);
    }
}