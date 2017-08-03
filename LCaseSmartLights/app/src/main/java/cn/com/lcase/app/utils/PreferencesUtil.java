package cn.com.lcase.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.net.LCaseConstants;

/**
 * Created by admin on 2016/10/25.
 */

public class PreferencesUtil {


    public static void putUserName(String userName) {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putString(LCaseConstants.USER_NAME, userName);
        edit.commit();
    }

    public static void putUserPhone(String userName) {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putString(LCaseConstants.USER_PHONE, userName);
        edit.commit();
    }

    public static void putDeviceCount(int deviceCount) {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putInt(LCaseConstants.USER_DEVICE_COUNT, deviceCount);
        edit.commit();
    }

    public static void putUserToken(String token) {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putString(LCaseConstants.USER_TOKEN, token);
        edit.commit();
    }

    public static void putUserPassword(String password) {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putString(LCaseConstants.USER_PASSWORD, password);
        edit.commit();
    }

    public static void putUserId(int user_id) {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putInt(LCaseConstants.USER_ID, user_id);
        edit.commit();
    }

    public static String getUserName(Context context) {
        return MyApplication.preferences.getString(LCaseConstants.USER_NAME, "");
    }

    public static String getUserPhone(Context context) {
        return MyApplication.preferences.getString(LCaseConstants.USER_PHONE, "");
    }

    public static String getUserToken(Context context) {
        return MyApplication.preferences.getString(LCaseConstants.USER_TOKEN, "");
    }

    public static String getUserPassword(Context context) {
        return MyApplication.preferences.getString(LCaseConstants.USER_PASSWORD, "");
    }

    public static int getDeviceCount(Context context) {
        return MyApplication.preferences.getInt(LCaseConstants.USER_DEVICE_COUNT, 0);
    }

    public static int getUserId(Context context) {
        return MyApplication.preferences.getInt(LCaseConstants.USER_ID, -1);
    }

    public static void clear() {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.clear().commit();
    }
    /***
     * ***************    语音唤醒设置    *******************
     */

    // 0指定wifi 1始终开启 2 始终关闭
    public static void setVoiceRelatedStatus(int status){
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putInt(LCaseConstants.VOICE_STATU, status);
        edit.commit();
    }
    public static int getVoiceRelatedStatus(){
        return MyApplication.preferences.getInt(LCaseConstants.VOICE_STATU, 1);
    }

    //如果是指定开启wifi情况下那么指定wifi的名字
    public static void setRelatedWifiName(String wifiName){
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putString(LCaseConstants.WIFI_NAME, wifiName);
        edit.commit();
    }
    public static String getRelatedWifiName(){
        return MyApplication.preferences.getString(LCaseConstants.WIFI_NAME, "");
    }

    /**
     * 是否开启语音反馈
     * @param feedback
     */
    public static void setVoicedback(Boolean feedback) {
        SharedPreferences.Editor edit = MyApplication.preferences.edit();
        edit.putBoolean(LCaseConstants.VOICEBACK, feedback);
        edit.commit();
    }
    public static Boolean getVoicedback() {
        return MyApplication.preferences.getBoolean(LCaseConstants.VOICEBACK, true);
    }
    /***
     * ***************    语音唤醒设置    *******************
     */
}
