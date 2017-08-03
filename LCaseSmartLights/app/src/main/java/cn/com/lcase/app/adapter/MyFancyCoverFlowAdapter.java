package cn.com.lcase.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dalong.francyconverflow.FancyCoverFlow;
import com.dalong.francyconverflow.FancyCoverFlowAdapter;

import java.util.List;

import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.net.LCaseConstants;


public class MyFancyCoverFlowAdapter<T> extends FancyCoverFlowAdapter {
    private Context mContext;
    public List<T> list;
    int count;
    int type;

    public MyFancyCoverFlowAdapter(Context context, List<T> list,int count,int type) {
        mContext = context;
        this.list = list;
        this.count = count;
        this.type = type;
    }

    @Override
    public View getCoverFlowItem(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_desk_lamp, null);
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            convertView.setLayoutParams(new FancyCoverFlow.LayoutParams(width / count, FancyCoverFlow.LayoutParams.WRAP_CONTENT));
            holder = new ViewHolder();
            holder.imgDesk = (ImageView) convertView.findViewById(R.id.img_desk_lamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (type == 1){
            Device device= (Device) list.get(position % list.size());
            if (!device.isOnoff()) {
                holder.imgDesk.setImageResource(R.mipmap.zhong);
            }else {
                holder.imgDesk.setImageResource(R.mipmap.da);
            }

        }else{
            Scene scene = (Scene) list.get(position % list.size());
            holder.imgDesk.setImageResource(LCaseConstants.SCENE_BIG_IMG[Integer.parseInt(scene.getImage())]);
        }
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
    }
}
