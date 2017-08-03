package cn.com.lcase.app;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.util.ArrayList;
import java.util.List;

import cn.com.lcase.app.client.SubscribeClient;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.utils.VoiceUtil;

/**
 * Created by admin on 2016/10/13.
 */

public class MyApplication extends Application {
    public static SharedPreferences preferences;
    public static SubscribeClient mClient;
    public static String busAction = "";
    public static VoiceUtil voiceUtil;
    public static List<Device> listDevice;
    public static List<Scene> listScene;//场景
    public static List<Device> exclusiveDeviceList;//专属设备
    public static String  groupName;//专属设备组名
    public static MyApplication instance;

    @Override
    public void onCreate() {
        initIFlyTek();
        super.onCreate();
        preferences = getSharedPreferences(LCaseConstants.USER_INFO, Activity.MODE_PRIVATE);
        initImageLoader();
        listDevice = new ArrayList<>();
        listScene = new ArrayList<>();
        exclusiveDeviceList=new ArrayList<>();
        instance = this;
    }

    public static String getGroupName() {
        return groupName;
    }

    public static void setGroupName(String groupName) {
        MyApplication.groupName = groupName;
    }

    public static List<Device> getExclusiveDeviceList() {
        return exclusiveDeviceList;
    }

    public static void setExclusiveDeviceList(List<Device> exclusiveDeviceList) {
        MyApplication.exclusiveDeviceList = exclusiveDeviceList;
    }

    public static List<Scene> getListScene() {
        return listScene;
    }

    public static void setListScene(List<Scene> listScene) {
        MyApplication.listScene = listScene;
    }

    public static List<Device> getListDevice() {
        return listDevice;
    }

    public static void setListDevice(List<Device> listDevice) {
        MyApplication.listDevice = listDevice;
    }

    private void initIFlyTek() {
        // 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用“,”分隔。
        // 设置你申请的应用appid
        StringBuffer param = new StringBuffer();
        param.append("appid=" + getString(R.string.app_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(MyApplication.this, param.toString());
    }

    private void initImageLoader() {

        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.mipmap.no_photo)
                .showImageOnFail(R.mipmap.no_photo).cacheInMemory(true).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE).bitmapConfig(Bitmap.Config.RGB_565)// 设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)// 载入图片前稍做延时可以提高整体滑动的流畅度
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(480, 480) // max width, max height
                .threadPoolSize(3)// 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2) // 降低线程的优先级保证主UI线程不受太大影响
                .denyCacheImageMultipleSizesInMemory().memoryCache(new LruMemoryCache(10 * 1024 * 1024)) // 建议内存设在5-10M,可以有比较好的表现
                .memoryCacheSize(5 * 1024 * 1024).diskCacheSize(50 * 1024 * 1024) // 硬盘缓存容量//
                // 50M
                .diskCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO) // 后进先出
                .diskCacheFileCount(100) // 缓存的文件数量
                .defaultDisplayImageOptions(options)
                .imageDownloader(new BaseImageDownloader(getApplicationContext(), 5 * 1000, 30 * 1000)) // connectTimeout
                // (5
                // s),
                // readTimeout
                // (30
                // s)
                .writeDebugLogs() // Remove for release app
                .build();

        ImageLoader.getInstance().init(config); // 初始化
    }
}
