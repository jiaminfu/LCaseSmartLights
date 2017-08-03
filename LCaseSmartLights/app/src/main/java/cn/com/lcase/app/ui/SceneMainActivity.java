package cn.com.lcase.app.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

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
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.NumberText;
import cn.com.lcase.app.utils.ScreenUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SceneMainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.mliseview)
    SwipeMenuListView mliseview;
    private List<Scene> data = new ArrayList<>();
    private MyAdapter adapter;
    private int checkedPosition = -1;
    private RadioGroup group1;
    private RadioGroup group2;
    private RadioGroup group3;
    EditText etGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_main);
        ButterKnife.bind(this);
        setActionBar();
        initView();
        EventBus.getDefault().register(this);
        MyApplication.busAction = "SceneMainActivity";
    }

    /**
     * EventBus 回调方法必须重写
     *
     * @param msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBusMessage msg) {
        switch (msg.getAction()) {
            case "SceneMainActivity":
                switch (msg.getMsg()) {
                    case "FF01":
                        Log.d("SceneMainActivity", "命令错误");
                        break;
                    case "FF02":
                        Log.d("SceneMainActivity", "SDID错误");
                        break;
                    case "FF03":
                        Log.d("SceneMainActivity", "指令消息错误");
                        break;
                    case "FF04":
                        Log.d("SceneMainActivity", "子设备类型错误");
                        break;
                    case "FFFF":
                        Log.d("SceneMainActivity", "未知错误");
                        break;
                    case "FF00":
                        Log.d("SceneMainActivity", "FF00命令发送成功");
                        break;
                    default:
                        Log.d("SceneMainActivity", "命令发送成功" + msg.getMsg());
                        break;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        initData();
        super.onResume();
    }

    private void initView() {
        adapter = new MyAdapter();
        mliseview.setAdapter(adapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                openItem.setBackground(new ColorDrawable(Color.rgb(255, 0,
                        0)));
                openItem.setWidth(ScreenUtil.dp2px(SceneMainActivity.this, 80f));
                openItem.setTitle("删除");
                openItem.setTitleSize(15);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);

            }
        };

        mliseview.setMenuCreator(creator);
        mliseview.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mliseview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        deleteScene(position);
                        break;
                }
                return false;
            }
        });
        mliseview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CacheUtils.getInstants().setCurrentScene(data.get(position));
                Intent intent = new Intent(SceneMainActivity.this, SceneActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    private void deleteScene(final int position) {
        Scene scene = data.get(position);
        Map<String, Integer> map = new HashMap<>();
        map.put("id", scene.getSid());
        new LCaseApiClient().deleteScene(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    ReturnVo<Object> body = response.body();
                    if (body.isSuccess()) {
                        data.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                    if (body.getMessage() != null) showToast(body.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
            }
        });
    }

    private void initData() {

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

    private void setActionBar() {
        configActionBar("场景");
        setAddImageResource(R.mipmap.add);
        setAddClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSceneDialog();
            }
        });
    }

    /**
     * 添加场景
     */
    private void showAddSceneDialog() {
        checkedPosition = -1;
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = View.inflate(this, R.layout.add_scene_layout, null);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button confirm = (Button) view.findViewById(R.id.btn_confirm);
        etGroupName = (EditText) view.findViewById(R.id.et_group_name);
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
                addScene(dialog, etGroupName.getText().toString().trim());
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.show();
        dialog.setContentView(view);
    }

    /**
     * 创建分组名称
     *
     * @param position
     * @return
     */
    public String createSceneName(int position) {
        Set<String> set = new HashSet<>();
        String name = LCaseConstants.SCENE_NAME[checkedPosition];
        NumberText nt = NumberText.getInstance(NumberText.Lang.ChineseSimplified);
        for (Scene scene : data) {
            if (Integer.parseInt(scene.getImage()) == position) {
                set.add(scene.getScenename());
            }
        }
        for (int j = 1; j < set.size() + 1; j++) {
            if (!set.contains(name + nt.getText(j + 1))) {
                name = LCaseConstants.SCENE_NAME[checkedPosition] + nt.getText(j + 1);
                break;
            }
        }
        return name;
    }

    /**
     * 添加场景
     *
     * @param dialog
     * @param name
     */
    private void addScene(final Dialog dialog, String name) {
        if (checkedPosition == -1) {
            showToast("请选择场景");
            return;
        }
        final Scene scene = new Scene();
        scene.setScenename(name);
        scene.setImage(checkedPosition + "");
        new LCaseApiClient().addScene(scene).enqueue(new Callback<ReturnVo<Scene>>() {
            @Override
            public void onResponse(Call<ReturnVo<Scene>> call, Response<ReturnVo<Scene>> response) {
                dialog.dismiss();
                if (response.body() != null) {
                    ReturnVo<Scene> body = response.body();
                    if (body.isSuccess() && body.getData() != null) {
                        data.add(body.getData());
                        CacheUtils.getInstants().setScenes((ArrayList<Scene>) data);
                        adapter.notifyDataSetChanged();
                    }
                    if (body.getMessage() != null) showToast(body.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Scene>> call, Throwable t) {
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
            etGroupName.setText(createSceneName(checkedPosition));
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
            etGroupName.setText(createSceneName(checkedPosition));
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
            etGroupName.setText(createSceneName(checkedPosition));
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(SceneMainActivity.this, R.layout.scene_item, null);
                holder = new ViewHolder();
                holder.img = (ImageView) convertView.findViewById(R.id.img_scene);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_scene_name);
                holder.btnStart = (Button) convertView.findViewById(R.id.btn_start);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Scene item = data.get(position);
            if (item.getImage() != null) {
                holder.img.setImageResource(LCaseConstants.SCENE_IMG[Integer.parseInt(item.getImage())]);
            }
            if (item.getScenename() != null) {
                holder.tvName.setText(item.getScenename());
            }
            holder.btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openScene(position);
                }
            });

            return convertView;
        }


        class ViewHolder {
            ImageView img;
            TextView tvName;
            Button btnStart;
        }
    }

    private void openScene(int position) {
        if (data.get(position).getSceneDetail() == null) {
            return;
        }
        groupCommand(data.get(position).getSceneDetail());
        Map<String, String> map = new HashMap<>();
        map.put("sceneid", data.get(position).getSid().toString());
        new LCaseApiClient().openScene(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    ReturnVo<Object> body = response.body();
                    showToast(body.getMessage());
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
}
