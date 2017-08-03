package cn.com.lcase.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.img_voice)
    ImageView imgVoice;
    @BindView(R.id.ll_fenzu)
    LinearLayout llFenzu;
    @BindView(R.id.wave1)
    ImageView wave1;
    @BindView(R.id.wave2)
    ImageView wave2;
    @BindView(R.id.wave3)
    ImageView wave3;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.tv_group)
    TextView tvGroup;
    @BindView(R.id.img_delete)
    ImageView imgDelete;
    @BindView(R.id.no_search_layout)
    RelativeLayout noSearchLayout;
    private boolean isShowing = false;
    private static final int ANIMATIONEACHOFFSET = 300;
    private AnimationSet aniSet, aniSet2, aniSet3;
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
    private List<Device> mList = new ArrayList<>();
    private List<Device> mListswitch = new ArrayList<>();//电灯和排气扇都属于开关
    private List<Device> mListLamp = new ArrayList<>();//电灯
    private List<Device> mListTv = new ArrayList<>();
    private List<Device> mListExtractor = new ArrayList<>();//排气扇

    MyAdapter adapterAll;
    MyAdapter adapterLamp;
    MyAdapter adapterTv;
    MyAdapter adapterExtractor;
    boolean isFirst = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aniSet = getNewAnimationSet();
        aniSet2 = getNewAnimationSet();
        aniSet3 = getNewAnimationSet();
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);
        setActionBar();
        adapterAll = new MyAdapter(this, mList);
        listView.setAdapter(adapterAll);
//        initData(new Device());
        etSearch.addTextChangedListener(this);
        EventBus.getDefault().register(this);
        MyApplication.busAction = "DeviceActivity";
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
            case "DeviceActivity":
                switch (msg.getMsg()) {
                    case "FF01":
                        Log.d("DeviceActivity", "命令错误");
                        break;
                    case "FF02":
                        Log.d("DeviceActivity", "SDID错误");
                        break;
                    case "FF03":
                        Log.d("DeviceActivity", "指令消息错误");
                        break;
                    case "FF04":
                        Log.d("DeviceActivity", "子设备类型错误");
                        break;
                    case "FF05":
                        Log.d("DeviceActivity", "电视不在线");
//                        只有查询电视机状态 才会返回ff05才会告知离线
                        for (int i = 0; i < mList.size(); i++) {
                            Device tempD = mList.get(i);
                            if ("2".equals(tempD.getType())) {
                                tempD.setOnoff(false);
                                adapterAll.notifyDataSetChanged();
                                adapterTv.notifyDataSetChanged();
                            }
                        }
                        break;
                    case "FFFF":
                        Log.d("DeviceActivity", "未知错误");
                        break;
                    case "FF00":
                        Log.d("DeviceActivity", "FF00命令发送成功");
                        break;
                    default:
                        Log.d("DeviceActivity", "命令发送成功" + msg.getMsg());
                        changeDeviceState(msg.getMsg());//一接受到消息
//                        changeDeviceState2(msg.getMsg());
                        isOffLine(msg.getMsg());
                        queryTvResult(msg.getMsg());
                        break;
                }
                break;
        }
    }

    /**
     * 如果设备离线那么就发不出命令
     */
    private void isOffLine(String msg) {
//        电视离线10C49032BBB95B40E8A5F08F51FC8CD9AF00
//       开关在线1071F75C4437FA3263D8CB33188008FB7C01
        if (msg.startsWith("10")) {
            int size = mList.size();
            for (int i = 0; i < size; i++) {
                Device tempD = mList.get(i);
                if ("1".equals(tempD.getType())) {
//                    item.getCode() 设备编号
//                    71F75C4437FA3263D8CB33188008FB7C1
                    if (msg.substring(2, msg.length() - 2).equals(tempD.getCode().substring(0, tempD.getCode().length() - 1))) {
                        if (msg.endsWith("00")) {
                            ToastUtil.showDialog(DeviceActivity.this, tempD.getName() + "已下线");
                        }
                    }
                } else {
                    if (msg.endsWith("00")) {
                        ToastUtil.showDialog(DeviceActivity.this, tempD.getName() + "已下线");
                        tempD.setOnoff(false);
                        adapterAll.notifyDataSetChanged();
                        adapterTv.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    /**
     * 一接受到消息改变对应状态  这是非主动的那么反馈的信息是0a开头
     */
    private void changeDeviceState(String msg) {
        if (msg.startsWith("0a")) {
            if (msg.length() > 55) {
//                那就是开关
//        0a71F75C4437FA3263D8CB33188008FB7C000000000000000001030000 这是中控反馈的灯
//        0a 71F75C4437FA3263D8CB33188008FB7C(设备码) 00（状态关） 00000000000000 0103 0000 这是中控反馈的灯
                String tempCode = msg.substring(2, 34) + msg.substring(msg.length() - 7, msg.length() - 6);//71F75C4437FA3263D8CB33188008FB7C1
                String state = msg.substring(34, 36);//00
                Boolean tempIsOn = false;
                if ("01".equals(state)) {
                    tempIsOn = true;
                }
                int size = mList.size();
                for (int i = 0; i < size; i++) {
                    Device tempD = mList.get(i);
                    if (tempCode.equals(tempD.getCode()) && tempIsOn.compareTo(tempD.isOnoff()) != 0) {
                        tempD.setOnoff(tempIsOn);
                        adapterAll.notifyDataSetChanged();
                        adapterLamp.notifyDataSetChanged();
                        adapterExtractor.notifyDataSetChanged();
                    }
                }
            } else {
//        电视在线0aC49032BBB95B40E8A5F08F51FC8CD9AF01000000000000000000 总共55
//        电视离线0aC49032BBB95B40E8A5F08F51FC8CD9AF00000000000000000000
                String tempCode = msg.substring(2, 34);
                String state = msg.substring(34, 36);//00
                Boolean tempIsOn = false;
                if ("01".equals(state)) {
                    tempIsOn = true;
                }
                int size = mList.size();
                for (int i = 0; i < size; i++) {
                    Device tempD = mList.get(i);
                    if (tempCode.equals(tempD.getCode()) && tempIsOn.compareTo(tempD.isOnoff()) != 0) {
                        tempD.setOnoff(tempIsOn);
                        adapterAll.notifyDataSetChanged();
                        adapterLamp.notifyDataSetChanged();
                        adapterExtractor.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    /**
     * 这个是根据App主动获取子设备账号反馈的信息 这是主动的那么反馈的信息是09开头
     */
    private void changeDeviceState2(String msg) {
        if (msg.startsWith("09")) {
            //        0971F75C4437FA3263D8CB33188008FB7C000000000000000003030000 这是中控反馈的灯
//        09 71F75C4437FA3263D8CB33188008FB7C(设备码) 00（状态关） 00000000000000 0303 0000 这是中控反馈的灯
            String tempCode = msg.substring(2, 34) + msg.substring(msg.length() - 7, msg.length() - 6);//71F75C4437FA3263D8CB33188008FB7C3
            String state = msg.substring(34, 36);//00
            Boolean tempIsOn = false;
            if ("01".equals(state)) {
                tempIsOn = true;
            }
            int size = mList.size();
            for (int i = 0; i < size; i++) {
                Device tempD = mList.get(i);
                if (tempCode.equals(tempD.getCode())) {
                    tempD.setOnoff(tempIsOn);
                    adapterAll.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!isFirst) {
        initData(new Device());
//        }
    }

    /**
     * 查询所有设备状态
     */
    private void queryAllCommand() {
        for (int i = 0; i < mList.size(); i++) {
            Device d = mList.get(i);
            if ("1".equals(d.getType())) {
//                08 71F75C4437FA3263D8CB33188008FB7C
                MyApplication.mClient.publish(BizUtil.queryCommand(d.getCode().substring(0, d.getCode().length() - 1)));
            } else {
                MyApplication.mClient.publish(BizUtil.queryCommand(d.getCode()));
            }
        }
    }

    /**
     * 查询电视设备状态 是不是离线还是在线
     */
    private void queryTvCommand() {
        for (int i = 0; i < mList.size(); i++) {
            Device d = mList.get(i);
            if ("2".equals(d.getType())) {
//                08 C49032BBB95B40E8A5F08F51FC8CD9AF00
                MyApplication.mClient.publish(BizUtil.queryCommand(d.getCode()));
            }

        }
    }

    /**
     * 查询电视设备状态 的结果
     */
    private void queryTvResult(String msg) {
        if (msg.startsWith("09")) {
            String tempCode = msg.substring(2, 34) ;//C49032BBB95B40E8A5F08F51FC8CD9AF0000000000000000000000 电视在线
            String state = msg.substring(34, 36);//00
            Boolean tempIsOn = false;
            int size = mList.size();
            for (int i = 0; i < size; i++) {
                Device tempD = mList.get(i);
                if (tempCode.equals(tempD.getCode())) {
                    if ("2".equals(tempD.getType())) {
                        tempIsOn = true;
                    }
                    tempD.setOnoff(tempIsOn);
                    if (adapterAll != null) {
                        adapterAll.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private void initData(Device device) {
        isFirst = false;
        new LCaseApiClient().deviceList(device).enqueue(new Callback<ReturnVo<List<Device>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Device>>> call, Response<ReturnVo<List<Device>>> response) {
                if (response != null && response.body() != null) {
                    if (response.body().isSuccess()) {
                        if (response.body().getData() != null && response.body().getData().size() > 0) {
                            noSearchLayout.setVisibility(View.GONE);
                            mList.clear();
                            mList.addAll(response.body().getData());
                            adapterAll.notifyDataSetChanged();
                            splitData(response.body().getData());
//                            queryAllCommand();
                        } else {
                            noSearchLayout.setVisibility(View.VISIBLE);
                        }

                    } else {
                        if (response.body().getMessage() != null) {
                            showToast(response.body().getMessage());
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnVo<List<Device>>> call, Throwable t) {
                noSearchLayout.setVisibility(View.VISIBLE);
                showNetError();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) listView.getAdapter().getItem(position);
                if (device.getType().equals("1")) {
                    Intent intent = new Intent(DeviceActivity.this, DeskLampActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("device_id", device.getId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(DeviceActivity.this, TelevisionActivity.class);
                    intent.putExtra("device", device);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 拆分数据，为过滤使用
     *
     * @param data
     */
    private void splitData(List<Device> data) {
        mListswitch.clear();
        mListLamp.clear();
        mListTv.clear();
        mListExtractor.clear();
        for (Device device : data) {
            switch (device.getType()) {
                case "1":
                    mListswitch.add(device);
                    if (device.getName().startsWith("排气扇")) {
                        mListExtractor.add(device);
                    } else {
                        mListLamp.add(device);
                    }
                    break;
                case "2":
                    queryTvCommand();
                    mListTv.add(device);
                    break;
//                case "3":
//                    mListExtractor.add(device);
//                    break;
            }
        }
        CacheUtils.getInstants().setLamps(mListswitch);
        adapterLamp = new MyAdapter(this, mListLamp);
        adapterTv = new MyAdapter(this, mListTv);
        adapterExtractor = new MyAdapter(this, mListExtractor);
    }


    private void setActionBar() {
        configActionBar("设备");
        if (CacheUtils.getInstants().getCurrentUser() != null) {
            if (!CacheUtils.getInstants().getCurrentUser().isMainaccount()) {
                setAddViewVisible(View.GONE);
            } else {
                setAddViewVisible(View.VISIBLE);
                setAddImageResource(R.mipmap.add);
                setAddClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(DeviceActivity.this, AddDeviceActivity.class));
                    }

                });
            }
        }else {
            setAddViewVisible(View.VISIBLE);
            setAddImageResource(R.mipmap.add);
            setAddClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(DeviceActivity.this, AddDeviceActivity.class));
                }

            });
        }

    }

    @OnClick({R.id.ll_fenzu, R.id.img_voice, R.id.img_delete, R.id.no_search_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_fenzu://显示分组
                showPopupWindow();
                break;
            case R.id.img_delete://删除
                etSearch.setText("");
                BizUtil.hideKeybord(this);
                break;
            case R.id.no_search_layout://没设备
                Device device = new Device();
                if (!etSearch.getText().toString().trim().isEmpty()) {
                    device.setName(etSearch.getText().toString().trim());
                }
                initData(device);
                break;
            case R.id.img_voice://声控
               /* if (isShowing) {
                    cancalWaveAnimation();
                } else {
                    showWaveAnimation();
                }*/
                onClickVoice();
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

    private void showPopupWindow() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.window, null);
        final PopupWindow window = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.setTouchable(true);
        window.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        window.setBackgroundDrawable(new BitmapDrawable());
        window.showAsDropDown(llFenzu);
        final Button btnTv = (Button) view.findViewById(R.id.tv);
        final Button btnLamp = (Button) view.findViewById(R.id.lamp);
        final Button btnAll = (Button) view.findViewById(R.id.all);
        final Button btnExtractor = (Button) view.findViewById(R.id.extractor);
        view.findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                tvGroup.setText(btnTv.getText().toString());
                listView.setAdapter(adapterTv);
            }
        });
        view.findViewById(R.id.lamp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                tvGroup.setText(btnLamp.getText().toString());
                listView.setAdapter(adapterLamp);
            }
        });
        view.findViewById(R.id.all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                tvGroup.setText(btnAll.getText().toString());
                listView.setAdapter(adapterAll);
            }
        });
        view.findViewById(R.id.extractor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                tvGroup.setText(btnExtractor.getText().toString());
                listView.setAdapter(adapterExtractor);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String name = etSearch.getText().toString().toString();
        if (name.isEmpty()) {
            imgDelete.setVisibility(View.GONE);
        } else {
            imgDelete.setVisibility(View.VISIBLE);
        }
        Device device = new Device();
        device.setName(name);
        initData(device);
    }


    class MyAdapter extends BaseAdapter {
        Context mContext;
        List<Device> mList;

        public MyAdapter(Context mContext, List<Device> mList) {
            this.mContext = mContext;
            this.mList = mList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_scene_set, null);
                holder = new ViewHolder();
                holder.ch = (ToggleButton) convertView.findViewById(R.id.child_check);
                holder.img = (ImageView) convertView.findViewById(R.id.img_id);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.tvFenZu = (TextView) convertView.findViewById(R.id.tv_fenzu);
                holder.line = convertView.findViewById(R.id.line);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Device item = mList.get(position);
            holder.ch.setChecked(item.isOnoff());
            if ("1".equals(item.getType())) {
                //                        只有开关类的才可以操作开关  电视类的只能显示 显示的意思是是否上线
                holder.ch.setEnabled(true);
            } else {
                holder.ch.setEnabled(false);
            }

            holder.ch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isOnoff()) {
                        closeDevice(item, MyAdapter.this);
                    } else {
                        openDevice(item, MyAdapter.this);
                    }
                }
            });
            if (position == mList.size() - 1) {
                holder.line.setVisibility(View.VISIBLE);
            } else {
                holder.line.setVisibility(View.GONE);
            }
            if (item.getImage() != -1) {
                holder.img.setImageResource(LCaseConstants.DEVICE_IMG[item.getImage()]);
            }
            holder.tvName.setText(item.getName());
            holder.tvFenZu.setText("【" + item.getGroupname() + "】");
            return convertView;
        }

        class ViewHolder {
            ToggleButton ch;
            ImageView img;
            TextView tvName;
            TextView tvFenZu;
            View line;

        }
    }

    /**
     * 开启设备
     *
     * @param item
     */
    private void openDevice(final Device item, final MyAdapter adapter) {
        if ("2".equals(item.getType())) {
            //         电视机的开
            MyApplication.mClient.publish(BizUtil.tvCommand(item.getCode(), "000001", "000001", "0000"));
        } else {
            MyApplication.mClient.publish(BizUtil.OnCommand(item.getCode()));//开灯指令
        }
        Map<String, String> map = new HashMap<>();
        map.put("id", item.getId().toString());
        new LCaseApiClient().openDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        item.setOnoff(!item.isOnoff());
                    } else {
                        item.setOnoff(item.isOnoff());
                    }
                    adapter.notifyDataSetChanged();
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                item.setOnoff(item.isOnoff());
                adapter.notifyDataSetChanged();
                showNetError();
            }
        });
    }

    /**
     * 关闭设备
     *
     * @param item
     */
    private void closeDevice(final Device item, final MyAdapter adapter) {
        if ("2".equals(item.getType())) {
            //         电视机的关
            MyApplication.mClient.publish(BizUtil.tvCommand(item.getCode(), "000001", "000000", "0000"));
        } else {
            MyApplication.mClient.publish(BizUtil.OffCommand(item.getCode()));//关灯指令
        }
        Map<String, String> map = new HashMap<>();
        map.put("id", item.getId().toString());
        new LCaseApiClient().closeDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        item.setOnoff(!item.isOnoff());
                    } else {
                        item.setOnoff(item.isOnoff());
                    }
                    adapter.notifyDataSetChanged();
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                item.setOnoff(item.isOnoff());
                adapter.notifyDataSetChanged();
                showNetError();
            }
        });
    }

    public void showAnimation() {
        showWaveAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancalWaveAnimation();
    }

}
