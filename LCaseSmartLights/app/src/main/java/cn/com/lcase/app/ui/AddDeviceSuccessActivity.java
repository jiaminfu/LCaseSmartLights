package cn.com.lcase.app.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.lcase.app.BaseActivity;
import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.net.LCaseApiClient;
import cn.com.lcase.app.net.ReturnVo;
import cn.com.lcase.app.utils.BizUtil;
import cn.com.lcase.app.utils.CacheUtils;
import cn.com.lcase.app.utils.NumberText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cn.com.lcase.app.net.LCaseConstants.DEVICE_NAME;

public class AddDeviceSuccessActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.device_list)
    ListView deviceList;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    MyAdapter adapter;
    private List<Device> newDevices = new ArrayList<>();

    private RadioGroup group1;
    private RadioGroup group2;
    private RadioGroup group3;
    private RadioGroup group4;
    private RadioGroup group5;
    private EditText etGroupName;
    private int checkedPosition = -1;
    private Boolean changeGroup = false;
    private Dialog renameDialog;
    private View view;
    private Button cancel;
    private Button confirm;
    private int itemposition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_success);
        ButterKnife.bind(this);
        configActionBar("添加设备");
        newDevices = CacheUtils.getInstants().getNewDevices();
        initData();
    }

    int count = 0;
    boolean isOn = false;

    private void initData() {
        adapter = new MyAdapter();
        deviceList.setAdapter(adapter);
        initRenameDialog();
      /*  deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

            }
        });*/
    }

    private void addDevice(Device device, final int position) {
        showLoadingDialog();
        new LCaseApiClient().addDevice(device).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                dismissLoadingDialog();
                if (response.body() != null) {
                    if (response.body().getMessage() != null) {
                        showToast("添加成功");
                        newDevices.remove(position);
                        adapter.notifyDataSetChanged();
//                        showToast(response.body().getMessage());
                        if (newDevices.size()==0) {
                            finish();
                        }
                    }
                    if (response.body().isSuccess()) {
                        //加判断，如果newDevices 的size 为0，直接finish；
                        newDevices.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    showNetError();
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                showNetError();
                dismissLoadingDialog();
            }
        });
    }

    @OnClick({R.id.btn_confirm, R.id.btn_confirm1})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                MyApplication.mClient.publish("0771F75C4437FA3263D8CB33188008FB7C81010000000000000001");
                break;
            case R.id.btn_confirm1:
                MyApplication.mClient.publish("0771F75C4437FA3263D8CB33188008FB7C81000000000000000001");

                break;
        }

    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return newDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return newDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(AddDeviceSuccessActivity.this).inflate(R.layout.add_device_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Device device = newDevices.get(position);
            if (device.getType() != null) {
                if (device.getType().equals("1")) {
                    holder.imgLogo.setImageResource(R.mipmap.taideng);
                } else {
                    holder.imgLogo.setImageResource(R.mipmap.dianshi);
                    holder.code.setText("电视");
                }
            } else {
                holder.imgLogo.setImageResource(R.mipmap.taideng);
            }
            if (device.getType().equals("1"))
                holder.code.setText(device.getName() == null ? (device.getCode() == null ? "未知设备" : device.getCode()) : device.getName());
            holder.add_device_success.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    final Device device = newDevices.get(position);
                    if (device.getType().equals("2")) {//电视，直接添加
                        device.setImage(14);
                        device.setName(DEVICE_NAME[14]);
                        addDevice(device, position);
                    } else {
                        if (count == 0) {
//
                            showToast("请注意设备状态，确定设备位置,可为设备命名同时添加设备");
                            count = 0;
                            isOn = false;
                            final android.os.Handler handler = new android.os.Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    //要做的事情
                                    if (isOn) {
                                        MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));//关灯指令
                                        isOn = false;
                                    } else {
                                        MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));//开灯指令
                                        isOn = true;
                                    }
                                    count++;
                                    if (count == 3) {
                                        handler.removeCallbacks(this);
                                        //继续下面的操作 为设备命名
                                        itemposition = position;
                                        if (renameDialog.isShowing()) {
                                            renameDialog.dismiss();
                                        }
                                        renameDialog.show();
                                        count=0;
                                    } else {
                                        handler.postDelayed(this, 3000);
                                    }
                                }
                            };
                            handler.postDelayed(runnable, 1500);
                        }
                    }
                }
            });
            return convertView;
        }


        class ViewHolder {
            @BindView(R.id.img_logo)
            ImageView imgLogo;
            @BindView(R.id.code)
            TextView code;
            @BindView(R.id.activity_add_device_success)
            LinearLayout add_device_success;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }

    }

    /**
     * 重命名
     */
    private void initRenameDialog() {
        renameDialog = new Dialog(AddDeviceSuccessActivity.this, R.style.Translucent_NoTitle);
        view = View.inflate(AddDeviceSuccessActivity.this, R.layout.rename_layout, null);
        cancel = (Button) view.findViewById(R.id.btn_cancel);
        confirm = (Button) view.findViewById(R.id.btn_confirm);
        group1 = (RadioGroup) view.findViewById(R.id.group1);
        group2 = (RadioGroup) view.findViewById(R.id.group2);
        group3 = (RadioGroup) view.findViewById(R.id.group3);
        group4 = (RadioGroup) view.findViewById(R.id.group4);
        group5 = (RadioGroup) view.findViewById(R.id.group5);
        view.findViewById(R.id.radio15).setEnabled(false);
//        view.findViewById(R.id.radio16).setEnabled(false);
        etGroupName = (EditText) view.findViewById(R.id.et_group_name);
        group1.setOnCheckedChangeListener(this);
        group2.setOnCheckedChangeListener(this);
        group3.setOnCheckedChangeListener(this);
        group4.setOnCheckedChangeListener(this);
        group5.setOnCheckedChangeListener(this);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameDialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDevice(itemposition, etGroupName.getText().toString().trim());
            }
        });
        renameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        renameDialog.setCancelable(true);
        renameDialog.setContentView(view);
    }


    private void updateDevice(final int itemposition, final String name) {
        final Device device = newDevices.get(itemposition);
        Device tempDevice = new Device();
        tempDevice.setName(name);
        tempDevice.setCode(device.getCode());
        tempDevice.setType(device.getType());
        tempDevice.setImage(checkedPosition);
//        dialog.dismiss();
        showLoadingDialog();
        new LCaseApiClient().addDevice(tempDevice).enqueue(new Callback<ReturnVo<Object>>() {
            @Override
            public void onResponse(Call<ReturnVo<Object>> call, Response<ReturnVo<Object>> response) {
                renameDialog.dismiss();
                dismissLoadingDialog();
                if (response.body() != null) {
                  /*  if (response.body().isSuccess()) {
                        device.setName(name);
                        device.setImage(checkedPosition);
                        adapter.notifyDataSetChanged();
//                        configActionBar(name);
                    }*/
                    if (response.body().getMessage() != null) {
                        device.setName(name);
                        device.setImage(checkedPosition);
                        newDevices.remove(itemposition);
                        adapter.notifyDataSetChanged();
//                        showToast(response.body().getMessage());
                        showToast("添加成功");
                        if (newDevices.size()==0) {
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnVo<Object>> call, Throwable t) {
                renameDialog.dismiss();
                dismissLoadingDialog();
                showNetError();
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == group1 && changeGroup == false) {
            changeGroup = true;
            group2.clearCheck();
            group3.clearCheck();
            group4.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio1:
                    checkedPosition = 0;
                    break;
                case R.id.radio2:
                    checkedPosition = 1;
                    break;
                case R.id.radio3:
                    checkedPosition = 2;
                    break;
                case R.id.radio4:
                    checkedPosition = 3;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group2 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group3.clearCheck();
            group4.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio5:
                    checkedPosition = 4;
                    break;
                case R.id.radio6:
                    checkedPosition = 5;
                    break;
                case R.id.radio7:
                    checkedPosition = 6;
                    break;
                case R.id.radio8:
                    checkedPosition = 7;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group3 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group2.clearCheck();
            group4.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio9:
                    checkedPosition = 8;
                    break;
                case R.id.radio10:
                    checkedPosition = 9;
                    break;
                case R.id.radio11:
                    checkedPosition = 10;
                    break;
                case R.id.radio12:
                    checkedPosition = 11;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group4 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group2.clearCheck();
            group3.clearCheck();
            group5.clearCheck();
            switch (checkedId) {
                case R.id.radio13:
                    checkedPosition = 12;
                    break;
                case R.id.radio14:
                    checkedPosition = 13;
                    break;
                case R.id.radio15:
                    checkedPosition = 14;
                    break;
                case R.id.radio16:
                    checkedPosition = 15;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
        if (group == group5 && changeGroup == false) {
            changeGroup = true;
            group1.clearCheck();
            group2.clearCheck();
            group3.clearCheck();
            group4.clearCheck();
            switch (checkedId) {
                case R.id.radio17:
                    checkedPosition = 16;
                    break;
            }
            changeGroup = false;
            etGroupName.setText(createDeviceName(checkedPosition));
        }
    }

    /**
     * 创建设备名称
     *
     * @param position
     * @return
     */
    public String createDeviceName(int position) {
        Set<String> set = new HashSet<>();
        String name = DEVICE_NAME[checkedPosition];
        NumberText nt = NumberText.getInstance(NumberText.Lang.ChineseSimplified);
        for (Device device : newDevices) {
            if (device.getImage() == position) {
                set.add(device.getName());
            }
        }
        for (int j = 1; j < set.size() + 1; j++) {
            if (!set.contains(name + nt.getText(j + 1))) {
                name = DEVICE_NAME[checkedPosition] + nt.getText(j + 1);
                break;
            }
        }
        return name;
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        super.onDestroy();
    }
}
