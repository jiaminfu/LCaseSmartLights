package cn.com.lcase.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.com.lcase.app.R;
import cn.com.lcase.app.net.LCaseConstants;

/**
 * Created by admin on 2017/4/7.
 */

public class AddDeviceRenameAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Context context;
    private List<String> nameList;
    private int curPosition = -1;

    public int getCurPosition() {
        return curPosition;
    }

    public void setCurPosition(int curPosition) {
        this.curPosition = curPosition;
    }

    public AddDeviceRenameAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        nameList = new ArrayList<>();
        nameList.addAll(java.util.Arrays.asList(LCaseConstants.DEVICE_NAME));
    }

    @Override
    public int getCount() {
        return nameList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView != null) {
            convertView = layoutInflater.inflate(R.layout.adapter_ad_device_rename, null);
            holder = new ViewHolder();
            holder.tv = (TextView) convertView.findViewById(R.id.device_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv.setText(nameList.get(position));
        if (curPosition == position) {
            holder.tv.setTextColor(context.getResources().getColor(R.color.theme_color));
        } else {
            holder.tv.setTextColor(context.getResources().getColor(R.color.text_color));
        }
        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curPosition = position;
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    class ViewHolder {
        private TextView tv;
    }
}
