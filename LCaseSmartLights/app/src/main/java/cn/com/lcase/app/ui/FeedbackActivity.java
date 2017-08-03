package cn.com.lcase.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.R;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.et_feedback)
    EditText etFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        setActionBar();
    }

    private void setActionBar() {
        configActionBar("意见反馈");
        setSaveViewVisible(View.VISIBLE);
        setSaveText("提交");
        setSaveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackSave();
            }
        });
    }

    private void feedbackSave() {
        String feedback = etFeedback.getText().toString().trim();
        if (feedback.isEmpty()) {
            showToast("请输入您的意见和建议");
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("content", feedback);
        new LCaseApiClient().feedbackSave(map).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                if (response.body() != null) {
                    if (response.body().isSuccess()) finish();
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
}
