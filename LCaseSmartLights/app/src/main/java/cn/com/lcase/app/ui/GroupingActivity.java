package cn.com.lcase.app.ui;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dalong.francyconverflow.FancyCoverFlow;

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
import cn.com.lcase.app.adapter.GroupFancyCoverFlowAdapter;
import cn.com.lcase.app.adapter.MyAdapter;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.NumberText;
import cn.com.lcase.app.utils.PreferencesUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GroupingActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.fancyCoverFlow)
    FancyCoverFlow fancyCoverFlow;
    @BindView(R.id.listView)
    ListView listView;
    private GroupFancyCoverFlowAdapter adapter;
    private int checkedPosition = -1;
    private RadioGroup group1;
    private RadioGroup group2;
    private RadioGroup group3;
    private MyAdapter mAdapter;
    List<Group> data = new ArrayList<>();
    List<Device> device = new ArrayList<>();
    List<List<Device>> devices = new ArrayList<>();
    int groupPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouping);
        ButterKnife.bind(this);
        setActionBar();
        initData();
        EventBus.getDefault().register(this);
        MyApplication.busAction = "GroupingActivity";
    }

    /**
     * EventBus 回调方法必须重写
     *
     * @param msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMessage msg) {
        switch (msg.getAction()) {
            case "GroupingActivity":
                switch (msg.getMsg()) {
                    case "FF01":
                        Log.d("GroupingActivity", "命令错误");
                        break;
                    case "FF02":
                        Log.d("GroupingActivity", "SDID错误");
                        break;
                    case "FF03":
                        Log.d("GroupingActivity", "指令消息错误");
                        break;
                    case "FF04":
                        Log.d("GroupingActivity", "子设备类型错误");
                        break;
                    case "FF05":
                        Log.d("DeviceActivity", "电视不在线");
//                        只有查询电视机状态 才会返回ff05才会告知离线
                        for (int i = 0; i < devices.size(); i++) {
                            for (int j = 0; j < devices.get(i).size(); j++) {
                                Device tempD = devices.get(i).get(j);
                                if ("2".equals(tempD.getType())) {
                                    tempD.setOnoff(false);
                                    if (mAdapter == null) {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                        break;
                    case "FFFF":
                        Log.d("GroupingActivity", "未知错误");
                        break;
                    default:
                        Log.d("GroupingActivity", "命令发送成功");
                        queryTvResult(msg.getMsg());
                        changeDeviceStatueResult(msg.getMsg());
                        break;
                }
                break;
        }
    }

    /**
     * 设备状态改变后（比如app发出开或者关） 收到设备状态的命令
     *
     * @param msg
     */
    private void changeDeviceStatueResult(String msg) {

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
                int size = device.size();
                for (int i = 0; i < size; i++) {
                    Device tempD = device.get(i);
                    if (tempCode.equals(tempD.getCode()) && tempIsOn.compareTo(tempD.isOnoff()) != 0) {
                        tempD.setOnoff(tempIsOn);
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
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
                int size = device.size();
                for (int i = 0; i < size; i++) {
                    Device tempD = device.get(i);
                    if (tempCode.equals(tempD.getCode()) && tempIsOn.compareTo(tempD.isOnoff()) != 0) {
                        tempD.setOnoff(tempIsOn);
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }

    }

    private void initData() {
        Map<String, String> map = new HashMap<>();
        new LCaseApiClient().queryGroupInfo(map).enqueue(new Callback<ReturnVo<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Group>>> call, Response<ReturnVo<List<Group>>> response) {
                if (response.body() != null) {
                    ReturnVo<List<Group>> body = response.body();
                    if (body.isSuccess()) {
                        if (body.getData() != null && body.getData().size() > 0)
                            splitData(body.getData());
                    } else {
                        if (body != null) {
                            showToast(body.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<Group>>> call, Throwable t) {
                showNetError();
            }
        });
    }

    /**
     * 拆分数据
     *
     * @param data
     */
    private void splitData(List<Group> data) {
        this.data.addAll(data);
        for (int i = 0; i < data.size(); i++) {
            String groupName = data.get(i).getGroupname();
            for (Device device : data.get(i).getDevicelist()) {
                device.setGroupname(groupName);
            }
            devices.add(data.get(i).getDevicelist());
        }

        setView();
        queryTvCommand();
    }

    /**
     * 查询电视设备状态 是不是离线还是在线
     */
    private void queryTvCommand() {
        for (int i = 0; i < devices.size(); i++) {
            for (int j = 0; j < devices.get(i).size(); j++) {
                Device d = devices.get(i).get(j);
                if ("2".equals(d.getType())) {
//                08 C49032BBB95B40E8A5F08F51FC8CD9AF00
                    MyApplication.mClient.publish(BizUtil.queryCommand(d.getCode()));
                }
            }
        }
    }

    /**
     * 查询电视设备状态 的结果
     */
    private void queryTvResult(String msg) {
        if (msg.startsWith("09")) {
            String tempCode = msg.substring(2, 34);//C49032BBB95B40E8A5F08F51FC8CD9AF0000000000000000000000 电视在线
            String state = msg.substring(34, 36);//00
            Boolean tempIsOn = false;
            for (int i = 0; i < devices.size(); i++) {
                List<Device> tempList = devices.get(i);
                for (int j = 0; j < tempList.size(); j++) {
                    Device tempD = tempList.get(j);
                    if (tempCode.equals(tempD.getCode())) {
                        if ("2".equals(tempD.getType())) {
                            tempIsOn = true;
                        }
                        tempD.setOnoff(tempIsOn);
                        if (mAdapter == null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    private void setView() {
        mAdapter = new MyAdapter(this, device);
        listView.setAdapter(mAdapter);
        adapter = new GroupFancyCoverFlowAdapter(this, data, 5);
        fancyCoverFlow.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        fancyCoverFlow.setUnselectedAlpha(0.9f);//通明度
        fancyCoverFlow.setUnselectedSaturation(0.8f);//设置选中的饱和度
        fancyCoverFlow.setUnselectedScale(0.7f);//设置选中的规模
        fancyCoverFlow.setSpacing(0);//设置间距
        fancyCoverFlow.setMaxRotation(1);//设置最大旋转
        fancyCoverFlow.setScaleDownGravity(0.7f);
        fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
        int num = Integer.MAX_VALUE / 2 % data.size();
        fancyCoverFlow.setCallbackDuringFling(false);
        int selectPosition = Integer.MAX_VALUE / 2 - num;
        fancyCoverFlow.setSelection(selectPosition);
        fancyCoverFlow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //跳转页面
                int pos = position % data.size();
                groupPosition = position % data.size();
                device.clear();
                device.addAll(devices.get(pos));
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (CacheUtils.getInstants().getCurrentUser().isMainaccount()) {
            fancyCoverFlow.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    /**震动服务*/
                    Vibrator vib = (Vibrator) GroupingActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
                    vib.vibrate(100);//只震动一秒，一次
                    showLongClickDialog(position % data.size());
                    return false;
                }
            });
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) mAdapter.getItem(position);
//                CacheUtils.getInstants().setLamps(devices.get(groupPosition));
                List<Device> texmpG = devices.get(groupPosition);
                List<Device> texmpL = new ArrayList<Device>();
                if (device.getType().equals("1")) {
//                    startActivity(new Intent(GroupingActivity.this, DeskLampActivity.class));
                    for (int i = 0; i < texmpG.size(); i++) {
                        if ("1".equals(texmpG.get(i).getType())) {
                            texmpL.add(texmpG.get(i));
                        }
                    }
                    int p = position;
                    for (int i = 0; i < texmpL.size(); i++) {
                        if (device.getId() == texmpL.get(i).getId()) {
                            p = i;
                        }
                    }
                    CacheUtils.getInstants().setLamps(texmpL);
                    Intent intent = new Intent(GroupingActivity.this, DeskLampActivity.class);
                    intent.putExtra("position", p);
                    intent.putExtra("device_id", device.getId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(GroupingActivity.this, TelevisionActivity.class);
                    intent.putExtra("device", device);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 创建分组名称
     *
     * @param position
     * @return
     */
    public String createGroupName(int position) {
        Set<String> set = new HashSet<>();
        String name = LCaseConstants.GROUP_NAME[checkedPosition];
        NumberText nt = NumberText.getInstance(NumberText.Lang.ChineseSimplified);
        for (Group group : data) {
            if (group.getGroupimage() == position) {
                set.add(group.getGroupname());
            }
        }
        for (int j = 1; j < set.size() + 1; j++) {
            if (!set.contains(name + nt.getText(j + 1))) {
                name = LCaseConstants.GROUP_NAME[checkedPosition] + nt.getText(j + 1);
                break;
            }
        }
        return name;
    }

    /**
     * 分组长按
     *
     * @param position
     */
    private void showLongClickDialog(final int position) {
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle1);
        View view = View.inflate(this, R.layout.long_clock_dialog, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams a = window.getAttributes();
        a.width = window.getWindowManager().getDefaultDisplay().getWidth();
        window.setAttributes(a);
        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //修改分组
                showRenameDialog("修改分组", 2, position);
            }
        });
        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除
                showDeleteDialog(position);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消
                dialog.dismiss();
            }
        });
    }

    /**
     * 删除
     *
     * @param position
     */
    private void showDeleteDialog(final int position) {
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = View.inflate(this, R.layout.delete_dialog, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.show();
        dialog.setContentView(view);
        view.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroup(position, dialog);
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    /**
     * 删除分组
     *
     * @param position
     * @param dialog
     */
    private void deleteGroup(final int position, final Dialog dialog) {
        Group groupDelete = data.get(position);
        if (groupDelete == null) {
            return;
        }
        new LCaseApiClient().deleteGroup(groupDelete).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                dialog.dismiss();
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        data.remove(position);
                        devices.remove(position);
                        adapter.notifyDataSetChanged();
                        fancyCoverFlow.setSelection(Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % data.size()));
                        if (response.body().getMessage() != null)
                            showToast(response.body().getMessage());
                    } else {
                        if (response.body().getMessage() != null) {
                            showToast(response.body().getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
                dialog.dismiss();
            }
        });

    }

    /**
     * 设置ActionBar
     */
    private void setActionBar() {
        configActionBar("分组");
        if (CacheUtils.getInstants().getCurrentUser() != null) {
            if (!CacheUtils.getInstants().getCurrentUser().isMainaccount()) {
                setAddViewVisible(View.GONE);
            } else {
                setAddViewVisible(View.VISIBLE);
                setAddImageResource(R.mipmap.add);
                setAddClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRenameDialog("添加分组", 1, 0);
                    }
                });
            }
        } else {
            setAddViewVisible(View.VISIBLE);
            setAddImageResource(R.mipmap.add);
            setAddClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRenameDialog("添加分组", 1, 0);
                }
            });
        }

    }

    /**
     * 重命名
     */
    EditText etGroupName;

    private void showRenameDialog(String title, final int type, final int pos) {
        checkedPosition = -1;
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = View.inflate(this, R.layout.add_group_layout, null);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button confirm = (Button) view.findViewById(R.id.btn_confirm);
        etGroupName = (EditText) view.findViewById(R.id.et_group_name);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(title);
        group1 = (RadioGroup) view.findViewById(R.id.group1);
        group2 = (RadioGroup) view.findViewById(R.id.group2);
        group3 = (RadioGroup) view.findViewById(R.id.group3);
        group1.setOnCheckedChangeListener(this);
        group2.setOnCheckedChangeListener(this);
        group3.setOnCheckedChangeListener(this);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case 1://添加分组
                        saveGroup(dialog, etGroupName.getText().toString().trim());
                        break;
                    case 2://修改分组
                        updateGroup(dialog, pos, etGroupName.getText().toString().trim());
                        break;
                }
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.show();
        dialog.setContentView(view);
    }

    /**
     * 修改分组
     *
     * @param dialog
     * @param pos
     * @param name
     */

    private void updateGroup(final Dialog dialog, final int pos, final String name) {
        if (checkedPosition == -1) {
            showToast("请选择分组");
            return;
        }
        final Group groupUpDate = new Group();
        groupUpDate.setGroupname(name);
        groupUpDate.setId(data.get(pos).getId());
        groupUpDate.setGroupimage(checkedPosition);
        new LCaseApiClient().updateGroup(groupUpDate).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                dialog.dismiss();
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        data.get(pos).setGroupname(name);
                        data.get(pos).setGroupimage(checkedPosition);
                        adapter.notifyDataSetChanged();
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

    /**
     * 添加分组
     *
     * @param dialog
     * @param name
     */
    private void saveGroup(final Dialog dialog, String name) {
        if (checkedPosition == -1) {
            showToast("请选择分组");
            return;
        }
        final Group groupNew = new Group();
        groupNew.setGroupname(name);
        groupNew.setGroupimage(checkedPosition);
        groupNew.setUserid(PreferencesUtil.getUserId(this));
        new LCaseApiClient().saveGroup(groupNew).enqueue(new Callback<ReturnVo<Group>>() {
            @Override
            public void onResponse(Call<ReturnVo<Group>> call, Response<ReturnVo<Group>> response) {
                dialog.dismiss();
                if (response.body() != null) {
                    if (response.body().isSuccess() && response.body().getData() != null) {
                        data.add(response.body().getData());
                        devices.add(new ArrayList<Device>());
                        adapter.notifyDataSetChanged();
                        fancyCoverFlow.setSelection(Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % data.size()) - 1);
                    }
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Group>> call, Throwable t) {
                dialog.dismiss();
                showNetError();
            }
        });
    }

    private Boolean changeGroup = false;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == group1 && changeGroup == false) {
            changeGroup = true;
            group2.clearCheck();
            group3.clearCheck();
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
            etGroupName.setText(createGroupName(checkedPosition));
        }
        if (group == group2 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group3.clearCheck();
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
            etGroupName.setText(createGroupName(checkedPosition));

        }
        if (group == group3 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group2.clearCheck();
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
            etGroupName.setText(createGroupName(checkedPosition));
        }
    }


    @OnClick({R.id.open_all, R.id.close_all})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_all:
                //全开
                openAll();
                break;
            case R.id.close_all:
                //全关
                closeAll();
                break;
        }
    }

    /**
     * 分组设备全开
     */
    private void openAll() {
        final Group group = data.get(groupPosition);
        if (group.getDevicelist() != null && group.getDevicelist().size() == 0) {
            showToast("当前分组没有设备");
            return;
        }
        groupCommand(group, true);
        Map<String, String> map = new HashMap<>();
        map.put("id", group.getId().toString());
        new LCaseApiClient().openAll(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                 /*   if (response.body().isSuccess()) {
                        for (Device device1 : data.get(groupPosition).getDevicelist()) {
                            device1.setOnoff(true);
                        }
                        mAdapter.notifyDataSetChanged();
                    }*/
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

    /**
     * 一组命令
     *
     * @param group
     * @param isOn  是否开关
     */
    private void groupCommand(Group group, Boolean isOn) {
        List<Device> temp = group.getDevicelist();
        for (int i = 0; i < temp.size(); i++) {
            Device device = temp.get(i);
            if ("1".equals(device.getType())) {
                if (isOn) {
                    MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));
                } else {
                    MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));
                }
            } else {
//                if (isOn) {
//                     //         电视机的开
//                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "0001", "0001", "0000"));
//                } else {
//                    //         电视机的关
//                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "0000", "0001", "0000"));
//                }
            }
        }
    }

    /**
     * 分组设备全关
     */
    private void closeAll() {
        final Group group = data.get(groupPosition);
        if (group.getDevicelist() != null && group.getDevicelist().size() == 0) {
            showToast("当前分组没有设备");
            return;
        }
        groupCommand(group, false);
        Map<String, String> map = new HashMap<>();
        map.put("id", group.getId().toString());
        new LCaseApiClient().closeAll(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                  /*  if (response.body().isSuccess()) {
                        for (Device device1 : data.get(groupPosition).getDevicelist()) {
                            device1.setOnoff(false);
                        }
                        mAdapter.notifyDataSetChanged();
                    }*/
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

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
