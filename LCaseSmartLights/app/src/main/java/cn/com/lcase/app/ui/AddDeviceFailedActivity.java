package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.view.View;

import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;

public class AddDeviceFailedActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_failed);
        configActionBar("添加设备");
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
