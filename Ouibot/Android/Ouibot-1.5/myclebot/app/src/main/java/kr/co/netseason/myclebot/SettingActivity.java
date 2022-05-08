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
import kr.co.netseason.myclebot.ViewSetting.CenterView;
import kr.co.netseason.myclebot.ViewSetting.NoticeView;
import kr.co.netseason.myclebot.ViewSetting.SpemListView;
import kr.co.netseason.myclebot.ViewSetting.VersionView;
import kr.co.netseason.myclebot.ViewSetting.ViewPermissionView;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class SettingActivity extends FragmentActivity implements ViewPermissionView.onPermissionClickListener{
    public static SettingActivity CONTEXT;

    private SettingTabPagerAdapter settingTabPagerAdapter;
    private ViewPager viewPager;

    private Messenger mService;

    private LinearLayout viewpermissionmenu;
    private LinearLayout spemlistmenu;
    private LinearLayout noticemenu;
    private LinearLayout centermenu;
    private LinearLayout versionmenu;

    private TextView viewpermissionmenu_text;
    private TextView spemlistmenu_text;
    private TextView noticemenu_text;
    private TextView centermenu_text;
    private TextView versionmenu_text;

    private LinearLayout viewpermissionmenu_line;
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
            viewPager.setCurrentItem(2);
        }
    }

    private void initUI() {
        setContentView(R.layout.activity_setting);

        ImageView ic_action_back = (ImageView) findViewById(R.id.ic_action_back);
        ic_action_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        settingTabPagerAdapter = new SettingTabPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getFragmentItem(int position) {
                if (position == 0) {
                    return new ViewPermissionView();
                } else if (position == 1) {
                    return new SpemListView();
                } else if (position == 2) {
                    return new NoticeView();
                } else if (position == 3) {
                    return new CenterView();
                } else {
                    return new VersionView();
                }
            }

            @Override
            public void updateFragmentItem(int position, Fragment fragment) {
                Logger.d("!!!", "updateFragmentItem position =" + position);
                if (position == 1) {
                    ((SpemListView) fragment).getSpemDBData(getApplicationContext());
                } else if (position == 0) {
                    ((ViewPermissionView) fragment).LoadDBData(getApplicationContext());
                }
            }

            @Override
            public int getCount() {
                return 5;
            }
        };
        viewPager = (ViewPager) findViewById(R.id.setting_activity_main_container);
        viewPager.setAdapter(settingTabPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setTopMenuBackGroundColor(position);
            }
        });
        viewPager.setOffscreenPageLimit(1);

//        uiTopSetting();
        initTopMenu();
    }

//    private void uiTopSetting() {
//        LinearLayout ic_action_back_Panel = (LinearLayout)findViewById(R.id.ic_action_back_Panel);
//        UIUtil.setIMAGESIZE(this, 60, ic_action_back_Panel, 1, Gravity.CENTER);
//        ImageView ic_action_back = (ImageView)findViewById(R.id.ic_action_back);
//        UIUtil.setIMAGESIZE(this, 30, ic_action_back, 1, Gravity.CENTER);
//
//        LinearLayout ic_action_setting_Panel = (LinearLayout)findViewById(R.id.ic_action_setting_Panel);
//        UIUtil.setIMAGESIZE(this, 633, 60, ic_action_setting_Panel, 1, Gravity.CENTER);
//        ImageView ic_action_setting = (ImageView)findViewById(R.id.ic_action_setting);
//        UIUtil.setIMAGESIZE(this, 30, ic_action_setting, 1, Gravity.CENTER);
//
//        LinearLayout ic_action_setting_Panel_ = (LinearLayout)findViewById(R.id.ic_action_setting_Panel_);
//        UIUtil.setIMAGESIZE(this, 60, ic_action_setting_Panel_, 1, Gravity.CENTER);
//    }

    private void initTopMenu() {
        viewpermissionmenu = (LinearLayout) findViewById(R.id.setting_menu_view_permission);
        viewpermissionmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        viewpermissionmenu_text = (TextView) findViewById(R.id.setting_menu_view_permission_text);
        viewpermissionmenu_line = (LinearLayout) findViewById(R.id.setting_menu_view_permission_line);

        spemlistmenu = (LinearLayout) findViewById(R.id.setting_menu_spem);
        spemlistmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        spemlistmenu_text = (TextView) findViewById(R.id.setting_menu_spem_text);
        spemlistmenu_line = (LinearLayout) findViewById(R.id.setting_menu_spem_line);

        noticemenu = (LinearLayout) findViewById(R.id.setting_menu_notice);
        noticemenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });
        noticemenu_text = (TextView) findViewById(R.id.setting_menu_notice_text);
        noticemenu_line = (LinearLayout) findViewById(R.id.setting_menu_notice_line);

        centermenu = (LinearLayout) findViewById(R.id.setting_menu_center);
        centermenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(3);
            }
        });
        centermenu_text = (TextView) findViewById(R.id.setting_menu_center_text);
        centermenu_line = (LinearLayout) findViewById(R.id.setting_menu_center_line);

        versionmenu = (LinearLayout) findViewById(R.id.setting_menu_version);
        versionmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(4);
            }
        });
        versionmenu_text = (TextView) findViewById(R.id.setting_menu_version_text);
        versionmenu_line = (LinearLayout) findViewById(R.id.setting_menu_version_line);

//        uimenuSetting();
    }

//    private void uimenuSetting() {
//        LinearLayout setting_top_menu_Panel = (LinearLayout)findViewById(R.id.setting_top_menu_Panel);
//        UIUtil.setIMAGESIZE(this, LinearLayout.LayoutParams.MATCH_PARENT, 45, setting_top_menu_Panel, 1, 0);
//    }

    private void setTopMenuBackGroundColor(int index) {
        viewpermissionmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        viewpermissionmenu_line.setBackgroundResource(R.color.top_menu_bg);
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
                viewpermissionmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                viewpermissionmenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 1:
                spemlistmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                spemlistmenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 2:
                noticemenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                noticemenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 3:
                centermenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                centermenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 4:
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
                if (settingTabPagerAdapter != null)
                    settingTabPagerAdapter.notifyDataSetChanged();
            }
        });
    }

}
