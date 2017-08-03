package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.lcase.app.R;
import cn.com.lcase.app.adapter.PermissionExpandableAdapter;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 2016/10/19.
 */

public class PublicPermissionsFragment extends Fragment{

    private ExpandableListView mListView;
    PermissionExpandableAdapter adapter;
    List<Group> data = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.public_layout,null);
        mListView = (ExpandableListView) view.findViewById(R.id.m_listview);
        mListView.setGroupIndicator(null);
        adapter = new PermissionExpandableAdapter(data,getActivity(),0);
        mListView.setAdapter(adapter);
        initData();
        return view;
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
                            setData(body.getData());
                    } else {
                        Toast.makeText(getActivity(), body.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<Group>>> call, Throwable t) {
                Toast.makeText(getActivity(), "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
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
