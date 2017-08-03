package cn.com.lcase.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDeviceActivity extends BaseActivity {
    List<Device> list = new ArrayList<>();//获取的设备集合
    List<Device> newDevices = new ArrayList<>();//需要添加的设备
    boolean isFirst = true;//是否是第一次获取设备
    boolean isQueryFirst = true;
    private CountDownTimer timer;
    private boolean isGetMessage = false;//是否接收到信息
    Set<String> deviceIds = new HashSet();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1://检查设备是否存在服务器中
                    queryExistDevice();
                    break;
            }
        }
    };

    private void queryExistDevice() {
        Map<String, List<Device>> map = new HashMap<>();
        map.put("deviceList", list);
        new LCaseApiClient().queryExistDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        addFiled();
                    } else {
                        if (response.body().getData() != null) {
                            if (response.body().getData().equals(0.0) && response.body().getMessage() != null) {
                                String devices = response.body().getMessage();
                                String[] s = devices.split(",");
                                for (Device device : list) {
                                    for (String s1 : s) {
                                        if (device.getCode().equals(s1)) {
                                            newDevices.add(device);
                                        }
                                    }
                                }
                                CacheUtils.getInstants().setNewDevices(newDevices);
                                addSuccess();
                            } else {
                                addFiled();
                            }
                        } else {
                            addFiled();
                        }
                    }
                } else {
                    addFiled();
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                addFiled();
            }
        });
    }

    //添加成功
    private void addSuccess() {
        startActivity(new Intent(this, AddDeviceSuccessActivity.class));
        finish();
    }

    //添加失败
    private void addFiled() {
        startActivity(new Intent(this, AddDeviceFailedActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        configActionBar("添加设备");
        EventBus.getDefault().register(this);
        MyApplication.mClient.publish("0b");//搜索子设备列表命令
        MyApplication.busAction = "AddDeviceActivity";
        timer = new CountDownTimer(3 * 35 * 1000, 35 * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isGetMessage) {
                    MyApplication.mClient.publish("0b");//搜索子设备列表命令
                }
            }

            @Override
            public void onFinish() {
                addFiled();
            }
        };
        timer.start();
    }

    /**
     * EventBus 回调方法必须重写
     *
     * @param msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMessage msg) {
        switch (msg.getAction()) {
            case "AddDeviceActivity":
                isGetMessage = true;
                timer.cancel();
                if (msg.getMsg().startsWith("0f")) {//子设备列表响应命令是以“0f”开头
                    String mqttStr = msg.getMsg();
                    toSplit(mqttStr);
                    isFirst = false;
                }
                if (msg.getMsg().startsWith("09")) {//获取开关联数返回值
                    String mqttStr = msg.getMsg();
                    if (isQueryFirst) toSplitSwitch(mqttStr);
                }
                break;
        }
    }

    //截取开关联数的返回值
    private void toSplitSwitch(String mqttStr) {
        //09响应头  71F75C4437FA3263D8CB33188008FB7C设备号   00开关状态  0000000000000003开关的位置 030000开关的联数
        Device device = new Device();
        String id = mqttStr.substring(2, 34) + mqttStr.substring(mqttStr.length() - 7, mqttStr.length() - 6);
        if (deviceIds.contains(id)) {
            isQueryFirst = false;
            handler.sendEmptyMessage(1);
            return;
        }
        device.setCode(id);
        device.setType("1");
        list.add(device);
        deviceIds.add(id);
    }

    //截取设备列表的返回值
    private void toSplit(String mqttStr) {
        if (!isFirst) return;
        // 0f 0001 65492C34A4133C4195A0E7EF444B56B9 0002  71F75C4437FA3263D8CB33188008FB7C 0001 01 C49032BBB95B40E8A5F08F51FC8CD9AF 0002 01
        /*
        * 当中控设备的子设备列表信息发生变化时，中控设备应把最新的子设备列表信息通知到App
0f  0001（1个产品）  65492C34A4133C4195A0E7EF444B56B9（product_key）  0002（设备个数） 71F75C4437FA3263D8CB33188008FB7C（第一个SDID） 0001（类型1表示开关） 01(表示在线) C49032BBB95B40E8A5F08F51FC8CD9AF（第二个SDID） 0002（类型1表示电视）
01(表示在线)
        * */
        String count = mqttStr.substring(38, 42);
        int num = Integer.parseInt(count);
        if (num == 0) {
            addFiled();
            return;
        }
        String devices = mqttStr.substring(42, mqttStr.length());
        boolean hasOnOff = false;
        for (int i = 0; i < num; i++) {
            Device device = new Device();
            String code = devices.substring(38 * i, 38 * (i + 1));
            String deviceId = code.substring(0, 32);
            int type = Integer.parseInt(code.substring(32, 36));
            if (type != 1) {//1.开关 2.电视 如果是开关，需要去查询是几联开关
                device.setCode(deviceId);
                device.setType(type + "");
                list.add(device);
                deviceIds.add(deviceId);
            } else {
                hasOnOff = true;
                /**
                 * APP在获取子设备列表后，如果是开关，则发送查询子设备状态08 +{SDID}，检查开关总联数，按照联数保留灯具数量
                 */
                MyApplication.mClient.publish("08" + deviceId);
            }
        }
        if (!hasOnOff) {
            handler.sendEmptyMessage(1);
        }
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        timer.cancel();
        super.onDestroy();
    }
}
