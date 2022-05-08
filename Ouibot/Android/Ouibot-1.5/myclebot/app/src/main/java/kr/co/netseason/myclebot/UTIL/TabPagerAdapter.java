package kr.co.netseason.myclebot.UTIL;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.WeakHashMap;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;

public abstract class TabPagerAdapter extends FragmentStatePagerAdapter {
//    public static WeakHashMap<Integer, Fragment> mFragments

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
        Logger.d("!!!", "TabPagerAdapter 생성");
//        mFragments = new WeakHashMap<Integer, Fragment>();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment item = getFragmentItem(position);
//        MainActivity.CONTEXT.mFragments.put(Integer.valueOf(position), item);
        Logger.d("!!!", "getItem == " + MainActivity.CONTEXT.mFragments.size());
        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
//        Integer key = Integer.valueOf(position);
//        if (MainActivity.CONTEXT.mFragments.containsKey(key)) {
//            MainActivity.CONTEXT.mFragments.remove(key);
//        }
        Logger.d("!!!", "destroyItem == " + MainActivity.CONTEXT.mFragments.size());

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        //아래는 Processor kill 이후 생긴 문제를 해결하기위한 임시 방편 코드 입니다. 맨 처음에 앱 실행시 8개의 뷰가 호출됩니다. 나중에 필히 삭제해주세요..
        if(MainActivity.CONTEXT.mFragments.size() ==0){
            Logger.d("!!!", "notifyDataSetChanged == " + MainActivity.CONTEXT.mFragments.size());
//            getItem(0);
//            getItem(1);
//            getItem(2);
//            getItem(3);
//            return;
        }
        for (Integer position : MainActivity.CONTEXT.mFragments.keySet()) {
            if (position != null && MainActivity.CONTEXT.mFragments.get(position) != null && position.intValue() < getCount()) {
                updateFragmentItem(position, MainActivity.CONTEXT.mFragments.get(position));
            }
        }

    }

    @Override
    public int getItemPosition(Object object) {
        //If the object is a fragment, check to see if we have it in the hashmap
        if (object instanceof Fragment) {
            int position = findFragmentPositionHashMap((Fragment) object);
            //If fragment found in the hashmap check if it should be shown
            if (position >= 0) {
                //Return POSITION_NONE if it shouldn't be display
                return (position >= getCount() ? POSITION_NONE : position);
            }
        }

        return super.getItemPosition(object);
    }

    /**
     * Find the location of a fragment in the hashmap if it being view
     *
     * @param object the Fragment we want to check for
     * @return the position if found else -1
     */
    protected int findFragmentPositionHashMap(Fragment object) {
        for (Integer position : MainActivity.CONTEXT.mFragments.keySet()) {
            if (position != null &&
                    MainActivity.CONTEXT.mFragments.get(position) != null &&
                    MainActivity.CONTEXT.mFragments.get(position) == object) {
                return position;
            }
        }

        return -1;
    }

    public abstract Fragment getFragmentItem(int position);

    public abstract void updateFragmentItem(int position, Fragment fragment);

}
