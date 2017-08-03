package cn.com.lcase.app.ui;

import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.ScreenUtil;
import cn.com.lcase.app.utils.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelevisionActivity extends BaseActivity {

    @BindView(R.id.fanhui_imageView)
    ImageView fanhuiImageView;
    @BindView(R.id.kaiguan_imageView)
    ImageView kaiguanImageView;
    @BindView(R.id.jingyin_imageView)
    ImageView jingyinImageView;
    @BindView(R.id.jia_voice)
    ImageView jiaVoice;
    @BindView(R.id.jian_voice)
    ImageView jianVoice;
    @BindView(R.id.up_channel)
    ImageView upChannel;
    @BindView(R.id.down_channel)
    ImageView downChannel;
    @BindView(R.id.ll_top)
    LinearLayout llTop;
    @BindView(R.id.view1)
    View view1;
    @BindView(R.id.img_shang)
    ImageView imgShang;
    @BindView(R.id.img_xia)
    ImageView imgXia;
    @BindView(R.id.img_you)
    ImageView imgYou;
    @BindView(R.id.img_zuo)
    ImageView imgZuo;
    @BindView(R.id.img_ok)
    ImageView imgOk;
    @BindView(R.id.wave1)
    ImageView wave1;
    @BindView(R.id.wave2)
    ImageView wave2;
    @BindView(R.id.wave3)
    ImageView wave3;
    @BindView(R.id.img_voice)
    ImageView imgVoice;
    @BindView(R.id.activity_television)
    RelativeLayout activityTelevision;
    @BindView(R.id.zhuye_iv)
    ImageView zhuye_iv;

    private Device device;
    private boolean isShowing = false;
    private AnimationSet aniSet, aniSet2, aniSet3;
    private static final int ANIMATIONEACHOFFSET = 300;
    private List<Group> mData = new ArrayList<>();
    private Boolean isMute = false;//是否静音

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_television);
        ButterKnife.bind(this);
        setActionBar();
        configActionBar("电视");
        aniSet = getNewAnimationSet();
        aniSet2 = getNewAnimationSet();
        aniSet3 = getNewAnimationSet();
        device = (Device) getIntent().getSerializableExtra("device");
        EventBus.getDefault().register(this);
        MyApplication.busAction = "TelevisionActivity";
        groupList();
        MyApplication.voiceUtil.setWakeUpAnimation(new VoiceFinishListener() {
            @Override
            public void onfinish() {
                cancalWaveAnimation();
            }
        }, this);
    }

    /**
     * 分组列表
     */
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

    /**
     * EventBus 回调方法必须重写
     *
     * @param msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMessage msg) {
        switch (msg.getAction()) {
            case "TelevisionActivity":
                switch (msg.getMsg()) {
                    case "FF01":
                        logD("命令错误");
                        break;
                    case "FF02":
                        logD("SDID错误");
                        break;
                    case "FF03":
                        logD("指令消息错误");
                        break;
                    case "FF04":
                        logD("子设备类型错误");
                        break;
                    case "FFFF":
                        logD("未知错误");
                        break;
                    case "FF00":
                        logD("FF00 发送成功");
                        break;
                    default:
                        logD("命令发送成功");
                        isOffLine(msg.getMsg());
                        break;
                }
                break;
        }
    }

    private void logD(String str) {
        Log.d("TelevisionActivity", str);
    }

    /**
     * 如果设备离线那么就发不出命令
     */
    private void isOffLine(String msg) {
//        电视离线10C49032BBB95B40E8A5F08F51FC8CD9AF00
//       开关在线1071F75C4437FA3263D8CB33188008FB7C01
        if (msg.startsWith("10")) {
            ToastUtil.showDialog(TelevisionActivity.this, device.getName() + "已下线");
        }

    }

    @OnClick({R.id.fanhui_imageView, R.id.kaiguan_imageView, R.id.jingyin_imageView, R.id.jia_voice, R.id.jian_voice, R.id.up_channel, R.id.down_channel, R.id.ll_top, R.id.img_shang, R.id.img_xia, R.id.img_you, R.id.img_zuo, R.id.img_ok, R.id.wave1, R.id.wave2, R.id.wave3, R.id.img_voice, R.id.activity_television, R.id.zhuye_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fanhui_imageView:
//                返回
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000020", "000020", "0000"));
                break;
            case R.id.kaiguan_imageView:
//                开关
                if (device.isOnoff()) {
                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000000", "0000"));
                    device.setOnoff(false);
                } else {
                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000001", "0000"));
                    device.setOnoff(true);
                }

                break;
            case R.id.jingyin_imageView:
                if (isMute) {
                    //                不是静音
                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000040", "000000", "0000"));
                    isMute=false;
                } else {
                    //                静音
                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000040", "000040", "0000"));
                    isMute=true;
                }

                break;
            case R.id.jia_voice:
//                加音量
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000004", "000004", "0000"));

                break;
            case R.id.jian_voice:
                //                减音量
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000004", "000000", "0000"));
                break;
            case R.id.up_channel:
                //                上频道
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000002", "000002", "0000"));
                break;
            case R.id.down_channel:
                //                下频道
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000002", "000000", "0000"));
                break;
            case R.id.ll_top:
                break;
            case R.id.img_shang:
                //                上频道
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000002", "000002", "0000"));
                break;
            case R.id.img_xia:
                //                下频道
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000002", "000000", "0000"));
                break;
            case R.id.img_you:
                //                加音量
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000004", "000004", "0000"));
                break;
            case R.id.img_zuo:
                //                减音量
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000004", "000000", "0000"));
                break;
            case R.id.img_ok:
                //                确认
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000800", "000000", "0000"));
                break;
            case R.id.wave1:
                break;
            case R.id.wave2:
                break;
            case R.id.wave3:
                break;
            case R.id.img_voice:
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
                break;
            case R.id.activity_television:
                break;
            case R.id.zhuye_iv:
                //                主页
                MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000008", "000008", "0000"));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setActionBar() {
        setAddImageResource(R.mipmap.more);
        if (!CacheUtils.getInstants().getCurrentUser().isMainaccount()) {
            setAddViewVisible(View.GONE);
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

    private void showPopupWindow() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.window2, null);
        final PopupWindow window = new PopupWindow(view, (int) (ScreenUtil.dp2px(this, 138f)), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.setTouchable(true);
        window.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        window.setBackgroundDrawable(new BitmapDrawable());
        window.showAsDropDown(getSupportActionBar().getCustomView().findViewById(R.id.layout_add));
        //更改分组
        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                showChangeDialog();
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
        map.put("id", device.getId().toString());
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
        listView.setAdapter(new TelevisionActivity.GroupAdapter(mData));
        BizUtil.setListViewHeightBasedOnChildren(listView, this);
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        layoutParams.height = ScreenUtil.dp2px(this, 51) * mData.size();
        listView.setLayoutParams(layoutParams);
        WindowManager.LayoutParams attributes = groupDialog.getWindow().getAttributes();
        if (attributes.height > ScreenUtil.getScreenHeight(TelevisionActivity.this)) {
            attributes.height = ScreenUtil.getScreenHeight(TelevisionActivity.this) / 2;
        }
        groupDialog.getWindow().setAttributes(attributes);
        groupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        groupDialog.setCancelable(true);
        groupDialog.show();
        groupDialog.setContentView(view);
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
                convertView = View.inflate(TelevisionActivity.this, R.layout.item_group_list, null);
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
        map.put("id", device.getId().toString());
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

    public void showAnimation() {
        showWaveAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancalWaveAnimation();
    }
}
