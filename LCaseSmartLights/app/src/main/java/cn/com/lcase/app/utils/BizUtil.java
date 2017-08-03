package cn.com.lcase.app.utils;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.model.Device;

public class BizUtil {



    static BizUtil instance;

    public static BizUtil getInstance() {
        if (instance == null) {
            instance = new BizUtil();
        }
        return instance;
    }

    /**
     * 关闭软键盘
     *
     * @param context
     */
    public static void hideKeybord(Activity context) {
        if (context == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && context.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 验证是否是手机号
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {

        Pattern p = Pattern.compile("^1(3[0-9]|4[57]|5[0-35-9]|8[0-9]|70)\\d{8}$");

        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    // 验证邮箱的正则表达式
    public static boolean isEmail(String email) {
        String str = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView, Context mContext) {
        //获取listview的适配器
        ListAdapter listAdapter = listView.getAdapter(); //item的高度

        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); //计算子项View 的宽高 //统计所有子项的总高度
            totalHeight += ScreenUtil.dp2px(mContext, listItem.getMeasuredHeight());
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

    public static void setSwipeMenuListViewHeightBasedOnChildren(SwipeMenuListView listView, Context mContext) {
        //获取listview的适配器
        ListAdapter listAdapter = listView.getAdapter(); //item的高度

        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); //计算子项View 的宽高 //统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public PackageInfo getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 拼接开灯操作
     */
    public static String OnCommand(String code) {
        String s = "07" + code.substring(0, code.length() - 1) + "81" + "01" + "000000000000000" + code.substring(code.length() - 1, code.length());
        return s;
    }

    /**
     * 拼接关灯操作
     */
    public static String OffCommand(String code) {
        String s = "07" + code.substring(0, code.length() - 1) + "81" + "00" + "000000000000000" + code.substring(code.length() - 1, code.length());
        return s;
    }

    /**
     * 拼接电视机操作
     * 07+SSID+flags+data   返回的s 总共62位
     *
     * @param data_before data的前六位
     * @param data_after  data的后四位
     */
    public static String tvCommand(String code, String flags, String data_before, String data_after) {
        String s = "07" + code + flags + data_before + "000000000000" + data_after;
        return s;
    }

    /**
     * 查询设备状态
     *
     * @param code
     * @return
     */
    public static String queryCommand(String code) {
        String s = "08" + code;
        return s;
    }

    /**
     * 查询所有设备状态
     */
    public static void queryAllCommand(List<Device> mList) {
        for (int i = 0; i < mList.size(); i++) {
            Device d = mList.get(i);
            if ("1".equals(d.getType())) {
//                08 71F75C4437FA3263D8CB33188008FB7C
                MyApplication.mClient.publish(BizUtil.queryCommand(d.getCode().substring(0, d.getCode().length() - 1)));
            } else {
                MyApplication.mClient.publish(BizUtil.queryCommand(d.getCode()));
            }
        }
    }

    /**
     * 查询设备后得到设备的状态
     *
     * @param msg
     * @param mList
     * @param adapterAll
     */
    private static void queryDeviceResult(String msg, List<Device> mList, BaseAdapter adapterAll) {
//        对于发出08 查询命令后得到灯的状态  对于电视的状态 如果是离线会返回FF05 那么这种情况如果有很多电视设备的情况 就会我们不知道是那个设备掉线了  所以我不需要查询设备  直接每次都用0a监听
        if (msg.startsWith("09")) {
            //        0971F75C4437FA3263D8CB33188008FB7C000000000000000003030000 这是中控反馈的灯
//        09 71F75C4437FA3263D8CB33188008FB7C(设备码) 00（状态关） 00000000000000 0303 0000 这是中控反馈的灯
            String tempCode = msg.substring(2, 34) + msg.substring(msg.length() - 7, msg.length() - 6);//71F75C4437FA3263D8CB33188008FB7C3
            String state = msg.substring(34, 36);//00
            Boolean tempIsOn = false;
            if ("01".equals(state)) {
                tempIsOn = true;
            }
            int size = mList.size();
            for (int i = 0; i < size; i++) {
                Device tempD = mList.get(i);
                if (tempCode.equals(tempD.getCode())) {
                    tempD.setOnoff(tempIsOn);
                    if (adapterAll != null) {
                        adapterAll.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    /**
     * 设备状态改变后（比如app发出开或者关） 收到设备状态的命令
     *
     * @param msg
     * @param mList
     * @param adapter
     */
    private static void changeDeviceStatueResult(String msg, List<Device> mList, BaseAdapter adapter) {

        if (msg.startsWith("0a")) {
            if (msg.length() > 55) {
//                那就是开关
//        0a71F75C4437FA3263D8CB33188008FB7C000000000000000001030000 这是中控反馈的灯
//        0a 71F75C4437FA3263D8CB33188008FB7C(设备码) 00（状态关） 00000000000000 0103 0000 这是中控反馈的灯
                String tempCode = msg.substring(2, 34) + msg.substring(msg.length() - 7, msg.length() - 6);//71F75C4437FA3263D8CB33188008FB7C1
                String state = msg.substring(34, 36);//00
                Boolean tempIsOn = false;
                if ("01".equals(state)) {
                    tempIsOn = true;
                }
                int size = mList.size();
                for (int i = 0; i < size; i++) {
                    Device tempD = mList.get(i);
                    if (tempCode.equals(tempD.getCode()) && tempIsOn.compareTo(tempD.isOnoff()) != 0) {
                        tempD.setOnoff(tempIsOn);
                        adapter.notifyDataSetChanged();
                    }
                }
            } else {
//        电视在线0aC49032BBB95B40E8A5F08F51FC8CD9AF01000000000000000000 总共55
//        电视离线0aC49032BBB95B40E8A5F08F51FC8CD9AF00000000000000000000
                String tempCode = msg.substring(2, 34);
                String state = msg.substring(34, 36);//00
                Boolean tempIsOn = false;
                if ("01".equals(state)) {
                    tempIsOn = true;
                }
                int size = mList.size();
                for (int i = 0; i < size; i++) {
                    Device tempD = mList.get(i);
                    if (tempCode.equals(tempD.getCode()) && tempIsOn.compareTo(tempD.isOnoff()) != 0) {
                        tempD.setOnoff(tempIsOn);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

    }
}
