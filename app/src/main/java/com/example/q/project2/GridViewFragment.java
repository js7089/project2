package com.example.q.project2;

//import com.example.q.project2.GridViewImageAdapter;
//import com.example.q.project2.AppConstant;
//import com.example.q.project2.Utils;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

public class GridViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Utils utils;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private GridViewImageAdapter adapter;
    private GridView gridView;
    private int columnWidth;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public GridViewFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_grid_view, container, false);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return view;
        }

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout_gridview);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        gridView = (GridView) view.findViewById(R.id.grid_view);

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