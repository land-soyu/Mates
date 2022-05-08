package kr.co.netseason.myclebot.UTIL;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.WeakHashMap;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;

public abstract class SettingTabPagerAdapter extends FragmentStatePagerAdapter {
    public SettingTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment item = getFragmentItem(position);
        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (Integer position : MainActivity.CONTEXT.settingFragments.keySet()) {
            if (position != null && MainActivity.CONTEXT.settingFragments.get(position) != null && position.intValue() < getCount()) {
                updateFragmentItem(position, MainActivity.CONTEXT.settingFragments.get(position));
            }
        }

    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof Fragment) {
            int position = findFragmentPositionHashMap((Fragment) object);
            if (position >= 0) {
                return (position >= getCount() ? POSITION_NONE : position);
            }
        }
        return super.getItemPosition(object);
    }

    protected int findFragmentPositionHashMap(Fragment object) {
        for (Integer position : MainActivity.CONTEXT.settingFragments.keySet()) {
            if (position != null &&
                    MainActivity.CONTEXT.settingFragments.get(position) != null &&
                    MainActivity.CONTEXT.settingFragments.get(position) == object) {
                return position;
            }
        }

        return -1;
    }

    public abstract Fragment getFragmentItem(int position);

    public abstract void updateFragmentItem(int position, Fragment fragment);

}
