package cn.com.lcase.app.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.client.SubscribeClient;
import cn.com.lcase.app.model.SysToken;
import cn.com.lcase.app.model.Us;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.NetUtil;
import cn.com.lcase.app.utils.PreferencesUtil;
import cn.com.lcase.app.utils.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends Activity {

    @BindView(R.id.img_splash)
    ImageView imgSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        setStatusBar();
        initData();
        managePermission();
    }

    private void initData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!NetUtil.isConnected(SplashActivity.this)) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                    Toast.makeText(SplashActivity.this, "请检查网络连接", Toast.LENGTH_SHORT).show();
                } else {
                    new LCaseApiClient().aboutUs().enqueue(new Callback<ReturnVo<Us>>() {
                        @Override
                        public void onResponse(Call<ReturnVo<Us>> call, Response<ReturnVo<Us>> response) {
                            if (response.body() != null) {
                                ReturnVo<Us> body = response.body();
                                if (body.isSuccess()) {
                                    CacheUtils.getInstants().setUs(body.getData());
                                    ImageLoader.getInstance().displayImage(LCaseConstants.SERVER_URL + body.getData().getStartimage(), imgSplash);
                                    toStartActivity();
                                } else {
                                    toStartActivity();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ReturnVo<Us>> call, Throwable t) {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                           /* startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                            Toast.makeText(SplashActivity.this, "请检查网络连接", Toast.LENGTH_SHORT).show();*/

                        }
                    });
                }
            }
        }, 1000);

    }

    /**
     * 权限管理
     */
    private void managePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 2);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ToastUtil.showToast(this, "获取权限失败,请在设置中手动打开权限");
                }
                return;
            }
        }
    }

    /**
     * 透明状态栏
     */
    private void setStatusBar() {
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private void toStartActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PreferencesUtil.getUserToken(SplashActivity.this).isEmpty()) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //自动登录
                    toLogin();
                }
            }
        }, 2000);
    }

    private void toLogin() {
        String name = PreferencesUtil.getUserName(this);
        String password = PreferencesUtil.getUserPassword(this);
        new LCaseApiClient().login(name, password).enqueue(new Callback<ReturnVo<SysToken>>() {
            @Override
            public void onResponse(Call<ReturnVo<SysToken>> call, Response<ReturnVo<SysToken>> response) {
                if (response != null && response.body() != null && response.body().isSuccess()) {
                    if (response.headers().get("Set-Cookie") != null) {
                        CacheUtils.getInstants().setCookie(response.headers().get("Set-Cookie"));
                    }
                    if (response.body().getData().getToken() != null) {
                        SysToken sysToken = response.body().getData();
                        if (sysToken.getUsername() != null) {
                            PreferencesUtil.putUserName(sysToken.getUsername());
                        }
                        if (sysToken.getPhone() != null) {
                            PreferencesUtil.putUserPhone(sysToken.getPhone());
                        }
                        if (sysToken.getToken() != null) {
                            CacheUtils.getInstants().setToken(sysToken.getToken());
                            PreferencesUtil.putUserToken(sysToken.getToken());
                        }
                        if (sysToken.getUserid() != null) {
                            PreferencesUtil.putUserId(sysToken.getUserid());
                        }
                        PreferencesUtil.putDeviceCount(sysToken.getDevicecount());
                        CacheUtils.getInstants().setCurrentUser(response.body().getData());
                        toConnectMqtt(sysToken);
                    }
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<SysToken>> call, Throwable t) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void toConnectMqtt(SysToken sysToken) {
        MyApplication.mClient =  new SubscribeClient("android_"+sysToken.getUsername(),sysToken.getSDID());;
    }
}
