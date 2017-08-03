package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.adapter.PermissionExpandableAdapter;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.model.UserInfo;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeputyAccountPermissionsSetActivity extends BaseActivity {

    @BindView(R.id.m_listView)
    ExpandableListView mListView;
    UserInfo currentSubUser;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    PermissionExpandableAdapter adapter;
    List<Group> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deputy_account_permissions_set);
        configActionBar("副账户权限");
        ButterKnife.bind(this);
        mListView.setGroupIndicator(null);
        adapter = new PermissionExpandableAdapter(data,this,1);
        mListView.setAdapter(adapter);
        initData();
    }

//    private void setActionBar() {
//        setSaveText("保存");
//        setSaveViewVisible(View.VISIBLE);
//        setSaveClick(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showToast("副账户权限");
//            }
//        });
//    }


    private void initData() {
        currentSubUser = CacheUtils.getInstants().getCurrentSubUser();
        tvTitle.setText(currentSubUser.getUsername());
        Map<String, String> map = new HashMap<>();
        map.put("userid", currentSubUser.getId().toString());
        new LCaseApiClient().querySubUserDevice(map).enqueue(new Callback<ReturnVo<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<Group>>> call, Response<ReturnVo<List<Group>>> response) {
                if (response.body() != null) {
                    ReturnVo<List<Group>> body = response.body();
                    if (body.isSuccess()) {
                        if (body.getData() != null && body.getData().size() > 0)
                            setData(body.getData());
                    } else {
                        showToast(body.getMessage());
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
     * 整理拿到的数据
     *
     * @param groups
     */
    private void setData(List<Group> groups){
        Group group = new Group();
        List<Device> list = new ArrayList<>();
        group.setGroupname("全部设备");
        for (int i = 0; i < groups.size(); i++) {
            for (int j = 0 ;j<groups.get(i).getDevicelist().size() ; j++){
                groups.get(i).getDevicelist().get(j).setGroupname(groups.get(i).getGroupname());
            }
            list.addAll(groups.get(i).getDevicelist());
        }
        data.addAll(groups);
        group.setDevicelist(list);
        data.add(0, group);
        adapter.notifyDataSetChanged();
    }

}
