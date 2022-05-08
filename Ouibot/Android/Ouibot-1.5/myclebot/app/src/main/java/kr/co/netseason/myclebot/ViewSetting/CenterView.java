package kr.co.netseason.myclebot.ViewSetting;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import kr.co.netseason.myclebot.ContectAddActivity;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class CenterView extends Fragment {
    private RelativeLayout centerview;
    private LinearLayout btn_control_long;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.e("!!!", "CenterView onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.e("!!!", "CenterView onCreateView");
        if(Config.Mode == Config.COMPILE_Ouibot){
            if (MainActivity.CONTEXT.settingFragments.containsKey(3)) {
                MainActivity.CONTEXT.settingFragments.remove(3);
            }
            MainActivity.CONTEXT.settingFragments.put(3, this);
        }else {
            if (MainActivity.CONTEXT.settingFragments.containsKey(2)) {
                MainActivity.CONTEXT.settingFragments.remove(2);
            }
            MainActivity.CONTEXT.settingFragments.put(2, this);
        }
        centerview = (RelativeLayout) inflater.inflate(R.layout.view_setting_center, container, false);


        btn_control_long = (LinearLayout) centerview.findViewById(R.id.btn_control_long);
        btn_control_long.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://oui-bot.com/ouibot/customer/faq.php"));
                startActivity(intent);
            }
        });

        return centerview;
    }

}
