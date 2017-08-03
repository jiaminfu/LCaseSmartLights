package cn.com.lcase.app.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Us;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;

public class AboutUsActivity extends BaseActivity {

    @BindView(R.id.email)
    TextView email;
    @BindView(R.id.phone)
    TextView phone;
    @BindView(R.id.QRCode)
    ImageView QRCode;
    @BindView(R.id.public_code)
    TextView publicCode;
    @BindView(R.id.tv_version)
    TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ButterKnife.bind(this);
        configActionBar("关于我们");
        initData();
    }

    private void initData() {
        if (CacheUtils.getInstants().getUs() == null) finish();
        setView(CacheUtils.getInstants().getUs());
        PackageInfo version = BizUtil.getInstance().getVersion(this);
        if(version != null){
            tvVersion.setText("当前版本v_"+version.versionName);
        }
    }

    private void setView(Us us) {
        email.setText(us.getEmail());
        phone.setText(us.getServicetel());
        publicCode.setText(us.getPublicnum());
        ImageLoader.getInstance().displayImage(LCaseConstants.SERVER_URL + us.getPubliccode(), QRCode);
    }

    /**
     * 发送邮件
     */
    void toSendEmail() {
        Uri uri = Uri.parse("mailto:zhaodongyu@5deyi.com");
//                String[] email = {"3802**92@qq.com"};
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
//                intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
//                intent.putExtra(Intent.EXTRA_SUBJECT, "这是邮件的主题部分"); // 主题
//                intent.putExtra(Intent.EXTRA_TEXT, "这是邮件的正文部分"); // 正文
        startActivity(intent);
    }

    public void toCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick({R.id.layout_email, R.id.layout_phone, R.id.layout_qrcode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_email:
                toSendEmail();
                break;
            case R.id.layout_phone:
                toCall(CacheUtils.getInstants().getUs().getServicetel());
                break;
            case R.id.layout_qrcode:
                //显示二维码
                showQRCodeDialog(CacheUtils.getInstants().getUs().getPubliccode());
                break;
        }
    }

    private void showQRCodeDialog(String publiccode) {
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle1);
        View view = View.inflate(this, R.layout.qr_code_layout, null);
        ImageView img_qr_code = (ImageView) view.findViewById(R.id.img_qrcode);
        ImageLoader.getInstance().displayImage(LCaseConstants.SERVER_URL + publiccode, img_qr_code);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.setContentView(view);
    }
}

