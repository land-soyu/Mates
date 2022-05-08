package kr.co.netseason.myclebot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kr.co.netseason.myclebot.API.ContactListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Security.SecurePreference;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.View.ContactModifyDialog;
import kr.co.netseason.myclebot.openwebrtc.CallActivity;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;


public class DialActivity extends FragmentActivity {
    private Context context;
    private TextView ouibotID;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        if ( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            setContentView(R.layout.view_dial);
//        } else {
//            setContentView(R.layout.view_dial);
//        }

        setContentView(R.layout.view_dial);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_dial);

        //서비스에 바인드 하도록 변경 2016.03.30
        //프로세서 킬 이후 아래 객체가 날라가는 문제 발견.
//        mService = getIntent().getParcelableExtra("message");
        initView();
        LoadDBData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        unbindService(conn);
    }

    public void LoadDBData() {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "phonebook_list_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(getApplicationContext()));
    }

    private Messenger mService;

    private void initView() {
        context = this;
        ImageView keypad_back = (ImageView)findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ouibotID = (TextView)findViewById(R.id.ouibotid);
        ImageView ouibotIddel = (ImageView)findViewById(R.id.ouibotiddel);
        ouibotIddel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ouibotID.getText().toString();
                int i = text.length();
                if (i > 0) {
                    text = text.substring(0, i - 1);
                    ouibotID.setText(text);
                }
            }
        });

        ImageButton dial_1 = (ImageButton)findViewById(R.id.dial_1);
        dial_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("1");
            }
        });
        ImageButton dial_2 = (ImageButton)findViewById(R.id.dial_2);
        dial_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("2");
            }
        });
        ImageButton dial_3 = (ImageButton)findViewById(R.id.dial_3);
        dial_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("3");
            }
        });
        ImageButton dial_4 = (ImageButton)findViewById(R.id.dial_4);
        dial_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("4");
            }
        });
        ImageButton dial_5 = (ImageButton)findViewById(R.id.dial_5);
        dial_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("5");
            }
        });
        ImageButton dial_6 = (ImageButton)findViewById(R.id.dial_6);
        dial_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("6");
            }
        });
        ImageButton dial_7 = (ImageButton)findViewById(R.id.dial_7);
        dial_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("7");
            }
        });
        ImageButton dial_8 = (ImageButton)findViewById(R.id.dial_8);
        dial_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("8");
            }
        });
        ImageButton dial_9 = (ImageButton)findViewById(R.id.dial_9);
        dial_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("9");
            }
        });
        ImageButton dial_0 = (ImageButton)findViewById(R.id.dial_0);
        dial_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouibotID.append("0");
            }
        });

        ImageButton dial_message = (ImageButton)findViewById(R.id.dial_message);
        dial_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ouibotID.getText().toString().equals("") ) {
                    Toast.makeText(context, getString(R.string.input_number), Toast.LENGTH_SHORT).show();
                } else if (ouibotID.getText().toString().length() != 8 ) {
                    Toast.makeText(context, getString(R.string.format_not_number_retry), Toast.LENGTH_SHORT).show();
                } else if (ouibotID.getText().toString().equals(OuiBotPreferences.getLoginId(context)) ) {
                    Toast.makeText(context, getString(R.string.can_not_message_you), Toast.LENGTH_SHORT).show();
                } else {
                    Intent nextActivity = new Intent(context, MessageSendActivity.class);
                    nextActivity.putExtra("peer_rtcid", ouibotID.getText().toString());
                    nextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(nextActivity);
                }
            }
        });
        ImageButton dial_call = (ImageButton)findViewById(R.id.dial_call);
        dial_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ouibotID.getText().toString().equals("") ) {
                    Toast.makeText(context, getString(R.string.input_number), Toast.LENGTH_SHORT).show();
                } else if (ouibotID.getText().toString().length() != 8 ) {
                    Toast.makeText(context, getString(R.string.format_not_number_retry), Toast.LENGTH_SHORT).show();
                } else if (ouibotID.getText().toString().equals(OuiBotPreferences.getLoginId(context)) ) {
                    Toast.makeText(context, getString(R.string.can_not_call_you), Toast.LENGTH_SHORT).show();
                } else {
                    new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "update_number.php", "rtcid=" + ouibotID.getText().toString());
                }
            }
        });


    }

    private String getNumberName(String str) {
        String returnStr = "Dial";
        for ( ContactListData data : mContactListData ) {
            if ( data.mNumber.equalsIgnoreCase(str) ) {
                returnStr = data.mName;
            }
        }
        return returnStr;
    }

    private void CallingStart() {
        try {
            Message msg = Message.obtain(null, Config.CALL_ACTIVITY_START, getNumberName(ouibotID.getText().toString())+"|" + ouibotID.getText().toString());
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<ContactListData> mContactListData = new ArrayList<ContactListData>();

    //AsyncTask<param,Progress,Result>
    private class httpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {

            String returnValue = "";
            HttpURLConnection conn = null;
            try {
                Logger.e("!!!", "args[0] = " + args[0]);
                Logger.e("!!!", "args[1] = " + args[1]);
                String urlString = Config.Server_IP + args[0];
                Logger.e("!!!", "urlString = " + urlString);
                URL url = new URL(urlString);

                // open connection
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);            // 입력스트림 사용여부
                conn.setDoOutput(false);            // 출력스트림 사용여부
                conn.setUseCaches(false);        // 캐시사용 여부
                conn.setReadTimeout(3000);        // 타임아웃 설정 ms단위
//                conn.setRequestMethod("GET");  // or GET
                conn.setRequestMethod("POST");

                // POST 값 전달 하기
                StringBuffer params = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //한글일 경우 URL인코딩
                params.append(args[1]);
                PrintWriter output = new PrintWriter(conn.getOutputStream());
                output.print(params.toString());
                output.close();

                // Response받기
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                for (; ; ) {
                    String line = br.readLine();
                    if (line == null) break;
                    sb.append(line + "\n");
                }

                br.close();
                conn.disconnect();
                br = null;
                conn = null;

                returnValue = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn!=null) {
                    conn.disconnect();
                }
            }
            return returnValue;
        }

        private ContactModifyDialog mCustomDialog;
        @Override
        protected void onPostExecute(String result) {
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if (result.contains("NOT OK") || result.contains("true") || result.contains("false") || result.trim().equals("") || result.trim().equals("[]") || result.trim().contains("null")) {
                return;
            }

            if ( result.contains("number") ) {
                String[] strs = result.split("\\|");
                if ( strs.length < 2 ) {
                    if ( Config.isPhoneId(ouibotID.getText().toString())) {
                        mCustomDialog = new ContactModifyDialog(context,
                                getString(R.string.phone_update_message),
                                getString(R.string.confirm), "", "",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mCustomDialog.dismiss();
                                    }
                                },
                                null,
                                null);
                        mCustomDialog.show();
                    } else {
                        mCustomDialog = new ContactModifyDialog(context,
                                getString(R.string.ouibot_update_message),
                                getString(R.string.confirm), "", "",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mCustomDialog.dismiss();
                                    }
                                },
                                null,
                                null);
                        mCustomDialog.show();
                    }
                } else {
                    CallingStart();
                }
                return;
            }

            try {
                JSONArray json = new JSONArray(result);

                mContactListData.clear();
                if (json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        ContactListData addInfo = null;
                        addInfo = new ContactListData(json.getJSONObject(i).getString("peer_name"), json.getJSONObject(i).getString("peer_rtcid"), json.getJSONObject(i).getInt("cert"), json.getJSONObject(i).getInt("cert"), json.getJSONObject(i).getInt("cert"), json.getJSONObject(i).getString("request"), json.getJSONObject(i).getString("app"));
                        mContactListData.add(addInfo);
                    }
                } else {
                    Logger.e("!!!", "Data impty");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
