package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.widget.ImageView;

import com.google.zxing.WriterException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.utils.PreferencesUtil;
import cn.com.zxing.EncodingHandler;

public class AddViceUserActivity extends BaseActivity {

    @BindView(R.id.rq_code)
    ImageView rqCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vice_user);
        ButterKnife.bind(this);
        configActionBar("添加副账户");
        try {
            rqCode.setImageBitmap(EncodingHandler.createQRCode("DY:" + PreferencesUtil.getUserToken(this),300));
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
