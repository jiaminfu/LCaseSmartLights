package cn.com.lcase.app.utils;

import java.util.ArrayList;
import java.util.List;

import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.model.SysToken;
import cn.com.lcase.app.model.Us;
import cn.com.lcase.app.model.UserInfo;

/**
 * Created by admin on 2016/10/24.
 */

public class CacheUtils {
    private static CacheUtils instants;
    private static String cookie;
    private static String token;
    private static UserInfo currentSubUser;
    private static SysToken currentUser;
    private static Scene currentScene;
    private static ArrayList<Scene> scenes;
    private static Device device;
    private static List<Device> lamps;
    private static List<Device> newDevices;

    public  List<Device> getNewDevices() {
        return newDevices;
    }

    public  void setNewDevices(List<Device> newDevices) {
        CacheUtils.newDevices = newDevices;
    }


    public  List<SceneDetail> getCurrentDevices() {
        return currentDevices;
    }

    public  void setCurrentDevices(List<SceneDetail> currentDevices) {
        CacheUtils.currentDevices = currentDevices;
    }

    private static List<SceneDetail> currentDevices;
    private static Us us;


    public SysToken getCurrentUser() {
        return currentUser;
    }

    public  void setCurrentUser(SysToken currentUser) {
        CacheUtils.currentUser = currentUser;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(Scene currentScene) {
        CacheUtils.currentScene = currentScene;
    }

    public UserInfo getCurrentSubUser() {
        return currentSubUser;
    }

    public  void setCurrentSubUser(UserInfo currentSubUser) {
        CacheUtils.currentSubUser = currentSubUser;
    }

    public Device getCurrentDevice() {
        return device;
    }

    public  void setCurrentDevice(Device device) {
        CacheUtils.device = device;
    }

    public  List<Device> getLamps() {
        return lamps;
    }

    public  void setLamps(List<Device> lamps) {
        CacheUtils.lamps = lamps;
    }

    public Us getUs() {
        return us;
    }

    public void setUs(Us us) {
        CacheUtils.us = us;
    }

    public ArrayList<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(ArrayList<Scene> scenes) {
        CacheUtils.scenes = scenes;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        CacheUtils.cookie = cookie;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        CacheUtils.token = token;
    }

    public static CacheUtils getInstants() {
        if (instants == null) {
            instants = new CacheUtils();
        }
        return instants;
    }
}
