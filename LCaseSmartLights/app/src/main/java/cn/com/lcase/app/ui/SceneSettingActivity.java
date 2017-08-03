package cn.com.lcase.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.ScreenUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SceneSettingActivity extends BaseActivity {

    @BindView(R.id.listView)
    SwipeMenuListView listView;
    List<SceneDetail> mList = new ArrayList<>();
    MyAdapter adapter;
    @BindView(R.id.img_scene)
    ImageView imgScene;
    @BindView(R.id.name_scene)
    TextView nameScene;
    boolean isFirst = true;
    private Scene scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_setting);
        ButterKnife.bind(this);
        setActionBar();
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirst) {
            queryData();
        }
    }

    private void initData() {
        scene = CacheUtils.getInstants().getCurrentScene();
        imgScene.setImageResource(LCaseConstants.SCENE_IMG[Integer.parseInt(scene.getImage())]);
        nameScene.setText(scene.getScenename());
        queryData();
    }

    /**
     * 查询场景设备列表
     */
    private void queryData() {
        isFirst = false;
        Map<String, String> map = new HashMap<>();
        map.put("sid", scene.getSid().toString());
        if (scene == null) return;
        new LCaseApiClient().querySceneDetail(map).enqueue(new Callback<ReturnVo<List<SceneDetail>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<SceneDetail>>> call, Response<ReturnVo<List<SceneDetail>>> response) {
                if (response.body() != null) {
                    ReturnVo<List<SceneDetail>> body = response.body();
                    if (body.isSuccess() && body.getData() != null && body.getData().size() != 0) {
                        mList.clear();
                        mList.addAll(body.getData());
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<SceneDetail>>> call, Throwable t) {
                showNetError();
            }
        });
    }

    private void initView() {
        adapter = new MyAdapter(this, mList);
        listView.setAdapter(adapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(255, 0,
                        0)));
                // set item width
                openItem.setWidth(ScreenUtil.dp2px(SceneSettingActivity.this, 80f));
                // set item title
                openItem.setTitle("删除");
                // set item title fontsize
                openItem.setTitleSize(15);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);
            }
        };
        listView.setMenuCreator(creator);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        deleteDevice(position);
                        break;
                }
                return false;
            }
        });

    }

    private void setActionBar() {
        configActionBar("设置");
        setSaveText("添加设备");
        setSaveViewVisible(View.VISIBLE);
        setSaveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CacheUtils.getInstants().setCurrentDevices(mList);
                startActivity(new Intent(SceneSettingActivity.this, AddEquipmentCombinationActivity.class));
            }
        });
    }

    @OnClick(R.id.btn_logout)
    public void onClick() {
        if (mList.size() == 0) return;
        updateDeviceStatus();
    }

    /**
     * 删除场景中的设备
     * @param position
     */
    private void deleteDevice(final int position) {
        Map<String, Integer> map = new HashMap<>();
        map.put("sceneid", mList.get(position).getSceneid());
        map.put("deviceid", mList.get(position).getDeviceid());
        new LCaseApiClient().deleteSceneDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        mList.remove(position);
                        adapter.notifyDataSetChanged();
                    }
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
     * 保存场景设置
     */
    private void updateDeviceStatus() {
//        List<SceneDetail> list = new ArrayList<>();
//        for (SceneDetail device : mList) {
//            SceneDetail sceneDetail = new SceneDetail();
//            sceneDetail.setOnoff(device.isOnoff());
//            sceneDetail.setId(device.getId());
//            list.add(sceneDetail);
//        }
        new LCaseApiClient().updateDeviceStatus(mList).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        finish();
                    }
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


    public class MyAdapter extends BaseAdapter {
        Context mContext;
        List<SceneDetail> mList;

        public MyAdapter(Context mContext, List<SceneDetail> mList) {
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
            final ViewHolder holder;
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
            final SceneDetail item = mList.get(position);
            holder.ch.setChecked(item.isOnoff());
            if (position == mList.size() - 1) {
                holder.line.setVisibility(View.VISIBLE);
            } else {
                holder.line.setVisibility(View.GONE);
            }
            holder.ch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isOnoff()) {
                        item.setOnoff(false);
                    } else {
                        item.setOnoff(true);
                    }
                    notifyDataSetChanged();
                }
            });
            holder.img.setImageResource(LCaseConstants.DEVICE_IMG[item.getImage()]);
            if (item.getName() != null) holder.tvName.setText(item.getName());
            if (item.getGroupname() != null)
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

}
