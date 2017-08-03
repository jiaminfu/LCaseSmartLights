package cn.com.lcase.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 2016/10/22.
 */

public class MyAdapter extends BaseAdapter {
    Context mContext;
    List<Device> mList;

    public MyAdapter(Context mContext, List<Device> mList) {
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
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_scene_set, null);
            holder = new ViewHolder();
            holder.ch = (ToggleButton) convertView.findViewById(R.id.child_check);
            holder.img = (ImageView) convertView.findViewById(R.id.img_id);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tvFenZu = (TextView) convertView.findViewById(R.id.tv_fenzu);
            holder.line = convertView.findViewById(R.id.line);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final Device item = mList.get(position);
            holder.ch.setChecked(item.isOnoff());
        holder.ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isOnoff()) {
                    closeDevice(item);
                } else {
                    openDevice(item);
                }
            }
        });
        if(position == mList.size()-1){
            holder.line.setVisibility(View.VISIBLE);
        }else{
            holder.line.setVisibility(View.GONE);
        }
        holder.img.setImageResource(LCaseConstants.DEVICE_IMG[item.getImage()]);
        holder.tvName.setText(item.getName());
        holder.tvFenZu.setText("【" + item.getGroupname() + "】");
        return convertView;
    }
    class ViewHolder {
        ToggleButton ch ;
        ImageView img  ;
        TextView tvName ;
        TextView tvFenZu ;
        View line;

    }
    /**
     * 开启设备
     *
     * @param item
     */
    private void openDevice(final Device item) {
        Map<String, String> map = new HashMap<>();
        map.put("id", item.getId().toString());
        new LCaseApiClient().openDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()){
                        item.setOnoff(!item.isOnoff());
                    }else{
                        item.setOnoff(item.isOnoff());
                    }
                    notifyDataSetChanged();
                    if (response.body().getMessage() != null) {
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                Toast.makeText(mContext, "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });
    }

    /**
     * 关闭设备
     *
     * @param item
     */
    private void closeDevice(final Device item) {
        Map<String, String> map = new HashMap<>();
        map.put("id", item.getId().toString());
        new LCaseApiClient().closeDevice(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()){
                        item.setOnoff(!item.isOnoff());
                    }else{
                        item.setOnoff(item.isOnoff());
                    }
                    notifyDataSetChanged();
                    if (response.body().getMessage() != null) {
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                item.setOnoff(item.isOnoff());
                notifyDataSetChanged();
                Toast.makeText(mContext, "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
