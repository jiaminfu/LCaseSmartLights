package cn.com.lcase.app.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.R;
import cn.com.lcase.app.client.SubscribeClient;
import cn.com.lcase.app.model.SysToken;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.MD5;
import cn.com.lcase.app.utils.PreferencesUtil;
import cn.com.lcase.app.utils.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cn.com.lcase.app.MyApplication.mClient;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements TextWatcher {
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.tv_forget)
    TextView tvForget;
    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.btn_login)
    Button btnLogin;
    private String name,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setStatusBar();
        initListeners();
    }

    private void initListeners() {
        etName.addTextChangedListener(this);
        etPwd.addTextChangedListener(this);
    }

    @OnClick({R.id.btn_login, R.id.tv_forget, R.id.tv_register})
    void toRegister(View v) {
        switch (v.getId()) {
            case R.id.btn_login://登录
                toLogin();
                break;
            case R.id.tv_forget://忘记密码
                startActivity(new Intent(this, ResetPasswordActivity.class));
                break;
            case R.id.tv_register://注册
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        /**
                         * 请求权限是一个异步任务  不是立即请求就能得到结果 在结果回调中返回
                         */
                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

                }else{
                    startActivity(new Intent(this, CaptureActivity.class));
                }
                break;
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this, CaptureActivity.class));
                } else {
                    ToastUtil.showToast(this,"获取权限失败,请在设置中手动打开权限");
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

    private void toLogin() {
        name = etName.getText().toString().trim();
        password = MD5.md5(etPwd.getText().toString().trim()).toLowerCase();
        if (password.length() < 6 || password.length() > 32){
            ToastUtil.showToast(this,"密码的长度为6-32位");
            return;
        }
        new LCaseApiClient().login(name, password).enqueue(new Callback<ReturnVo<SysToken>>() {
            @Override
            public void onResponse(Call<ReturnVo<SysToken>> call, Response<ReturnVo<SysToken>> response) {
                if (response != null && response.body() != null && response.body().isSuccess()) {
                    if (response.headers().get("Set-Cookie") != null){
                        CacheUtils.getInstants().setCookie(response.headers().get("Set-Cookie"));
                    }
                    if(response.body().getData().getToken() != null){
                        SysToken sysToken = response.body().getData();
                        if (sysToken.getUsername() != null){
                            PreferencesUtil.putUserName(sysToken.getUsername());
                        }
                        if (sysToken.getPhone() != null){
                            PreferencesUtil.putUserPhone(sysToken.getPhone());
                        }
                        if (sysToken.getToken() != null) {
                            CacheUtils.getInstants().setToken(sysToken.getToken());
                            PreferencesUtil.putUserToken(sysToken.getToken());
                        }
                        if(sysToken.getUserid() != null){
                            PreferencesUtil.putUserId(sysToken.getUserid());
                        }
                        PreferencesUtil.putDeviceCount(sysToken.getDevicecount());
                        PreferencesUtil.putUserPassword(password);
                        CacheUtils.getInstants().setCurrentUser(response.body().getData());
                        toConnectMqtt(sysToken);
                    }
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    if (response != null && response.body() != null && response.body().getMessage() != null) {
                        Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<SysToken>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "网络错误，请稍候重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toConnectMqtt(SysToken sysToken) {
//        mClient = new SubscribeClient("android",sysToken.getSDID());
//        clientId 先寫成android_+sysToken.getUserid() 形式 以後如果後台有這個字段就直接用後台的
        mClient = new SubscribeClient("android_"+sysToken.getUsername(),sysToken.getSDID());

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        boolean a = etName.getText().toString().trim().length() > 5;
        boolean b = etPwd.getText().toString().trim().length() > 5;
        if(a && b){
            btnLogin.setEnabled(true);
        }else{
            btnLogin.setEnabled(false);
        }
    }
}

