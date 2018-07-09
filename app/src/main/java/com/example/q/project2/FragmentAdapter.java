package com.example.q.project2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private int tabCount;
    private static final String TAG = "FragmentAdapter";

    public FragmentAdapter(FragmentManager fm, int tabCount){
        super(fm);
        this.tabCount=tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        if(position > this.getCount() || position < 0)
            Log.d(TAG, "Error On Setting Fragment");

        switch (position) {
            case 0 :
                return new Contacts();
<<<<<<< HEAD
=======
//                return new GridViewFragment();

>>>>>>> 155114e0547396625890b55d18791c6e029fda54
            case 1 :
                return new GridViewFragment();
            case 2 :
                return new GridViewFragment();
//                return new Temp();
            default:
                Log.e("MainFragmentAdapter : ", "CANNOT GET FRAGMENTS");
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

}
