package cn.com.lcase.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dalong.francyconverflow.FancyCoverFlow;
import com.dalong.francyconverflow.FancyCoverFlowAdapter;

import java.util.List;

import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Group;
import cn.com.lcase.app.net.LCaseConstants;


public class GroupFancyCoverFlowAdapter extends FancyCoverFlowAdapter {
    private Context mContext;

    public List<Group> list;
    int count;

    public GroupFancyCoverFlowAdapter(Context context, List<Group> list, int count) {
        mContext = context;
        this.list = list;
        this.count = count;
    }

    @Override
    public View getCoverFlowItem(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.group_item, null);
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            convertView.setLayoutParams(new FancyCoverFlow.LayoutParams(width / count, FancyCoverFlow.LayoutParams.WRAP_CONTENT));
            holder = new ViewHolder();
            holder.imgDesk = (ImageView) convertView.findViewById(R.id.icon);
            holder.tvName = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int pos = position % list.size();
        holder.imgDesk.setImageResource(LCaseConstants.GROUP_IMG[list.get(pos).getGroupimage()]);
        holder.tvName.setText(list.get(position % list.size()).getGroupname());
        return convertView;
    }


    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object getItem(int i) {
        return list.get(i % list.size());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    static class ViewHolder {
        ImageView imgDesk;
        TextView tvName;
    }
}
