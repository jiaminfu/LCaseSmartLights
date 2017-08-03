package cn.com.lcase.app.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.ui.DeskLampActivity;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.widget.MyImageView;


public class HeadViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<Device> mList;
    private int p = 0;
    private ViewPager viewPager;
    private DeskLampActivity activity;


    public HeadViewPagerAdapter(Context context, List<Device> list, ViewPager viewPager) {
        this.mContext = context;
        this.mList = list;
        this.viewPager = viewPager;
        activity = (DeskLampActivity) mContext;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList == null ? 0 : Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        //Log.d("remove", mImageViews[position].hashCode() + "");
//        container.removeView(mList.get(position%mList.size()));
        View view = (View) object;
        ((ViewPager) container).removeView(view);
    }

    //���View
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        // TODO Auto-generated method stub
        final MyImageView myImageView = new MyImageView(mContext);
        final Device device = mList.get(position % mList.size());
        if (device.isOnoff()) {
            myImageView.setImage(R.mipmap.da);
        } else {
            myImageView.setImage(R.mipmap.zhong);
        }
//        myImageView.setText(device.getName());
        container.addView(myImageView);
        myImageView.getmImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == viewPager.getCurrentItem()) {
                    if (device.isOnoff()) {
                        MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));//关灯指令
                        activity.closeDevice();
                        device.setOnoff(false);
                        myImageView.setImage(R.mipmap.zhong);
                    } else {
                        MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));//开灯指令
                        activity.openDevice();
                        device.setOnoff(true);
                        myImageView.setImage(R.mipmap.da);
                    }
                }

                notifyDataSetChanged();
            }
        });
        return myImageView;
    }

}
