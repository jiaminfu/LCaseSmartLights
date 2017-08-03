package cn.com.lcase.app.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 2016/10/21.
 */

public class PermissionExpandableAdapter extends BaseExpandableListAdapter {

    List<Group> itemList;
    Context mContext;
    int type;

    public PermissionExpandableAdapter(List<Group> itemList, Context mContext, int type) {
        this.itemList = itemList;
        this.mContext = mContext;
        this.type = type;
    }


    @Override
    public int getGroupCount() {
        return itemList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).getDevicelist().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return itemList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemList.get(groupPosition).getDevicelist().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.group_layout_1, null);
            holder = new GroupViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.img_right);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }
        Group items = itemList.get(groupPosition);
        if (isExpanded) {
            holder.img.setImageResource(R.mipmap.shouqi);
        } else {
            holder.img.setImageResource(R.mipmap.right);
        }
        holder.tvGroupName.setText(items.getGroupname());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.child_layout_1, null);
            holder = new ChildViewHolder();
            holder.chIsCheck = (ToggleButton) convertView.findViewById(R.id.child_check);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.tv_fenzu);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.img = (ImageView) convertView.findViewById(R.id.img_device);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }
        final Device item = itemList.get(groupPosition).getDevicelist().get(childPosition);
        if (type == 1) {
            holder.chIsCheck.setChecked(item.isEnable());
        } else {
            holder.chIsCheck.setChecked(item.ispublic());
        }

        holder.chIsCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case 1:
                        //副账户设置权限
                        setPower(item);
                        break;
                    default:
                        if (item.isIspublic()) {
                            //关闭公共权限
                            setPrivate(item);
                        } else {
                            //开启公共权限
                            setPublic(item);
                        }
                        break;
                }
            }
        });
        holder.tvName.setText(item.getName());
        if (item.getGroupname() != null)
            holder.tvGroupName.setText("【" + item.getGroupname() + "】");
        if (item.getImage() != -1)
            holder.img.setImageResource(LCaseConstants.DEVICE_IMG[item.getImage()]);
        return convertView;
    }

    /**
     * 开启公共权限
     * @param item
     */
    private void setPublic(final Device item) {
        Map<String,String> map = new HashMap<>();
        map.put("deviceid",item.getId().toString());
        new LCaseApiClient().setPublic(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()){
                        item.setIspublic(!item.isIspublic());
                        notifyDataSetChanged();
                    }else{
                        item.setIspublic(item.isIspublic());
                        notifyDataSetChanged();
                    }
                    if (response.body().getMessage() != null){
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                Toast.makeText(mContext, "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 关闭公共权限
     * @param item
     */
    private void setPrivate(final Device item) {
        Map<String,String> map = new HashMap<>();
        map.put("deviceid",item.getId().toString());
        new LCaseApiClient().setPrivate(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()){
                        item.setIspublic(!item.isIspublic());
                        notifyDataSetChanged();
                    }else{
                        item.setIspublic(item.isIspublic());
                        notifyDataSetChanged();
                    }
                    if (response.body().getMessage() != null){
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                Toast.makeText(mContext, "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置副账户权限
     * @param item
     */
    private void setPower(final Device item) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceid", item.getId().toString());
        map.put("userid", CacheUtils.getInstants().getCurrentSubUser().getId().toString());
        map.put("enable", !item.isEnable());
        new LCaseApiClient().setPower(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()){
                        item.setEnable(!item.isEnable());
                        notifyDataSetChanged();
                    }else{
                        item.setEnable(item.isEnable());
                        notifyDataSetChanged();
                    }
                    if (response.body().getMessage() != null){
                        Toast.makeText(mContext, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                Toast.makeText(mContext, "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class GroupViewHolder {
        TextView tvGroupName;
        ImageView img;
    }

    class ChildViewHolder {
        ImageView img;
        TextView tvName;
        TextView tvGroupName;
        ToggleButton chIsCheck;
    }
}
