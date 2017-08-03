package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.adapter.MyExpandableAdapter;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEquipmentCombinationActivity extends BaseActivity {

    @BindView(R.id.mliseview)
    ExpandableListView mliseview;
    private List<Group> data = new ArrayList<>();
    private MyExpandableAdapter adapter;
    private Scene currentScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_equipment_combination);
        ButterKnife.bind(this);
        setActionBar();
        initData();
    }

    /**
     * 整理拿到的数据
     *
     * @param groups
     */
    private void setData(List<Group> groups) {
        if (groups.size() == 0) return;
        Group group = new Group();
        List<Device> list = new ArrayList<>();
        group.setGroupname("全部设备");
        List<SceneDetail> devices = CacheUtils.getInstants().getCurrentDevices();
        List<String> names = new ArrayList<>();
        for (SceneDetail device : devices) {
            names.add(device.getName());
        }
        for (int x = groups.size() - 1; x >= 0 ; x--){
            List<Device> devicelist = groups.get(x).getDevicelist();
            for (int y = devicelist.size() - 1 ; y >= 0 ; y--){
                if (names.contains(devicelist.get(y).getName())){
                    devicelist.remove(y);
                }else{
                    devicelist.get(y).setGroupname(groups.get(x).getGroupname());
                }
            }
            if (devicelist.size() == 0){
                groups.remove(x);
            }else{
                list.addAll(groups.get(x).getDevicelist());
            }
        }
        data.addAll(groups);
        group.setDevicelist(list);
        data.add(0, group);
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        currentScene = CacheUtils.getInstants().getCurrentScene();
        adapter = new MyExpandableAdapter(data, AddEquipmentCombinationActivity.this);
        mliseview.setAdapter(adapter);
        mliseview.setGroupIndicator(null);
        Map<String, String> map = new HashMap<>();
        new LCaseApiClient().queryGroupInfo(map).enqueue(new Callback<ReturnVo<List<Group>>>() {
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

    private void setActionBar() {
        configActionBar("添加设备组合");
        setSaveText("确定");
        setSaveViewVisible(View.VISIBLE);
        setSaveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sceneAddDevice();
            }
        });
    }


    public void sceneAddDevice() {
        StringBuffer deviceIds = new StringBuffer();
        if (data.size() == 0) return;
        if (data.get(0).getDevicelist().size() == 0) return;
        for (int i = 0; i < data.get(0).getDevicelist().size(); i++) {
            if (data.get(0).getDevicelist().get(i).isChecked()) {
                deviceIds.append(data.get(0).getDevicelist().get(i).getId().toString()+",");
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("sceneid", currentScene.getSid().toString());
        map.put("deviceid", deviceIds.toString().length() == 0? "":deviceIds.toString().substring(0, deviceIds.toString().length() - 1));
        new LCaseApiClient().sceneAddDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
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

}
