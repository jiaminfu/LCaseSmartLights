package cn.com.lcase.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.dalong.francyconverflow.FancyCoverFlow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.adapter.MyFancyCoverFlowAdapter;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SceneActivity extends BaseActivity {

    @BindView(R.id.fancyCoverFlow)
    FancyCoverFlow fancyCoverFlow;
    int currentPosition;
    private Boolean isfirst=true;
    private  ArrayList<Scene> data;
    private MyFancyCoverFlowAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        ButterKnife.bind(this);
        setActionBar();
        initData();
        EventBus.getDefault().register(this);
        MyApplication.busAction = "SceneActivity";
    }
    /**
     * EventBus 回调方法必须重写
     *
     * @param msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMessage msg) {
        switch (msg.getAction()) {
            case "SceneActivity":
                switch (msg.getMsg()){
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
                        logD("命令发送成功FF00");
                        break;
                    default:
                        logD("命令发送成功"+msg.getMsg());
                        break;
                }
                break;
        }
    }
    private void logD(String str){
        Log.d("SceneActivity",str);
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
    private void setActionBar() {
        setAddImageResource(R.mipmap.more);
        setAddClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isfirst=false;
                startActivity(new Intent(SceneActivity.this,SceneSettingActivity.class));
            }
        });
    }

    private void initData() {
        int pos = getIntent().getIntExtra("position",0);
         data = CacheUtils.getInstants().getScenes();
        adapter = new MyFancyCoverFlowAdapter(this, data,3,2);
        fancyCoverFlow.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        fancyCoverFlow.setUnselectedAlpha(0.5f);//通明度
        fancyCoverFlow.setUnselectedSaturation(1);//设置选中的饱和度
        fancyCoverFlow.setUnselectedScale(0.5f);//设置选中的规模
        fancyCoverFlow.setSpacing(10);//设置间距
        fancyCoverFlow.setMaxRotation(0);//设置最大旋转
        fancyCoverFlow.setScaleDownGravity(0.6f);
        fancyCoverFlow.setCallbackDuringFling(false);
        fancyCoverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
        int num = Integer.MAX_VALUE / 2 % data.size();
        int selectPosition = Integer.MAX_VALUE / 2 - num;
        fancyCoverFlow.setSelection(selectPosition + pos);
        fancyCoverFlow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPosition = position;
                Scene item = (Scene) adapter.getItem(position % data.size());
                CacheUtils.getInstants().setCurrentScene(item);
                if (item != null){
                    configActionBar(item.getScenename());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        fancyCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentPosition != position) return;
                openScene((Scene) adapter.getItem(position % data.size()));
            }
        });
    }
    private void openScene(Scene item ) {
        if (item.getSceneDetail()==null) {
            return;
        }
        groupCommand(item.getSceneDetail());
        Map<String, String> map = new HashMap<>();
        map.put("sceneid", item.getSid().toString());
        new LCaseApiClient().openScene(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    ReturnVo<Object> body = response.body();
                   if (body.getMessage() != null )showToast(body.getMessage());
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
     */
    private void groupCommand(List<SceneDetail> list) {
        for (int i = 0; i < list.size(); i++) {
            SceneDetail device = list.get(i);
            if (!TextUtils.isEmpty(device.getCode())) {
                if ("1".equals(device.getType())) {
                    if (device.isOnoff()) {
                        MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));
                    } else {
                        MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));
                    }
                } else {
                    if (device.isOnoff()) {
                        //         电视机的开
                        MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000001", "0000"));
                    } else {
                        //         电视机的关
                        MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000000", "0000"));
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        if (!isfirst) {
            new LCaseApiClient().sceneList().enqueue(new Callback<ReturnVo<List<Scene>>>() {
                @Override
                public void onResponse(Call<ReturnVo<List<Scene>>> call, Response<ReturnVo<List<Scene>>> response) {
                    if (response.body() != null) {
                        if (response.body().isSuccess()) {
                            if (response.body().getData() != null && response.body().getData().size() > 0) {
                                data.clear();
                                data.addAll(response.body().getData());
                                CacheUtils.getInstants().setScenes((ArrayList<Scene>) data);
                                MyApplication.setListScene(response.body().getData());
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            if (response.body().getMessage() != null)
                                showToast(response.body().getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ReturnVo<List<Scene>>> call, Throwable t) {
                    showNetError();
                }
            });
        }
        super.onResume();
    }
}
