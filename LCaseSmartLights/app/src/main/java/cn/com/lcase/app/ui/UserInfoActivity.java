package cn.com.lcase.app.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Version;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.MD5;
import cn.com.lcase.app.utils.PreferencesUtil;
import cn.com.lcase.app.utils.UpdateUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserInfoActivity extends BaseActivity {


    @BindView(R.id.user_photo)
    CircleImageView userPhoto;
    @BindView(R.id.user_phone)
    TextView userPhone;
    @BindView(R.id.user_device)
    TextView userDevice;
    @BindView(R.id.layout_permissions)
    LinearLayout layoutPermissions;
    @BindView(R.id.layout_changepwd)
    LinearLayout layoutChangepwd;
    @BindView(R.id.layout_device)
    LinearLayout layoutDevice;
    @BindView(R.id.layout_voice)
    LinearLayout layoutVoice;
    @BindView(R.id.layout_vice_user)
    LinearLayout layoutViceUser;
    @BindView(R.id.layout_feedback)
    LinearLayout layoutFeedback;
    @BindView(R.id.layout_version)
    LinearLayout layoutVersion;
    @BindView(R.id.layout_info)
    LinearLayout layoutInfo;
    @BindView(R.id.btn_logout)
    Button btnLogout;
    @BindView(R.id.layout_to_main_account)
    LinearLayout layoutToMainAccount;
    Version currentVersion;
    PackageInfo version;
    @BindView(R.id.version_code)
    TextView versionCode;
    private Button getCode;
    private Handler handler;
    private Thread thread;
    private Runnable run;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        configActionBar("我的");
        initData();
    }

    private void initData() {
        version = BizUtil.getInstance().getVersion(UserInfoActivity.this);
        checkUpdate();
        userPhone.setText(PreferencesUtil.getUserPhone(this));
        userDevice.setText(String.format(getString(R.string.device_count), PreferencesUtil.getDeviceCount(this)));
        if (CacheUtils.getInstants().getCurrentUser()!=null) {
            if (CacheUtils.getInstants().getCurrentUser().isMainaccount()) {
                layoutToMainAccount.setVisibility(View.GONE);
                layoutPermissions.setVisibility(View.VISIBLE);
                layoutViceUser.setVisibility(View.VISIBLE);
            } else {
                layoutToMainAccount.setVisibility(View.VISIBLE);
                layoutPermissions.setVisibility(View.GONE);
                layoutViceUser.setVisibility(View.GONE);
                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int i = msg.arg1;
                        switch (msg.what) {
                            case 0:
                                getCode.setText(i + "s");
                                break;
                            case 1:
                                getCode.setEnabled(true);
                                getCode.setText("获取验证码");
                                break;

                            default:
                                break;
                        }
                    }
                };
                run = new Runnable() {

                    @Override
                    public void run() {

                        for (int i = 60; i > 0; i--) {
                            Message msg = new Message();
                            msg.what = 0;
                            msg.arg1 = i;
                            handler.sendMessage(msg);
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
        }else {
            layoutToMainAccount.setVisibility(View.VISIBLE);
            layoutPermissions.setVisibility(View.GONE);
            layoutViceUser.setVisibility(View.GONE);
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    int i = msg.arg1;
                    switch (msg.what) {
                        case 0:
                            getCode.setText(i + "s");
                            break;
                        case 1:
                            getCode.setEnabled(true);
                            getCode.setText("获取验证码");
                            break;

                        default:
                            break;
                    }
                }
            };
            run = new Runnable() {

                @Override
                public void run() {

                    for (int i = 60; i > 0; i--) {
                        Message msg = new Message();
                        msg.what = 0;
                        msg.arg1 = i;
                        handler.sendMessage(msg);
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

    }

    @OnClick({R.id.user_photo, R.id.layout_to_main_account, R.id.layout_permissions, R.id.layout_changepwd, R.id.layout_device, R.id.layout_voice, R.id.layout_vice_user, R.id.layout_feedback, R.id.layout_version, R.id.layout_info, R.id.btn_logout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_photo:
                break;
            case R.id.layout_permissions:
                //权限设置
                startActivity(new Intent(this, PermissionsActivity.class));
                break;
            case R.id.layout_changepwd:
                //修改密码
                showChangePwdDialog();
                break;
            case R.id.layout_device:
                //专属设备
                startActivity(new Intent(this, ExclusiveEquipmentActivity.class));
                break;
            case R.id.layout_voice:
                //语音设置
                startActivity(new Intent(this, VoiceSettingActivity.class));
                break;
            case R.id.layout_vice_user:
                //添加副账户
                showAddViceUserDialog();
                break;
            case R.id.layout_feedback:
                //意见建议
                startActivity(new Intent(this, FeedbackActivity.class));
                break;
            case R.id.layout_version:
                //版本更新
                if(currentVersion!=null){
                    if (currentVersion.getVersioncode() == version.versionCode) {
                        showUpdateDialog(0);
                    } else {
                        showUpdateDialog(1);
                    }
                }

                break;
            case R.id.layout_info:
                //关于我们
                startActivity(new Intent(this, AboutUsActivity.class));
                break;
            case R.id.btn_logout:
                //退出登录
                logout();
                break;
            case R.id.layout_to_main_account:
                //转为主账户
                Intent intent = new Intent(this, CaptureActivity.class);
                intent.putExtra("main", 1);
                startActivityForResult(intent, 100);
                break;
        }
    }

    private void checkUpdate() {
        new LCaseApiClient().queryAppVersion().enqueue(new Callback<ReturnVo<Version>>() {
            @Override
            public void onResponse(Call<ReturnVo<Version>> call, Response<ReturnVo<Version>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess() && response.body().getData() != null) {
                        Version body = response.body().getData();
                        currentVersion = body;
                        if (currentVersion.getVersioncode() > version.versionCode) {
                            versionCode.setText("v_" + currentVersion.getVersionName());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Version>> call, Throwable t) {
                showNetError();
            }
        });

    }

    /**
     * 显示更新窗口
     *
     * @param type
     */
    private void showUpdateDialog(final int type) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(UserInfoActivity.this);
        switch (type) {
            case 1:
                dialog.setTitle("发现新版本");
                dialog.setMessage(currentVersion.getReason());
                break;
            default:
                dialog.setTitle("当前已是最新版本");
                break;
        }
        dialog.setCancelable(false);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (type) {
                    case 1:
                        UpdateUtil util = new UpdateUtil(currentVersion, UserInfoActivity.this);
                        util.downloadApk();
                        dialogInterface.dismiss();
                        break;
                    default:
                        dialogInterface.dismiss();
                        break;
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.create().show();


    }

    //退出登录
    private void logout() {
        if (CacheUtils.getInstants().getCookie() != null && CacheUtils.getInstants().getToken() != null) {
            new LCaseApiClient().logout().enqueue(new Callback<ReturnVo<Object>>() {
                @Override
                public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                    if (response.body() != null) {
                        if (response.body().isSuccess()) {
                            PreferencesUtil.clear();
                            CacheUtils.getInstants().setToken("");
                            UserInfoActivity.this.setResult(RESULT_OK);
                            finish();
                        } else {
                            if (response.body().getMessage() != null) {
                                showToast(response.body().getMessage());
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

    /**
     * 添加副账户
     */
    private void showAddViceUserDialog() {
        final Dialog dialog = new Dialog(this);
        View view = View.inflate(this, R.layout.add_vice_user_layout, null);
        final EditText etPwd = (EditText) view.findViewById(R.id.et_pwd);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button confirm = (Button) view.findViewById(R.id.btn_confirm);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = etPwd.getText().toString().trim();
                if (pwd.isEmpty()) {
                    showToast("请输入密码");
                    return;
                }
                checkPassword(dialog, MD5.md5(pwd));

            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.show();
        dialog.setContentView(view);
    }

    //验证密码
    private void checkPassword(final Dialog dialog, String pwd) {
        Map<String, String> map = new HashMap<>();
        map.put("password", pwd.toLowerCase());
        new LCaseApiClient().checkPassword(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        startActivity(new Intent(UserInfoActivity.this, AddViceUserActivity.class));
                        dialog.dismiss();
                    } else {
                        if (response.body().getMessage() != null) {
                            showToast(response.body().getMessage());
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

    /**
     * 修改密码
     */
    private void showChangePwdDialog() {
        final Dialog dialog = new Dialog(this);
        View view = View.inflate(this, R.layout.change_pwd_layout, null);
        final EditText etOldPwd = (EditText) view.findViewById(R.id.et_old_pwd);
        final EditText etNewPwd = (EditText) view.findViewById(R.id.et_new_pwd);
        final EditText etNewPwd2 = (EditText) view.findViewById(R.id.et_re_pwd);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button confirm = (Button) view.findViewById(R.id.btn_confirm);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPwd = etOldPwd.getText().toString().trim();
                String newPwd = etNewPwd.getText().toString().trim();
                String newPwd2 = etNewPwd2.getText().toString().trim();
                if (oldPwd.isEmpty() || newPwd.isEmpty() || newPwd2.isEmpty()) {
                    showToast("请确认信息完整");
                    return;
                }
                if (oldPwd.length() < 6 || newPwd.length() < 6 || newPwd2.length() < 6) {
                    showToast("密码不能少于6位");
                    return;
                }
                if (oldPwd.length() > 32|| newPwd.length() > 32 || newPwd2.length() > 32) {
                        showToast("密码的长度为6-32位");
                    return;
                }
                if (!newPwd.equals(newPwd2)) {
                    showToast("两次输入的密码不一致，请重新输入");
                    return;
                }
                rePassword(oldPwd, newPwd, dialog);
            }
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.show();
        dialog.setContentView(view);
    }

    /**
     * 修改密码
     *
     * @param oldPwd
     * @param newPwd
     * @param dialog
     */
    private void rePassword(String oldPwd, final String newPwd, final Dialog dialog) {
        Map<String, String> map = new HashMap<>();
        map.put("oldpassword", MD5.md5(oldPwd).toLowerCase());
        map.put("password", MD5.md5(newPwd).toLowerCase());
        new LCaseApiClient().rePassword(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    ReturnVo<Object> body = response.body();
                    if (body.isSuccess()) {
                        if (response.body().getMessage() != null)
                            showToast(response.body().getMessage());
                        PreferencesUtil.putUserPassword(MD5.md5(newPwd).toLowerCase());
                        dialog.dismiss();
                    } else {
                        if (body.getMessage() != null) {
                            showToast(body.getMessage());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data.hasExtra("device_id")) {
                showMainUser(data.getStringExtra("device_id"));
            }
        }
    }

    /**
     * 转为主账号
     *
     * @param device_id
     */
    private void showMainUser(final String device_id) {
        final Dialog dialog = new Dialog(this);
        View view = View.inflate(this, R.layout.change_main_user_layout, null);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        getCode = (Button) view.findViewById(R.id.btn_get_code);
        Button confirm = (Button) view.findViewById(R.id.btn_confirm);
        final EditText etEmail = (EditText) view.findViewById(R.id.et_email);
        final EditText etCode = (EditText) view.findViewById(R.id.et_code);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (!BizUtil.isEmail(email)) {
                    showToast("请输入正确的email格式");
                    return;
                }
                etCode.requestFocus();
                toGetEmail(email);
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String code = etCode.getText().toString().trim();
                transformMainUser(device_id, email, code, dialog);
            }


        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.show();
        dialog.setContentView(view);
    }

    private void transformMainUser(String device_id, String email, String code, final Dialog dialog) {
        Map<String, String> map = new HashMap<>();
        map.put("verifyCode", code);
        map.put("phone", PreferencesUtil.getUserPhone(this) == null ? "" : PreferencesUtil.getUserPhone(this));
        map.put("email", email);
        map.put("controlcode", device_id);
        new LCaseApiClient().transformMainUser(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) {
                        dialog.dismiss();
                        showToast("转主账户成功，请重新登录");
                        PreferencesUtil.clear();
                        CacheUtils.getInstants().setToken("");
                        UserInfoActivity.this.setResult(RESULT_OK);
                        finish();

                    } else {
                        if (response.body().getMessage() != null) {
                            showToast(response.body().getMessage());
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                dialog.dismiss();
                showNetError();
            }
        });
    }

    private void toGetEmail(String email) {
        getCode.setEnabled(false);
        thread = new Thread(run);
        thread.start();
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        new LCaseApiClient().getEmail(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null && response.body().isSuccess()) {
                    if (response.body().getMessage() != null)
                        showToast(response.body().getMessage());
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
}