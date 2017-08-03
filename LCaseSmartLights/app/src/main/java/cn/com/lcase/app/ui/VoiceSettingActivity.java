package cn.com.lcase.app.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.utils.PreferencesUtil;
import cn.com.lcase.app.utils.ScreenUtil;
import cn.com.lcase.app.utils.ToastUtil;

public class VoiceSettingActivity extends BaseActivity {

    @BindView(R.id.voice_switch)
    ToggleButton voiceSwitch;
    @BindView(R.id.chose_wifi)
    TextView choseWifi;
    @BindView(R.id.wifi_open)
    CheckBox wifi_open;
    @BindView(R.id.always_open)
    CheckBox alwaysOpen;
    @BindView(R.id.always_close)
    CheckBox alwaysClose;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.chose_wifi_layout)
    LinearLayout choseWifiLayout;
    private List<ScanResult> list = new ArrayList<>();
    private MyAdapter adapter;
    private WifiManager wifiManager;
    private Dialog dialog;
    private String wifiName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_setting);
        ButterKnife.bind(this);
        setActionBar();
        adapter = new MyAdapter();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        initEvent();
//        init();
        if (PreferencesUtil.getVoicedback()) {
            voiceSwitch.setChecked(true);
        } else {
            voiceSwitch.setChecked(false);
        }
        switch (PreferencesUtil.getVoiceRelatedStatus()) {
            case 1:
                alwaysOpen.setChecked(true);
                break;
            case 2:
                alwaysClose.setChecked(true);
                break;
            default:
                wifi_open.setChecked(true);
                String str = PreferencesUtil.getRelatedWifiName();
                if (!TextUtils.isEmpty(str)) {
                    choseWifi.setText("指定WIFI开启（点击添加WIFI）\n指定WIFI为："+str);
                }
                break;
        }
    }

    private void init() {
        openWifi();
        list.clear();
        list.addAll(wifiManager.getScanResults());
        if (list == null) {
            Toast.makeText(this, "wifi未打开！", Toast.LENGTH_LONG).show();
        } else {
            adapter.notifyDataSetChanged();
        }
//        listView.getLayoutParams().height = listView.getLayoutParams().height/2;


    }

    /**
     * 打开WIFI
     */
    private void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

    }

    private void setActionBar() {
        configActionBar("语音唤醒设置");
        setSaveViewVisible(View.VISIBLE);
    }

    /**
     * 保存数据
     */
    private void saveData() {
        if (voiceSwitch.isChecked()) {
            PreferencesUtil.setVoicedback(true);
        }else {
            PreferencesUtil.setVoicedback(false);
        }
        if (wifi_open.isChecked()) {
            PreferencesUtil.setVoiceRelatedStatus(0);
            PreferencesUtil.setRelatedWifiName(wifiName);
        } else if (alwaysOpen.isChecked()) {
            MyApplication.voiceUtil.openVoiceWakeuper();
            PreferencesUtil.setVoiceRelatedStatus(1);
        } else if (alwaysClose.isChecked()) {
            MyApplication.voiceUtil.stopVoiceWakeuper();
            PreferencesUtil.setVoiceRelatedStatus(2);
        }
        opeanVoice();
      /*  if (voiceSwitch.isChecked()) {
            PreferencesUtil.setLanguageFeedback(true);
            if (wifi_open.isChecked()) {
                PreferencesUtil.setVoiceRelatedStatus(0);
                PreferencesUtil.setRelatedWifiName(wifiName);
            } else if (alwaysOpen.isChecked()) {
                MyApplication.voiceUtil.openVoiceWakeuper();
                PreferencesUtil.setVoiceRelatedStatus(1);
            } else if (alwaysClose.isChecked()) {
                MyApplication.voiceUtil.stopVoiceWakeuper();
                PreferencesUtil.setVoiceRelatedStatus(2);
            }
        } else {
            PreferencesUtil.setLanguageFeedback(false);
        }*/
    }
    /**
     * 是否开启语音唤醒
     */
    private void opeanVoice() {
        switch (PreferencesUtil.getVoiceRelatedStatus()) {
            case 1:
//                    alwaysOpen.setChecked(true);
                MyApplication.voiceUtil.openVoiceWakeuper();
                break;
            case 2:
//                    alwaysClose.setChecked(true);
                MyApplication.voiceUtil.stopVoiceWakeuper();
                break;
            default:
                String str = PreferencesUtil.getRelatedWifiName();
                if (TextUtils.isEmpty(str)) {
                    MyApplication.voiceUtil.openVoiceWakeuper();
                } else if (str.equals(getWifiName())) {
                    MyApplication.voiceUtil.openVoiceWakeuper();
                } else {
                    MyApplication.voiceUtil.stopVoiceWakeuper();
                }
                break;
        }

    }
    /**
     * 获取当前连接的wifi的名字
     */
    private String getWifiName() {
        WifiManager  mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int wifiState = mWifi.getWifiState();
        WifiInfo info = mWifi.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : "";
        return wifiId;
    }
    private void initEvent() {
        setSaveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                finish();
//                showLoadingDialog();
//                Snackbar.make(container, "hehehehe", Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        showWifiDialog();
//                    }
//                }).show();
            }

        });
  /*      voiceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!voiceSwitch.isChecked()) {
//                    由开到关的状态
                    wifi_open.setChecked(false);
                    choseWifi.setText("指定WIFI开启（点击添加WIFI）");
                    alwaysOpen.setChecked(false);
                    alwaysClose.setChecked(false);
                }
            }
        });*/
        alwaysClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (voiceSwitch.isChecked()) {
                    wifi_open.setChecked(false);
                    choseWifi.setText("指定WIFI开启（点击添加WIFI）");
                    alwaysOpen.setChecked(false);
                    alwaysClose.setChecked(true);
//                } else {
//                    alwaysClose.setChecked(false);
//                }
            }
        });
        alwaysOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (voiceSwitch.isChecked()) {
                    wifi_open.setChecked(false);
                    choseWifi.setText("指定WIFI开启（点击添加WIFI）");
                    alwaysOpen.setChecked(true);
                    alwaysClose.setChecked(false);
//                } else {
//                    alwaysOpen.setChecked(false);
//                }
            }
        });
    }

    @OnClick(R.id.chose_wifi_layout)
    public void onClick() {
//        if (voiceSwitch.isChecked()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                /**
                 * 请求权限是一个异步任务  不是立即请求就能得到结果 在结果回调中返回
                 */
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE}, 2);

            } else {
                showWifiDialog();
            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showWifiDialog();
                } else {
                    ToastUtil.showToast(this, "获取权限失败,请在设置中手动打开权限");
                }
                return;
            }
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(VoiceSettingActivity.this, R.layout.item_wifi, null);
                holder = new ViewHolder();
                holder.chose = (CheckBox) convertView.findViewById(R.id.chose);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ScanResult result = list.get(position);
            holder.tvName.setText(result.SSID);
            return convertView;
        }


        class ViewHolder {
            TextView tvName;
            CheckBox chose;
        }
    }

    private void showWifiDialog() {
        init();
        dialog = new Dialog(this, R.style.Translucent_NoTitle1);
        View view = View.inflate(this, R.layout.list_wifi_dialog, null);
        ListView listView = (ListView) view.findViewById(R.id.lsv);
        final List<String> names = new ArrayList<>();
        for (ScanResult result : list) {
            names.add(result.SSID);
        }
        if (names.size() == 0) return;
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.wifi_name_item, R.id.tv_name, names));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);
        dialog.show();

        //获取listView 的高度
        int totalHeight = 0;

        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
            View listItem = listView.getAdapter().getView(i, null, listView);
            listItem.measure(0, 0); //计算子项View 的宽高 //统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
        attributes.width = ScreenUtil.getScreenWidth(this) * 2 / 3;
        if (totalHeight > ScreenUtil.getScreenHeight(this)) {
            attributes.height = ScreenUtil.getScreenHeight(this) * 2 / 3;
        }
        dialog.getWindow().setAttributes(attributes);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectWifiUi(names.get(i));
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * 选择wifi后的其他状态改变
     */
    private void selectWifiUi(String name) {
        wifi_open.setChecked(true);
        alwaysOpen.setChecked(false);
        alwaysClose.setChecked(false);
        wifiName = name;
        choseWifi.setText("指定WIFI开启（点击添加WIFI）\n指定WIFI为："+wifiName);
    }
}
