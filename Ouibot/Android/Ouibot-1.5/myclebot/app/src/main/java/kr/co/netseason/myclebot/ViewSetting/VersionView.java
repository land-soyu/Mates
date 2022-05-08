package kr.co.netseason.myclebot.ViewSetting;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class VersionView extends Fragment {
    private Context context;
    private RelativeLayout versionView;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.e("!!!", "VersionView onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.e("!!!", "VersionView onCreateView");
        if(Config.Mode == Config.COMPILE_Ouibot){
            if (MainActivity.CONTEXT.settingFragments.containsKey(4)) {
                MainActivity.CONTEXT.settingFragments.remove(4);
            }
            MainActivity.CONTEXT.settingFragments.put(4, this);
        }else {
            if (MainActivity.CONTEXT.settingFragments.containsKey(3)) {
                MainActivity.CONTEXT.settingFragments.remove(3);
            }
            MainActivity.CONTEXT.settingFragments.put(3, this);
        }
        context = inflater.getContext();
        versionView = (RelativeLayout) inflater.inflate(R.layout.view_setting_version, container, false);
        TextView versionview_empty = (TextView)versionView.findViewById(R.id.versionview_empty);
        versionview_empty.setText(Config.getAppVersionName(context));
        return versionView;
    }
}
