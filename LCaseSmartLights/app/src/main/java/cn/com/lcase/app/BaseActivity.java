package cn.com.lcase.app;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.lcase.app.ui.LoginActivity;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.NetUtil;
import cn.com.lcase.app.utils.ScreenUtil;
import cn.com.lcase.app.utils.ToastUtil;

/**
 * Created by admin on 2016/10/17.
 */

public class BaseActivity extends AppCompatActivity {


    private ActionBar actionBar;
    private RelativeLayout layoutBack;
    private TextView tvTitle;
    private TextView tvSave;
    private RelativeLayout layoutAdd;
    private RelativeLayout layoutSave;
    private ImageView imgAdd;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = View.inflate(this, R.layout.progress_layout, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(view);
        actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.action_bar_register);
        tvTitle = (TextView) actionBar.getCustomView().findViewById(R.id.tv_title);
        tvSave = (TextView) actionBar.getCustomView().findViewById(R.id.save);
        layoutBack = (RelativeLayout) actionBar.getCustomView().findViewById(R.id.layout_back);
        layoutAdd = (RelativeLayout) actionBar.getCustomView().findViewById(R.id.layout_add);
        layoutSave = (RelativeLayout) actionBar.getCustomView().findViewById(R.id.layout_save);
        imgAdd = (ImageView) actionBar.getCustomView().findViewById(R.id.add);
    }

    public void configActionBar(String title) {
        tvTitle.setText(title);
        layoutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setAddClick(View.OnClickListener listener) {
        layoutAdd.setOnClickListener(listener);
    }

    public void setSaveClick(View.OnClickListener listener) {
        layoutSave.setOnClickListener(listener);
    }

    public void setAddViewVisible(int visible) {
        layoutAdd.setVisibility(visible);
        imgAdd.setVisibility(visible);
    }

    public void setSaveViewVisible(int visible) {
        layoutSave.setVisibility(visible);
        tvSave.setVisibility(visible);
    }

    public void setAddImageResource(int id) {
        imgAdd.setImageResource(id);
    }

    public void setSaveText(String text) {
        tvSave.setText(text);
    }

    /**
     * 是否显示actionbar
     */
    public void enableBackBehavior() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * 横竖屏、输入设备变化等
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void showToast(String str) {
        ToastUtil.showToast(this, str);
    }

    public void showNetError() {
        ToastUtil.showToast(this, "网络错误，请稍候重试");
    }

    public void showToast(int str) {
        ToastUtil.showToast(this, str);
    }

    /**
     * 获取屏幕宽度--像素
     *
     * @return
     */
    public int getScreenWidth() {
        return ScreenUtil.getScreenWidth(this);
    }

    /**
     * 获取屏幕高度--像素
     *
     * @return
     */
    public int getScreenHeight() {
        return ScreenUtil.getScreenHeight(this);
    }

    /**
     * 是否已连接网络
     *
     * @return
     */
    public boolean isConnected() {
        return NetUtil.isConnected(this);
    }

    /**
     * 跳转到登录界面
     */
    public void toLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * 点击屏幕隐藏键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        BizUtil.hideKeybord(this);
        return super.onTouchEvent(event);
    }

    /**
     * 显示加载dialog
     */
    public void showLoadingDialog() {
        if (dialog != null) {
            dialog.show();
        }
    }

    /**
     * 显示加载dialog
     */
    public void dismissLoadingDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
