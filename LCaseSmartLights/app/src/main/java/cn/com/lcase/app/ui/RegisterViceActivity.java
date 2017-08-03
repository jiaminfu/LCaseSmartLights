package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.UserInfo;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.MD5;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViceActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.et_phone)
    EditText etPhone;
    @BindView(R.id.btn_code)
    Button btnCode;
    @BindView(R.id.et_code)
    EditText etCode;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.et_re_pwd)
    EditText etRePwd;
    @BindView(R.id.btn_register)
    Button btnRegister;
    private String  phone, code, pwd, repwd,mainToken;
    private Handler handler;
    private Thread thread;
    private Runnable run;
    private boolean isRun = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_vice);
        ButterKnife.bind(this);
        configActionBar("注册");
        initListeners();
        initData();
    }

    private void initListeners() {
        etCode.addTextChangedListener(this);
        etPhone.addTextChangedListener(this);
        etPwd.addTextChangedListener(this);
        etRePwd.addTextChangedListener(this);
    }

    @OnClick({R.id.btn_code, R.id.btn_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_code:
                //获取验证码
                toGetCode();
                break;
            case R.id.btn_register:
                //注册
                toRegister();
                break;
        }
    }
    private void initData() {
        if (getIntent().hasExtra("result")){
            mainToken = getIntent().getStringExtra("result");
        }
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int i = msg.arg1;
                switch (msg.what) {
                    case 0:
                        isRun = true;
                        btnCode.setText(i+"s");
                        break;
                    case 1:
                        isRun = false;
                        btnCode.setEnabled(true);
                        btnCode.setText("重新获取");
                        break;

                    default:
                        break;
                }
            }
        };
        run = new Runnable() {

            @Override
            public void run() {

                for(int i=60;i>0;i--){
                    Message msg = new Message();
                    msg.what = 0;
                    msg.arg1 = i;
                    handler.sendMessage(msg );
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler.sendEmptyMessage(1);

            }
        };
    }
    private void toRegister() {
        phone = etPhone.getText().toString().trim();
        code = etCode.getText().toString().trim();
        pwd = etPwd.getText().toString().trim();
        repwd = etRePwd.getText().toString().trim();
        if (!pwd.equals(repwd)) {
            showToast("两次输入的密码不一致，请重新输入");
            return;
        }
        if (pwd.length() < 6 || pwd.length() > 32){
            showToast("密码的长度为6-32位");
            return;
        }
        UserInfo user = new UserInfo();
        user.setPhone(phone);
        user.setUserpwd(MD5.md5(pwd).toLowerCase());
        user.setUsername(phone);
        user.setVerifyCode(code);
        new LCaseApiClient().saveSubUser(mainToken,user).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    if (response.body().getMessage() != null) showToast(response.body().getMessage());
                    finish();
                } else {
                    if (response != null && response.body() != null && response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                    }
                }

            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
               showNetError();
            }
        });

    }

    private void toGetCode() {

        phone = etPhone.getText().toString().trim();
        if (!BizUtil.isMobileNO(phone)) {
            showToast("请输入正确的手机号码");
            return;
        }
        btnCode.setEnabled(false);
        etCode.requestFocus();
        thread  = new Thread(run);
        thread.start();
        Map<String, String> phones = new HashMap<>();
        phones.put("phone", phone);
        phones.put("type", "registered");
        new LCaseApiClient().getVerifyCode(phones).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    if (response.body().getMessage() != null) {
                        if (response.body().getMessage() != null) showToast(response.body().getMessage());
                        if (response.headers().get("Set-Cookie") != null){
                            CacheUtils.getInstants().setCookie(response.headers().get("Set-Cookie"));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
            showNetError();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        boolean b = etPhone.getText().toString().trim().length() > 8;
        boolean c = etCode.getText().toString().trim().length() > 3;
        boolean d = etPwd.getText().toString().trim().length() > 3;
        boolean e = etRePwd.getText().toString().trim().length() > 3;
        if (b && !isRun) {
            btnCode.setEnabled(true);
        } else {
            btnCode.setEnabled(false);
        }
        if ( b && c && d && e) {
            btnRegister.setEnabled(true);
        } else {
            btnRegister.setEnabled(false);
        }

    }
}
