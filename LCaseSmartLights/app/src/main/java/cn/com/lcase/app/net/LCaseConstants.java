package cn.com.lcase.app.net;

import cn.com.lcase.app.R;

/**
 * Created by admin on 2016/10/24.
 */

public class LCaseConstants {
//    public static final String SERVER_URL = "http://192.168.1.96:8080";//欧阳进
//    public static final String SERVER_URL = "http://192.168.1.251:8080";//龚柱
    public static final String SERVER_URL = "http://120.76.239.179:8081";//外网

    public static final String CONTEXT_PATH = "/smarthome/app/";

    /**
     * MQTT 用浏览器打开后台地址是http://123.207.246.211:18083
     * 账号 admin 密码public
     */
    public static final String MQTT_HOST = "tcp://123.207.246.211:1883";//MQTT服务器地址

//    public static final String MQTT_HOST = "tcp://120.76.239.179:1883";//239.179MQTT服务器地址

    public static final String USER_INFO = "user_info";
    public static final String USER_NAME = "user_name";
    public static final String USER_PHONE = "user_phone";
    public static final String USER_TOKEN = "user_token";
    public static final String USER_PASSWORD = "user_password";
    public static final String USER_DEVICE_COUNT = "user_device_count";
    public static final String USER_ID = "user_id";

    public static final String  LANGUAGEFEEDBACK = "language_back";
    public static final String  VOICEBACK = "voice_back";
    public static final String  VOICE_STATU = "voice_statu";
    public static final String  WIFI_NAME = "wifi_name";

    public static final int[] GROUP_IMG = new int[]{R.mipmap.cantingda, R.mipmap.datingda, R.mipmap.chekuda, R.mipmap.chufangda, R.mipmap.chucangshida, R.mipmap.zhuwoda, R.mipmap.yangtaida, R.mipmap.shufangda, R.mipmap.ciwoda, R.mipmap.ertongfangda, R.mipmap.zoulangda, R.mipmap.xishoujianda, R.mipmap.weifenzuda};

    public static final String[] GROUP_NAME = new String[]{"餐厅", "大厅", "车库", "厨房", "储藏室", "主卧", "阳台", "书房", "次卧", "儿童房", "走廊", "浴室", "未分组"};

    public static final String[] DEVICE_NAME = new String[]{"台灯", "吊灯", "射灯", "床头灯", "书桌灯", "客厅灯", "餐厅灯", "主卧灯", "次卧灯", "浴室灯", "壁灯", "厨房灯", "廊灯", "阳台灯", "电视", "排气扇", "未命名"};

    public static final int[] DEVICE_IMG = new int[]{R.mipmap.taideng, R.mipmap.diaodeng, R.mipmap.shedeng, R.mipmap.chuangtoudeng, R.mipmap.shuzhuodeng, R.mipmap.ketingdeng, R.mipmap.cantingdeng, R.mipmap.zhuwodeng, R.mipmap.ciwodeng, R.mipmap.yushideng, R.mipmap.bideng, R.mipmap.chufangdeng, R.mipmap.langdeng, R.mipmap.yangtaideng, R.mipmap.dianshi, R.mipmap.paiqishan,R.mipmap.weimingmingd};

    public static final String[] SCENE_NAME = new String[]{"回家", "睡觉", "离家", "起床", "起夜", "会客", "影院", "就餐", "回房", "离房", "全开", "全关"};

    public static final int[] SCENE_IMG = new int[]{R.mipmap.huijia, R.mipmap.shuijiao, R.mipmap.lijia, R.mipmap.qichuang, R.mipmap.qiye, R.mipmap.huike, R.mipmap.yingyuan, R.mipmap.jiucan, R.mipmap.huifang, R.mipmap.lifang, R.mipmap.quankai, R.mipmap.quanguan};

    public static final int[] SCENE_BIG_IMG = new int[]{R.mipmap.huijia_yes, R.mipmap.shuijiao_yes, R.mipmap.lijia_yes, R.mipmap.qichuang_yes, R.mipmap.qiye_yes, R.mipmap.huike_yes, R.mipmap.yingyuan_yes, R.mipmap.jiucan_yes, R.mipmap.huifang_yes, R.mipmap.lifang_yes, R.mipmap.quankai_yes, R.mipmap.quanguan_yes};

    public static final String API_LOGIN = "login";//登录
    public static final String API_LOGOUT = "loginout";//登出
    public static final String API_GET_VERIFY_CODE = "getVerifyCode";//获取验证码
    public static final String API_SAVE_USER = "user/saveuser";//主账户注册
    public static final String API_SAVE_SUB_USER = "user/saveSubUser";//副账户注册
    public static final String API_RESET_PASS = "user/resetpass";//通过手机号重置密码
    public static final String API_RESET_PASS_EMAIL = "user/resetpassemail";//通过邮箱重置密码
    public static final String API_SEND_EMAIL = "sendEmail";//通过邮箱获取验证码
    public static final String API_DEVICE_LIST = "device/list";//设备列表
    public static final String API_DEVICE_DELETE = "device/delete";//删除设备
    public static final String API_RE_PASSWORD = "user/repassword";//修改密码
    public static final String API_CHECK_PASSWORD = "checkpassword";//效验密码
    public static final String API_ABOUT_US = "aboutus";//关于我们
    public static final String API_QUERY_GROUP_INFO = "group/queryGroupinfo";//分组列表信息
    public static final String API_GROUP_LIST = "group/list";//分组列表
    public static final String API_UPDATE_DEVICE_GROUP = "device/updateDeviceGroup";//修改设备分组
    public static final String API_SAVE_GROUP = "group/savegroup";//添加分组
    public static final String API_UPDATE_GROUP = "group/updategroup";//修改分组
    public static final String API_DELETE_GROUP = "group/deletegroup";//删除分组
    public static final String API_SCENE_LIST = "scene/list";//场景列表
    public static final String API_SAVE_SCENE = "scene/saveScene";//添加场景
    public static final String API_DELETE_SCENE = "scene/deletescene";//删除场景
    public static final String API_DELETE_SCENE_DEVICE = "scene/deletedevice";//删除场景中的设备
    public static final String API_OPEN_SCENE = "scene/openScene";//启动场景
    public static final String API_SCENE_ADD_DEVICE = "scene/addDevice";//场景添加设备
    public static final String API_OPEN_DEVICE = "device/openDevice";//启动设备
    public static final String API_QUERY_SCENE_DETAIL = "scene/querySceneDetail";//场景设备明细
    public static final String API_UPDATE_DEVICE_STATUS = "scene/updateDeviceStatus";//保存场景设置
    public static final String API_CLOSE_DEVICE = "device/closeDevice";//关闭设备
    public static final String API_UPDATE_DEVICE = "device/updateDevice";//关闭设备
    public static final String API_FEED_BACK_SAVE = "feedback/save";//提交反馈
    public static final String API_QUERY_SUB_USER = "user/querySubUser";//查询子账户
    public static final String API_OPEN_ALL = "group/openAll";//全开
    public static final String API_CLOSE_ALL = "group/closeAll";//全关
    public static final String API_QUERY_SUB_USER_DEVICE = "user/querySubUserDevice";//查询子账户下的设备
    public static final String API_SET_POWER = "device/setpower";//设置副账户权限
    public static final String API_SET_PRIVATE = "device/setprivate";//关闭公共权限
    public static final String API_SET_PUBLIC = "device/setpublic";//开启公共权限
    public static final String API_QUERY_APP_VERSION = "queryappversion";//检查更新
    public static final String API_QUERY_PRIVATE_DEVICE = "group/queryprivatedevice";//专属设备列表
    public static final String API_SAVE_PRIVATE_DEVICE = "device/saveprivatedevice";//设为专属设备
	public static final String API_TRANS_FOR_MAIN_USER = "user/transformMainUser";//转为主账户
	public static final String API_ADD_DEVICE = "device/saveDevice";//新增设备
	public static final String API_QUERY_EXIST_DEVICE = "device/queryExistDevice";//查询设备是否存在
}
