package kr.co.netseason.myclebot;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.UTIL.SettingTabPagerAdapter;
import kr.co.netseason.myclebot.UTIL.TabPagerAdapter;
import kr.co.netseason.myclebot.ViewSetting.CenterView;
import kr.co.netseason.myclebot.ViewSetting.NoticeView;
import kr.co.netseason.myclebot.ViewSetting.SpemListView;
import kr.co.netseason.myclebot.ViewSetting.VersionView;
import kr.co.netseason.myclebot.ViewSetting.ViewPermissionView;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class SettingAndroidActivity extends FragmentActivity implements ViewPermissionView.onPermissionClickListener{
    public static SettingAndroidActivity CONTEXT;

    private SettingTabPagerAdapter tabPagerAdapter;
    private ViewPager viewPager;

    private Messenger mService;

    private LinearLayout spemlistmenu;
    private LinearLayout noticemenu;
    private LinearLayout centermenu;
    private LinearLayout versionmenu;

    private TextView spemlistmenu_text;
    private TextView noticemenu_text;
    private TextView centermenu_text;
    private TextView versionmenu_text;

    private LinearLayout spemlistmenu_line;
    private LinearLayout noticemenu_line;
    private LinearLayout centermenu_line;
    private LinearLayout versionmenu_line;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int currentItem = viewPager.getCurrentItem();
        Logger.e("!!!", "SettingActivity onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        initUI();
        viewPager.setCurrentItem(currentItem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CONTEXT = this;
        switch (Config.Mode) {
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 2:
                break;
            default:
                break;
        }

        mService = getIntent().getParcelableExtra("message");
        initUI();

        if ( getIntent().getIntExtra("alram", -1) > -1 ) {
            viewPager.setCurrentItem(1);
        }
    }

    private void initUI() {
        setContentView(R.layout.activity_setting_android);

        ImageView ic_action_back = (ImageView) findViewById(R.id.ic_action_back);
        ic_action_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tabPagerAdapter = new SettingTabPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getFragmentItem(int position) {
                if (position == 0) {
                    return new SpemListView();
                } else if (position == 1) {
                    return new NoticeView();
                } else if (position == 2) {
                    return new CenterView();
                } else {
                    return new VersionView();
                }
            }

            @Override
            public void updateFragmentItem(int position, Fragment fragment) {
                Logger.d("!!!", "updateFragmentItem position =" + position);
                if (position == 0) {
                    ((SpemListView) fragment).getSpemDBData(getApplicationContext());
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
        viewPager = (ViewPager) findViewById(R.id.setting_activity_main_container);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setTopMenuBackGroundColor(position);
            }
        });
        viewPager.setOffscreenPageLimit(1);

        initTopMenu();
    }

    private void initTopMenu() {
        spemlistmenu = (LinearLayout) findViewById(R.id.setting_menu_spem);
        spemlistmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        spemlistmenu_text = (TextView) findViewById(R.id.setting_menu_spem_text);
        spemlistmenu_line = (LinearLayout) findViewById(R.id.setting_menu_spem_line);

        noticemenu = (LinearLayout) findViewById(R.id.setting_menu_notice);
        noticemenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        noticemenu_text = (TextView) findViewById(R.id.setting_menu_notice_text);
        noticemenu_line = (LinearLayout) findViewById(R.id.setting_menu_notice_line);

        centermenu = (LinearLayout) findViewById(R.id.setting_menu_center);
        centermenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });
        centermenu_text = (TextView) findViewById(R.id.setting_menu_center_text);
        centermenu_line = (LinearLayout) findViewById(R.id.setting_menu_center_line);

        versionmenu = (LinearLayout) findViewById(R.id.setting_menu_version);
        versionmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(3);
            }
        });
        versionmenu_text = (TextView) findViewById(R.id.setting_menu_version_text);
        versionmenu_line = (LinearLayout) findViewById(R.id.setting_menu_version_line);
    }

    private void setTopMenuBackGroundColor(int index) {
        spemlistmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        spemlistmenu_line.setBackgroundResource(R.color.top_menu_bg);
        noticemenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        noticemenu_line.setBackgroundResource(R.color.top_menu_bg);
        centermenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        centermenu_line.setBackgroundResource(R.color.top_menu_bg);
        versionmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        versionmenu_line.setBackgroundResource(R.color.top_menu_bg);
        switch (index) {
            case 0:
                spemlistmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                spemlistmenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 1:
                noticemenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                noticemenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 2:
                centermenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                centermenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 3:
                versionmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                versionmenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPermissionCALLClick(String number) {
        Logger.e("!!!", "onPermissionCALLClick = " + number);
        try {
            Message msg = Message.obtain(null, Config.CERTIFICATION_ANSWER, number);
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshViewPaagerData();
    }

    public void refreshViewPaagerData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tabPagerAdapter != null)
                    tabPagerAdapter.notifyDataSetChanged();
            }
        });
    }
}
