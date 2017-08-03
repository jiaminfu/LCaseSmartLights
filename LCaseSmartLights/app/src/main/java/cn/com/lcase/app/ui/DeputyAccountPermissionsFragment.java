package cn.com.lcase.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.com.lcase.app.R;
import cn.com.lcase.app.model.UserInfo;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 2016/10/19.
 */

public class DeputyAccountPermissionsFragment extends Fragment{
    private ListView listView;
    List<UserInfo> subUsers = new ArrayList<>();
    MyAdapter adapter ;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deputy_account_layout,null);
        listView = (ListView) view.findViewById(R.id.listView);
        adapter =  new MyAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CacheUtils.getInstants().setCurrentSubUser(subUsers.get(position));
                startActivity(new Intent(getActivity(),DeputyAccountPermissionsSetActivity.class));
            }
        });
        initData();
        return view;
    }

    private void initData() {
        new LCaseApiClient().querySubUser().enqueue(new Callback<ReturnVo<List<UserInfo>>>() {
            @Override
            public void onResponse(Call<ReturnVo<List<UserInfo>>> call, Response<ReturnVo<List<UserInfo>>> response) {
                if (response.body() != null ){
                    if (response.body().isSuccess() && response.body().getData() != null && response.body().getData().size() > 0){
                        subUsers.addAll(response.body().getData());
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<List<UserInfo>>> call, Throwable t) {
                Toast.makeText(getActivity(), "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
            }
        });
    }



    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return subUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return subUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = View.inflate(getContext(), R.layout.item_fuzhanghu,null);
            TextView name = (TextView) convertView.findViewById(R.id.tv_title);
            name.setText(subUsers.get(position).getUsername());
            return convertView;
        }
    }
}
