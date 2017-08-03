package cn.com.lcase.app.ui;

import android.os.Bundle;

import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;

/**
 * 创建日期：2017/5/18 on 12:00
 * 描述:语音指令
 * 作者:admin
 */
public class VoiceCommandsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_commands);
        setActionBar();
    }

    private void setActionBar() {
        configActionBar("语音指令参考");
     /*   setSaveViewVisible(View.VISIBLE);
        setSaveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePrivateDevice();
            }
        });*/
    }
}
