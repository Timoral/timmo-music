package com.timmo.music;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new GenresFragment();
            case 1:
                return new ArtistsFragment();
            case 2:
                return new AlbumsFragment();
            case 3:
                return new SongsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Genre";
            case 1:
                return "Artists";
            case 2:
                return "Albums";
            case 3:
                return "Songs";
            default:
                return "";
        }
    }
}