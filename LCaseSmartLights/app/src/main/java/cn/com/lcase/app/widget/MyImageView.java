package cn.com.lcase.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.com.lcase.app.R;

public class MyImageView extends RelativeLayout {
    public ImageView getmImageView() {
        return mImageView;
    }

//    public TextView getTv() {
//        return tv;
//    }

    private ImageView mImageView;
//    private TextView tv;

    public MyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public MyImageView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        LayoutInflater.from(context).inflate(R.layout.my_image_view, this);
        mImageView = (ImageView) findViewById(R.id.myImage);
//        tv = (TextView) findViewById(R.id.text);
    }

    public void setImage(int id) {
        mImageView.setImageResource(id);
    }

//    public void setText(String str) {
//        tv.setText(str);
//    }

}
