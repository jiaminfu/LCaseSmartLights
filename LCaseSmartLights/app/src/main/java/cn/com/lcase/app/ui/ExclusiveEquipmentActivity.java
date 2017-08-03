package cn.com.lcase.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.adapter.ExclusiveEquipmentAdapter;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExclusiveEquipmentActivity extends BaseActivity {

    @BindView(R.id.m_listView)
    ExpandableListView mListView;
    private List<Group> data = new ArrayList<>();
    private ExclusiveEquipmentAdapter adapter;
    private TextView zhiling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclusive_equipment);
        ButterKnife.bind(this);
        zhiling = (TextView) findViewById(R.id.zhiling);
        setActionBar();
        initData();
        zhiling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExclusiveEquipmentActivity.this, VoiceCommandsActivity.class));
            }
        });
    }

    private void setActionBar() {
        configActionBar("专属设备");
        setSaveViewVisible(View.VISIBLE);
        setSaveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePrivateDevice();
            }
        });
    }

/*    *//**
     * 保存私有设备
     *//*
    private void savePrivateDevice() {
        List<Device> temp = new ArrayList<>();
        StringBuffer deviceIds = new StringBuffer();
        if (data.size() == 0) return;
        if (data.get(0).getDevicelist().size() == 0) return;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).getDevicelist().size(); j++) {
                if (data.get(i).getDevicelist().get(j).isUnique()) {
                    deviceIds.append(data.get(i).getDevicelist().get(j).getId().toString() + ",");
                    temp.add(data.get(i).getDevicelist().get(j));
                }
            }
        }
        setSaveData(temp);
        Map<String, String> map = new HashMap<>();
        map.put("deviceid", deviceIds.toString().length() == 0 ? "" : deviceIds.toString().substring(0, deviceIds.toString().length() - 1));
        new LCaseApiClient().savePrivateDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                    if (response.body().isSuccess()) {
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
            }
        });
    }*/

    /**
     * 保存私有设备
     */
    private void savePrivateDevice() {
        List<Device> temp = new ArrayList<>();
        StringBuffer deviceIds = new StringBuffer();
        if (data.size() == 0) return;
        if (data.get(0).getDevicelist().size() == 0) return;
        for (int i = 0; i < data.get(0).getDevicelist().size(); i++) {
            if (data.get(0).getDevicelist().get(i).isUnique()) {
                deviceIds.append(data.get(0).getDevicelist().get(i).getId().toString() + ",");
                temp.add(data.get(0).getDevicelist().get(i));
            }
        }
        setSaveData(temp);
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isChecked()) {
                setSaveData2(data.get(i).getGroupname());
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("deviceid", deviceIds.toString().length() == 0 ? "" : deviceIds.toString().substring(0, deviceIds.toString().length() - 1));
        new LCaseApiClient().savePrivateDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                    if (response.body().isSuccess()) {
                        finish();
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
     * 整理拿到专属设备的数据
     *
     * @param temp
     */
    private void setSaveData(List<Device> temp) {
//      保留选中的专属设备
        if (temp.size() > 0) {
            MyApplication.setExclusiveDeviceList(temp);
        }
    }

    /**
     * 整理拿到专属设备的数据
     *
     * @param gName 组名
     */
    private void setSaveData2(String gName) {
//        保留组名就可以

        MyApplication.groupName = gName;

    }

    /**
     * 整理拿到的数据
     *
     * @param groups
     */
    private void setData(List<Group> groups) {
//        Group group = new Group();
//        List<Device> list = new ArrayList<>();
//        group.setGroupname("全部设备");
//        for (int i = 0; i < groups.size(); i++) {
//            for (int j = 0; j < groups.get(i).getDevicelist().size(); j++) {
//                groups.get(i).getDevicelist().get(j).setGroupname(groups.get(i).getGroupname());
//            }
//            list.addAll(groups.get(i).getDevicelist());
//        }
//        data.addAll(groups);
//        group.setDevicelist(list);
//        data.add(0, group);
//        adapter.notifyDataSetChanged();
        Group group = new Group();
        List<Device> list = new ArrayList<>();
        group.setGroupname("全部设备");
        for (int i = 0; i < groups.size(); i++) {
            Group groupTemp = groups.get(i);
            if (groupTemp.getGroupname().contains("卧")) {
                for (int j = 0; j < groupTemp.getDevicelist().size(); j++) {
                    groupTemp.getDevicelist().get(j).setGroupname(groupTemp.getGroupname());
                }
                list.addAll(groups.get(i).getDevicelist());
                data.add(groupTemp);
            }
        }
        group.setDevicelist(list);
        data.add(0, group);
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        adapter = new ExclusiveEquipmentAdapter(data, ExclusiveEquipmentActivity.this);
        mListView.setAdapter(adapter);
        mListView.setGroupIndicator(null);
        new LCaseApiClient().queryPrivateDevice().enqueue(new Callback<ReturnVo<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Group>>> call, Response<ReturnVo<List<Group>>> response) {
                if (response.body() != null) {
                    ReturnVo<List<Group>> body = response.body();
                    if (body.isSuccess()) {
                        if (body.getData() != null && body.getData().size() > 0)
                            setData(body.getData());
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


}
