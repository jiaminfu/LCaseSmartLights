package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

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

public class ResetPasswordActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, TextWatcher {

    @BindView(R.id.radio_phone)
    RadioButton radioPhone;
    @BindView(R.id.radio_email)
    RadioButton radioEmail;
    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.et_phone)
    EditText etPhone;
    @BindView(R.id.layout_phone)
    RelativeLayout layoutPhone;
    @BindView(R.id.layout_email)
    RelativeLayout layoutEmail;
    @BindView(R.id.btn_phone)
    Button btnPhone;
    @BindView(R.id.btn_email)
    Button btnEmail;
    @BindView(R.id.et_code)
    EditText etCode;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.et_re_pwd)
    EditText etRePwd;
    @BindView(R.id.btn_code)
    Button btnCode;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.btn_email_code)
    Button btnEmailCode;
    @BindView(R.id.et_email_code)
    EditText etEmailCode;
    @BindView(R.id.et_email_pwd)
    EditText etEmailPwd;
    @BindView(R.id.et_email_re_pwd)
    EditText etEmailRePwd;
    private String email, phone, code, pwd, repwd;
    private Handler handler;
    private Runnable run;
    private boolean isRun = false;
    private boolean isRun1 = false;
    private Thread thread;
    private Handler handler1;
    private Thread thread1;
    private Runnable run1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);
        configActionBar("重置密码");
        setListeners();
        initData();
        initDataEmail();
    }
    private void initData() {
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
    private void initDataEmail() {
        handler1 = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int i = msg.arg1;
                switch (msg.what) {
                    case 2:
                        isRun1 = true;
                        btnEmailCode.setText(i+"s");
                        break;
                    case 3:
                        isRun1 = false;
                        btnEmailCode.setEnabled(true);
                        btnEmailCode.setText("重新获取");
                        break;

                    default:
                        break;
                }
            }
        };
        run1 = new Runnable() {

            @Override
            public void run() {

                for(int i=60;i>0;i--){
                    Message msg = new Message();
                    msg.what = 2;
                    msg.arg1 = i;
                    handler1.sendMessage(msg );
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler1.sendEmptyMessage(3);

            }
        };
    }
    private void setListeners() {
        radioGroup.setOnCheckedChangeListener(this);
        etEmail.addTextChangedListener(this);
        etPhone.addTextChangedListener(this);
        etCode.addTextChangedListener(this);
        etPwd.addTextChangedListener(this);
        etRePwd.addTextChangedListener(this);
        etEmailCode.addTextChangedListener(this);
        etEmailPwd.addTextChangedListener(this);
        etEmailRePwd.addTextChangedListener(this);
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_phone:
                layoutEmail.setVisibility(View.GONE);
                btnEmail.setVisibility(View.GONE);
                layoutPhone.setVisibility(View.VISIBLE);
                btnPhone.setVisibility(View.VISIBLE);
                break;
            case R.id.radio_email:
                layoutEmail.setVisibility(View.VISIBLE);
                btnEmail.setVisibility(View.VISIBLE);
                layoutPhone.setVisibility(View.GONE);
                btnPhone.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        boolean a1 = etPhone.getText().toString().length() > 10;
        boolean a2 = etCode.getText().toString().length() > 0;
        boolean a3 = etPwd.getText().toString().length() > 5;
        boolean a4 = etRePwd.getText().toString().length() > 5;
        boolean a5 = etEmail.getText().toString().length() > 0;
        boolean b = etEmailCode.getText().toString().length() > 0;
        boolean b1 = etEmailPwd.getText().toString().length() > 5;
        boolean b2 = etEmailRePwd.getText().toString().length() > 5;

        if (a5 && !isRun1) {
            btnEmailCode.setEnabled(true);
        } else {
            btnEmailCode.setEnabled(false);
        }

        if (a1 && !isRun) {
            btnCode.setEnabled(true);
        } else {
            btnCode.setEnabled(false);
        }

        if(a5 && b && b1 && b2){
            btnEmail.setEnabled(true);
        } else {
            btnEmail.setEnabled(false);
        }


        if (a1 && a2 && a3 && a4) {
            btnPhone.setEnabled(true);
        } else {
            btnPhone.setEnabled(false);
        }

    }

    @OnClick({R.id.btn_code, R.id.btn_phone, R.id.btn_email, R.id.btn_email_code})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_code:
                //获取手机验证码
                toGetCode();
                break;
            case R.id.btn_email_code:
                //获取邮箱验证码
                toGetEmail();
                break;
            case R.id.btn_phone:
                //通过手机重置密码
                toReset();
                break;
            case R.id.btn_email:
                //通过邮箱重置密码
                toResetWithEmail();
                break;
        }
    }

    private void toGetEmail() {
        email = etEmail.getText().toString().trim();
        if (!BizUtil.isEmail(email)) {
            showToast("请输入正确的email格式");
            return;
        }
        btnEmailCode.setEnabled(false);
        thread1  = new Thread(run1);
        thread1.start();
        HashMap<String, String> map = new HashMap<>();
        map.put("email",email);
        new LCaseApiClient().getEmail(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    etEmailCode.requestFocus();
                    if (response.body().getMessage() != null) showToast(response.body().getMessage());
                        if (response.headers().get("Set-Cookie") != null) {
                            CacheUtils.getInstants().setCookie(response.headers().get("Set-Cookie"));
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
            }
        });
    }

    private void toResetWithEmail() {
        email = etEmail.getText().toString().trim();
        code = etEmailCode.getText().toString().trim();
        pwd = etEmailPwd.getText().toString().trim();
        repwd = etEmailRePwd.getText().toString().trim();
        if (!pwd.equals(repwd)) {
            showToast("两次输入的密码不一致，请重新输入");
            return;
        }
        UserInfo user = new UserInfo();
        user.setEmail(email);
        user.setUserpwd(MD5.md5(pwd).toLowerCase());
        user.setVerifyCode(code);

        new LCaseApiClient().resetPasswordEmail(user).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    if (response.body().getMessage() != null) showToast(response.body().getMessage());
                    finish();
                } else {
                    if (response.body().getMessage() != null) {
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

    private void toReset() {
        phone = etPhone.getText().toString().trim();
        code = etCode.getText().toString().trim();
        pwd = etPwd.getText().toString().trim();
        repwd = etRePwd.getText().toString().trim();
        if (!pwd.equals(repwd)) {
            showToast("两次输入的密码不一致，请重新输入");
            return;
        }
        UserInfo user = new UserInfo();
        user.setPhone(phone);
        user.setUserpwd(MD5.md5(pwd).toLowerCase());
        user.setVerifyCode(code);

        new LCaseApiClient().resetPassword(user).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    if (response.body().getMessage() != null) showToast(response.body().getMessage());
                    finish();
                } else {
                    if (response.body().getMessage() != null) {
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
        thread  = new Thread(run);
        thread.start();
        Map<String, String> phones = new HashMap<>();
        phones.put("phone", phone);
        phones.put("type", "forget");
        new LCaseApiClient().getVerifyCode(phones).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    etCode.requestFocus();
                    if (response.body().getMessage() != null) {
                        showToast(response.body().getMessage());
                        if (response.headers().get("Set-Cookie") != null) {
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

}
