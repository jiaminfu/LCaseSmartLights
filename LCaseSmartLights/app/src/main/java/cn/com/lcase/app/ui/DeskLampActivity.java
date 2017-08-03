package cn.com.lcase.app.ui;

import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.adapter.HeadViewPagerAdapter;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.NumberText;
import cn.com.lcase.app.utils.ScreenUtil;
import cn.com.lcase.app.utils.ToastUtil;
import cn.com.lcase.app.widget.HeadViewPagerTransformer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cn.com.lcase.app.net.LCaseConstants.DEVICE_NAME;


public class DeskLampActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    //    @BindView(R.id.fancyCoverFlow)
//    FancyCoverFlow fancyCoverFlow;
    @BindView(R.id.viewpager_show)
    ViewPager mViewPager;
    @BindView(R.id.img_voice)
    ImageView img_voice;
    @BindView(R.id.wave1)
    ImageView wave1;
    @BindView(R.id.wave2)
    ImageView wave2;
    @BindView(R.id.wave3)
    ImageView wave3;
    private int checkedPosition = -1;
    private RadioGroup group1;
    private RadioGroup group2;
    private RadioGroup group3;
    private RadioGroup group4;
    private RadioGroup group5;
    private EditText etGroupName;
    private List<Device> lamps;
    //    private MyFancyCoverFlowAdapter adapter;
    int currentPosition;
    int chose;
    private boolean isShowing = false;
    private AnimationSet aniSet, aniSet2, aniSet3;
    private static final int ANIMATIONEACHOFFSET = 300;
    private int pos;

    private int count;
    private Boolean isOn;

    private HeadViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desk_lamp);
        ButterKnife.bind(this);
        setActionBar();
        aniSet = getNewAnimationSet();
        aniSet2 = getNewAnimationSet();
        aniSet3 = getNewAnimationSet();
        EventBus.getDefault().register(this);
        MyApplication.busAction = "DeskLampActivity";
        initData();
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
            case "DeskLampActivity":
                switch (msg.getMsg()) {
                    case "FF01":
                        Log.d("DeskLampActivity", "命令错误");
                        break;
                    case "FF02":
                        Log.d("DeskLampActivity", "SDID错误");
                        break;
                    case "FF03":
                        Log.d("DeskLampActivity", "指令消息错误");
                        break;
                    case "FF04":
                        Log.d("DeskLampActivity", "子设备类型错误");
                        break;
                    case "FFFF":
                        Log.d("DeskLampActivity", "未知错误");
                        break;
                    default:
                        Log.d("DeskLampActivity", "命令发送成功");
                        break;
                }
                break;
        }
    }

    private void initData() {
        pos = getIntent().getIntExtra("position", 0);
        int id = getIntent().getIntExtra("device_id", 0);
        lamps = CacheUtils.getInstants().getLamps();
        if (id != 0) {
            for (int i = 0; i < lamps.size(); i++) {
                if (lamps.get(i).getId() - id == 0) {
                    pos = i;
                }
            }
        }
//        adapter = new MyFancyCoverFlowAdapter(this, lamps, 3, 1);
//        fancyCoverFlow.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//        fancyCoverFlow.setUnselectedAlpha(0.5f);//通明度
//        fancyCoverFlow.setUnselectedSaturation(0.5f);//设置选中的饱和度
//        fancyCoverFlow.setUnselectedScale(0.2f);//设置选中的规模
//        fancyCoverFlow.setSpacing(0);//设置间距
//        fancyCoverFlow.setMaxRotation(0);//设置最大旋转
//        fancyCoverFlow.setScaleDownGravity(0.5f);
//        fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
        int num = Integer.MAX_VALUE / 2 % lamps.size();
        int selectPosition = Integer.MAX_VALUE / 2 - num;
//        fancyCoverFlow.setSelection(selectPosition + pos);
//        fancyCoverFlow.setCallbackDuringFling(false);
//        fancyCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position != currentPosition) return;
//                Device item = (Device) adapter.getItem(position % lamps.size());
//                if (item.isOnoff()) {
//                    MyApplication.mClient.publish(BizUtil.OffCommand(item.getCode()));//关灯指令
//                    closeDevice();
//                    item.setOnoff(false);
//                    adapter.notifyDataSetChanged();
//                } else {
//                    MyApplication.mClient.publish(BizUtil.OnCommand(item.getCode()));//开灯指令
//                    openDevice();
//                    item.setOnoff(true);
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });
//        fancyCoverFlow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                currentPosition = position;
//                Device item = (Device) adapter.getItem(position % lamps.size());
//                chose = item.getImage();
//                if (item != null) {
//                    CacheUtils.getInstants().setCurrentDevice(item);
//                    configActionBar(item.getName());
//                }
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
        inidViewPager(selectPosition);
    }

    private void inidViewPager(int selectPosition ) {
        viewPagerAdapter = new HeadViewPagerAdapter(this, lamps, mViewPager);
        mViewPager.setAdapter(viewPagerAdapter);
        mViewPager.setCurrentItem(selectPosition + pos);
        Device item =  lamps.get(mViewPager.getCurrentItem() % lamps.size());
        chose = item.getImage();
        if (item != null) {
            CacheUtils.getInstants().setCurrentDevice(item);
            configActionBar(item.getName());
        }
        mViewPager.setPageTransformer(true, new HeadViewPagerTransformer());
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                // TODO Auto-generated method stub
                currentPosition = arg0;
                Device item =  lamps.get(arg0 % lamps.size());
                chose = item.getImage();
                if (item != null) {
                    CacheUtils.getInstants().setCurrentDevice(item);
                    configActionBar(item.getName());
                }

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });

    }

    @OnClick({R.id.img_voice})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_voice:
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

    public void cancalWaveAnimation() {
        isShowing = false;
        wave1.clearAnimation();
        wave2.clearAnimation();
        wave3.clearAnimation();
    }

    public void openDevice() {
        Map<String, String> map = new HashMap<>();
        map.put("id", CacheUtils.getInstants().getCurrentDevice().getId().toString());
        new LCaseApiClient().openDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
            }
        });
    }

    public void closeDevice() {
        Map<String, String> map = new HashMap<>();
        map.put("id", CacheUtils.getInstants().getCurrentDevice().getId().toString());
        new LCaseApiClient().closeDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
            }
        });
    }

    List<Group> mData = new ArrayList<>();

    private void groupList() {
        new LCaseApiClient().groupList().enqueue(new Callback<ReturnVo<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Group>>> call, Response<ReturnVo<List<Group>>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess() && response.body().getData() != null && response.body().getData().size() != 0) {
                        mData = response.body().getData();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<Group>>> call, Throwable t) {
                showNetError();
            }
        });
    }

    private void setActionBar() {
        setAddImageResource(R.mipmap.more);
        if (CacheUtils.getInstants().getCurrentUser() != null) {
            if (!CacheUtils.getInstants().getCurrentUser().isMainaccount()) {
                setAddViewVisible(View.GONE);
            } else {
                setAddViewVisible(View.VISIBLE);
            }
        } else {
            setAddViewVisible(View.VISIBLE);
        }

        setAddClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        groupList();
    }

    private void showPopupWindow() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.window1, null);
        final PopupWindow window = new PopupWindow(view, (int) (ScreenUtil.dp2px(this, 138f)), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.setTouchable(true);
        window.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        window.setBackgroundDrawable(new BitmapDrawable());
        window.showAsDropDown(getSupportActionBar().getCustomView().findViewById(R.id.layout_add));
        //重命名
        view.findViewById(R.id.rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                if (CacheUtils.getInstants().getCurrentDevice().getName().startsWith("未命名")) {
                    findDevice(CacheUtils.getInstants().getCurrentDevice());
                } else {
                    showRenameDialog();
                }
            }
        });
        //更改分组
        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                if (CacheUtils.getInstants().getCurrentDevice().getName().startsWith("未命名")) {
                    ToastUtil.showToast(DeskLampActivity.this, "未命名的设备不可以分组");
                } else {
                    showChangeDialog();
                }
            }
        });
        //删除设备
        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                showDeleteDialog();
            }
        });
    }

    /**
     * 找到设备的位置 类似于添加设备的时候那样
     *
     * @param tempDevice
     */
    private void findDevice(final Device tempDevice) {
        if (count == 0) {
//
            showToast("请注意设备状态，确定设备位置,可为设备命名同时添加设备");
            count = 0;
            isOn = false;
            final android.os.Handler handler = new android.os.Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    //要做的事情
                    if (isOn) {
                        MyApplication.mClient.publish(BizUtil.OffCommand(tempDevice.getCode()));//关灯指令
                        isOn = false;
                    } else {
                        MyApplication.mClient.publish(BizUtil.OnCommand(tempDevice.getCode()));//开灯指令
                        isOn = true;
                    }
                    count++;
                    if (count == 3) {
                        handler.removeCallbacks(this);
                        //继续下面的操作 为设备命名
                        showRenameDialog();
                        count = 0;
                    } else {
                        handler.postDelayed(this, 3000);
                    }
                }
            };
            handler.postDelayed(runnable, 1500);
        }
    }

    /**
     * 删除
     */
    private void showDeleteDialog() {
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = View.inflate(this, R.layout.delete_dialog, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.title);
        tvTitle.setText("确定删除该设备？");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.show();
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceDelete(dialog);
            }
        });
        dialog.setContentView(view);

    }

    private void deviceDelete(final Dialog dialog) {
        Map<String, String> map = new HashMap<>();
        map.put("id", CacheUtils.getInstants().getCurrentDevice().getId().toString());
        new LCaseApiClient().deviceDelete(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                dialog.dismiss();
                if (response.body() != null) {
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                dialog.dismiss();
                showNetError();
            }
        });
    }

    /**
     * 更改分组
     */
    Dialog groupDialog;

    private void showChangeDialog() {
        groupDialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = View.inflate(this, R.layout.change_layout, null);
        ListView listView = (ListView) view.findViewById(R.id.list_group);
        listView.setAdapter(new GroupAdapter(mData));
        BizUtil.setListViewHeightBasedOnChildren(listView, this);
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = ScreenUtil.dp2px(this, 51) * mData.size();
        listView.setLayoutParams(layoutParams);
        WindowManager.LayoutParams attributes = groupDialog.getWindow().getAttributes();
        if (attributes.height > ScreenUtil.getScreenHeight(DeskLampActivity.this)) {
            attributes.height = ScreenUtil.getScreenHeight(DeskLampActivity.this) / 2;
        }
        groupDialog.getWindow().setAttributes(attributes);
        groupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        groupDialog.setCancelable(true);
        groupDialog.show();
        groupDialog.setContentView(view);
    }

    /**
     * 重命名
     */
    private void showRenameDialog() {
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = View.inflate(this, R.layout.rename_layout, null);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button confirm = (Button) view.findViewById(R.id.btn_confirm);
        group1 = (RadioGroup) view.findViewById(R.id.group1);
        group2 = (RadioGroup) view.findViewById(R.id.group2);
        group3 = (RadioGroup) view.findViewById(R.id.group3);
        group4 = (RadioGroup) view.findViewById(R.id.group4);
        group5 = (RadioGroup) view.findViewById(R.id.group5);
        view.findViewById(R.id.radio15).setEnabled(false);
//        view.findViewById(R.id.radio16).setEnabled(false);
        etGroupName = (EditText) view.findViewById(R.id.et_group_name);
        group1.setOnCheckedChangeListener(this);
        group2.setOnCheckedChangeListener(this);
        group3.setOnCheckedChangeListener(this);
        group4.setOnCheckedChangeListener(this);
        group5.setOnCheckedChangeListener(this);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDevice(dialog, etGroupName.getText().toString().trim());
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.show();
        dialog.setContentView(view);
    }

    private void updateDevice(final Dialog dialog, final String name) {
        Device device = CacheUtils.getInstants().getCurrentDevice();
        device.setName(name);
        device.setImage(checkedPosition);
        new LCaseApiClient().updateDevice(device).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                dialog.dismiss();
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        lamps.get(currentPosition % lamps.size()).setImage(checkedPosition);
                        lamps.get(currentPosition % lamps.size()).setName(DEVICE_NAME[checkedPosition]);
//                        adapter.notifyDataSetChanged();
                        viewPagerAdapter.notifyDataSetChanged();
                        configActionBar(name);
                    }
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                dialog.dismiss();
                showNetError();
            }
        });
    }

    private Boolean changeGroup = false;

    /**
     * 创建设备名称
     *
     * @param position
     * @return
     */
    public String createDeviceName(int position) {
        Set<String> set = new HashSet<>();
        String name = LCaseConstants.DEVICE_NAME[checkedPosition];
        NumberText nt = NumberText.getInstance(NumberText.Lang.ChineseSimplified);
        for (Device device : lamps) {
            if (device.getImage() == position) {
                set.add(device.getName());
            }
        }
        for (int j = 1; j < set.size() + 1; j++) {
            if (!set.contains(name + nt.getText(j + 1))) {
                name = LCaseConstants.DEVICE_NAME[checkedPosition] + nt.getText(j + 1);
                break;
            }
        }
        return name;
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == group1 && changeGroup == false) {
            changeGroup = true;
            group2.clearCheck();
            group3.clearCheck();
            group4.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio1:
                    checkedPosition = 0;
                    break;
                case R.id.radio2:
                    checkedPosition = 1;
                    break;
                case R.id.radio3:
                    checkedPosition = 2;
                    break;
                case R.id.radio4:
                    checkedPosition = 3;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group2 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group3.clearCheck();
            group4.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio5:
                    checkedPosition = 4;
                    break;
                case R.id.radio6:
                    checkedPosition = 5;
                    break;
                case R.id.radio7:
                    checkedPosition = 6;
                    break;
                case R.id.radio8:
                    checkedPosition = 7;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group3 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group2.clearCheck();
            group4.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio9:
                    checkedPosition = 8;
                    break;
                case R.id.radio10:
                    checkedPosition = 9;
                    break;
                case R.id.radio11:
                    checkedPosition = 10;
                    break;
                case R.id.radio12:
                    checkedPosition = 11;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group4 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group2.clearCheck();
            group3.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio13:
                    checkedPosition = 12;
                    break;
                case R.id.radio14:
                    checkedPosition = 13;
                    break;
                case R.id.radio15:
                    checkedPosition = 14;
                    break;
                case R.id.radio16:
                    checkedPosition = 15;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group5 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group2.clearCheck();
            group3.clearCheck();
            group4.clearCheck();
            switch (checkedId) {
                case R.id.radio17:
                    checkedPosition = 16;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
    }

    class GroupAdapter extends BaseAdapter {
        List<Group> list;

        public GroupAdapter(List<Group> list) {
            this.list = list;
        }

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(DeskLampActivity.this, R.layout.item_group_list, null);
                holder = new ViewHolder();
                holder.btn = (Button) convertView.findViewById(R.id.btn);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.btn.setText(list.get(position).getGroupname());
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDeviceGroup(position);
                }
            });
            return convertView;
        }

        class ViewHolder {
            Button btn;
        }
    }

    private void updateDeviceGroup(int position) {
        Map<String, String> map = new HashMap<>();
        map.put("id", CacheUtils.getInstants().getCurrentDevice().getId().toString());
        map.put("groupid", mData.get(position).getId().toString());
        new LCaseApiClient().updateDeviceGroup(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        groupDialog.dismiss();
                    }
                    if (response.body().getMessage() != null)
                        showToast(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
            }
        });
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
