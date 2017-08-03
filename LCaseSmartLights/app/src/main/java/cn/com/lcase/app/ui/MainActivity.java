package cn.com.lcase.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.PreferencesUtil;
import cn.com.lcase.app.utils.VoiceUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {

    @BindView(R.id.layout_add)
    RelativeLayout layoutAdd;
    @BindView(R.id.img_voice)
    ImageView imgVoice;
    @BindView(R.id.img_shebei)
    ImageView imgShebei;
    @BindView(R.id.img_fenzu)
    ImageView imgFenzu;
    @BindView(R.id.img_changjing)
    ImageView imgChangjing;
    @BindView(R.id.img_wode)
    ImageView imgWode;
    @BindView(R.id.wave1)
    ImageView wave1;
    @BindView(R.id.wave2)
    ImageView wave2;
    @BindView(R.id.wave3)
    ImageView wave3;
    private boolean isShowing = false;
    private static final int ANIMATIONEACHOFFSET = 300;
    private AnimationSet aniSet, aniSet2, aniSet3;
    private String tag = "MainActivity";
    private WifiManager mWifi;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x222) {
                wave2.startAnimation(aniSet2);
            } else if (msg.what == 0x333) {
                wave3.startAnimation(aniSet3);
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aniSet = getNewAnimationSet();
        aniSet2 = getNewAnimationSet();
        aniSet3 = getNewAnimationSet();
        if (MyApplication.mClient != null) {
            MyApplication.mClient.publish("0b");//一进来的时候就先发0b 因为重启设备后需要发0b才能通讯
        }
        //    讯飞语音实例对象
        MyApplication.voiceUtil = new VoiceUtil(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //设置状态栏
        setStatusBar();
        if (CacheUtils.getInstants().getCurrentUser() != null) {
            if (!CacheUtils.getInstants().getCurrentUser().isMainaccount()) {
                layoutAdd.setVisibility(View.GONE);
            } else {
                layoutAdd.setVisibility(View.VISIBLE);
            }
        } else {
            layoutAdd.setVisibility(View.VISIBLE);
        }
        EventBus.getDefault().register(this);
        MyApplication.busAction = "MainActivity";
        MyApplication.voiceUtil.setWakeUpAnimation(new VoiceFinishListener() {
            @Override
            public void onfinish() {
                cancalWaveAnimation();
            }
        }, this);
    }

    /**
     * EventBus 回调方法必须重写
     *
     * @param msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMessage msg) {
        switch (msg.getAction()) {
            case "MainActivity":
                switch (msg.getMsg()) {
                    case "FF01":
                        Log.d("MainActivity", "命令错误");
                        break;
                    case "FF02":
                        Log.d("MainActivity", "SDID错误");
                        break;
                    case "FF03":
                        Log.d("MainActivity", "指令消息错误");
                        break;
                    case "FF04":
                        Log.d("MainActivity", "子设备类型错误");
                        break;
                    case "FFFF":
                        Log.d("MainActivity", "未知错误");
                        break;
                    case "FF00":
                        Log.d("MainActivity", "FF00命令发送成功");
                        break;
                    default:
                        Log.d("MainActivity", "命令发送成功" + msg.getMsg());
                        break;
                }
                break;
        }
    }

    private AnimationSet getNewAnimationSet() {
        AnimationSet as = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(1f, 10f, 1f, 10f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(ANIMATIONEACHOFFSET * 3);
        sa.setRepeatCount(-1);//
        AlphaAnimation aniAlp = new AlphaAnimation(1, 0.1f);
        aniAlp.setRepeatCount(-1);//
        as.setDuration(ANIMATIONEACHOFFSET * 3);
        as.addAnimation(sa);
        as.addAnimation(aniAlp);
        return as;
    }

    private void showWaveAnimation() {
        if (isShowing == true) {
            return;
        }
        isShowing = true;
        wave1.startAnimation(aniSet);
        handler.sendEmptyMessageDelayed(0x222, ANIMATIONEACHOFFSET);
        handler.sendEmptyMessageDelayed(0x333, ANIMATIONEACHOFFSET * 2);

    }

    public void cancalWaveAnimation() {
        isShowing = false;
        wave1.clearAnimation();
        wave2.clearAnimation();
        wave3.clearAnimation();
    }

    /**
     * 透明状态栏
     */
    private void setStatusBar() {
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @OnClick({R.id.img_shebei, R.id.img_fenzu, R.id.img_changjing, R.id.img_wode, R.id.layout_add, R.id.img_voice})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_shebei:
                startActivity(new Intent(this, DeviceActivity.class));
                break;
            case R.id.img_fenzu:
                startActivity(new Intent(this, GroupingActivity.class));
                break;
            case R.id.img_changjing:
                startActivity(new Intent(this, SceneMainActivity.class));
                break;
            case R.id.img_wode:
                startActivityForResult(new Intent(this, UserInfoActivity.class), 1);
                break;
            case R.id.img_voice:
                onClickVoice();
                break;
            case R.id.layout_add:
                startActivity(new Intent(this, AddDeviceActivity.class));
                break;
        }
    }

    /**
     * 点击话筒之后的操作
     */
    private void onClickVoice() {
        if (!isShowing) {
            showWaveAnimation();
            MyApplication.voiceUtil.holdVoiceButton(new VoiceFinishListener() {
                @Override
                public void onfinish() {
                    cancalWaveAnimation();
                }
            });
        } else {
            cancalWaveAnimation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
        opeanVoice();
    }

    @Override
    protected void onDestroy() {
        Log.d(tag, "onResume");
        super.onDestroy();
        if (MyApplication.mClient != null) {
            MyApplication.mClient.onDestroy();
        }
        if (MyApplication.voiceUtil != null) {
            MyApplication.voiceUtil.onDestroy();
        }
        EventBus.getDefault().unregister(this);
    }

    private void getData() {
        /**
         * 获取设备列表
         */
        new LCaseApiClient().deviceList(new Device()).enqueue(new Callback<ReturnVo<List<Device>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Device>>> call, Response<ReturnVo<List<Device>>> response) {
                if (response != null && response.body() != null) {
                    if (response.body().isSuccess()) {
                        if (response.body().getData() != null && response.body().getData().size() > 0) {
                            MyApplication.setListDevice(response.body().getData());
                        } else {
                            Log.d(tag, "response.body().getData()>0");
                        }
                    } else {
                        if (response.body().getMessage() != null) {
                            Log.d(tag, response.body().getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<Device>>> call, Throwable t) {
                Log.d(tag, " initdata Throwable" + t.toString());
            }
        });
        /**
         * 获取场景
         */
        new LCaseApiClient().sceneList().enqueue(new Callback<ReturnVo<List<Scene>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Scene>>> call, Response<ReturnVo<List<Scene>>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        if (response.body().getData() != null && response.body().getData().size() > 0) {
                            MyApplication.setListScene(response.body().getData());
                        }
                    } else {
                        if (response.body().getMessage() != null)
                            Log.d(tag, "sceneList" + response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<Scene>>> call, Throwable t) {
                Log.d(tag, " sceneList initdata Throwable" + t.toString());
            }
        });
        /**
         * 获取专属设备数据  获取到专属设备 按道理直接获取设备列表就可以  但是有时候比如我已经分类好了专属设备 选中主卧这个组
         * 但是主卧中的某个设备换组别了那么就要根据组别去找那些组
         *
         * 所以找专属设备 先获取选中的组别 然后再去根据组别去获取选中组别下的设备
         */
        new LCaseApiClient().queryPrivateDevice().enqueue(new Callback<ReturnVo<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Group>>> call, Response<ReturnVo<List<Group>>> response) {
                if (response.body() != null) {
                    ReturnVo<List<Group>> body = response.body();
                    if (body.isSuccess()) {
                        if (body.getData() != null && body.getData().size() > 0)
                            setData(body.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<Group>>> call, Throwable t) {

            }
        });

    }
    private void  getGrounp(){
        /**
         * 获取组别
         */
        Map<String, String> map = new HashMap<>();
        new LCaseApiClient().queryGroupInfo(map).enqueue(new Callback<ReturnVo<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Group>>> call, Response<ReturnVo<List<Group>>> response) {
                if (response.body() != null) {
                    ReturnVo<List<Group>> body = response.body();
                    if (body.isSuccess()) {
                        if (body.getData() != null && body.getData().size() > 0)
                            splitData(body.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<Group>>> call, Throwable t) {

            }
        });
    }
    /**
     * 拆分数据
     *
     * @param data
     */
    private void splitData(List<Group> data) {
        List<Device> temp = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String groupName = data.get(i).getGroupname();
            if (groupName .equals(MyApplication.getGroupName())) {
                for (Device device : data.get(i).getDevicelist()) {
                    device.setGroupname(groupName);
                    temp.add(device);
                }
            }
        }
        if (temp.size() > 0) {
            MyApplication.setExclusiveDeviceList(temp);
        }
    }
    /**
     * 整理拿到专属设备的数据
     *
     * @param groups
     */
    private void setData(List<Group> groups) {
        List<Device> temp = new ArrayList<>();

        for (int i = 0; i < groups.size(); i++) {
            Group groupTemp = groups.get(i);
            if (groupTemp.getGroupname().contains("卧")) {
                for (int j = 0; j < groupTemp.getDevicelist().size(); j++) {
                    groupTemp.getDevicelist().get(j).setGroupname(groupTemp.getGroupname());
                    if (groupTemp.getDevicelist().get(j).isUnique()) {
                        MyApplication.setGroupName(groupTemp.getGroupname());
                        temp.add(groupTemp.getDevicelist().get(j));
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(MyApplication.getGroupName())) {
            getGrounp();
        }
        if (temp.size() > 0) {
            MyApplication.setExclusiveDeviceList(temp);
        }
    }

    public void showAnimation() {
        showWaveAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancalWaveAnimation();
    }

    /**
     * 是否开启语音唤醒
     */
    private void opeanVoice() {
//        if (PreferencesUtil.getLanguageFeedback()) {
//            voiceSwitch.setChecked(true);
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
//                    wifi_open.setChecked(true);
                    break;
            }
//        } else {
////            voiceSwitch.setChecked(false);
//            MyApplication.voiceUtil.stopVoiceWakeuper();
//        }
    }

    /**
     * 获取当前连接的wifi的名字
     */
    private String getWifiName() {
        mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int wifiState = mWifi.getWifiState();
        WifiInfo info = mWifi.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : "";
        return wifiId;
    }
}
