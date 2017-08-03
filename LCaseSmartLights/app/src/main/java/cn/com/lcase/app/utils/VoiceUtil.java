package cn.com.lcase.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.R;
import cn.com.lcase.app.model.Device;
import cn.com.lcase.app.model.Scene;
import cn.com.lcase.app.model.SceneDetail;
import cn.com.lcase.app.pcmplayer.MyThread;
import cn.com.lcase.app.ui.DeskLampActivity;
import cn.com.lcase.app.ui.DeviceActivity;
import cn.com.lcase.app.ui.MainActivity;
import cn.com.lcase.app.ui.TelevisionActivity;
import cn.com.lcase.app.ui.VoiceFinishListener;

import static android.content.Context.MODE_PRIVATE;
import static cn.com.lcase.app.MyApplication.mClient;
import static cn.com.lcase.app.R.raw.over;

/**
 * Created by admin on 2017/4/6.
 * 主要负责语音功能实现
 */

public class VoiceUtil {
    private String TAG = "VoiceUtil";

    final int EVENT_PLAY_OVER = 0x100;
    private Thread mThread = null;
    private byte[] data = null;
    private Handler mHandler;

    // 语音唤醒对象
    private VoiceWakeuper mVoiceWakeuper;
    // 语义理解对象（语音到语义）。
    private SpeechUnderstander mSpeechUnderstander;
    // 语音听写对象
    private SpeechRecognizer mSpeechRecognizer;
    // 语音合成对象
    private SpeechSynthesizer mSpeechSynthesizer;

    // 唤醒结果内容
    private String ivw_result;
    // 识别结果内容
    private String iat_result;
    // 设置门限值 ： 门限值越低越容易被唤醒
    private final static int MAX = 60;
    private final static int MIN = 20;
    private int curThresh = MIN;
    private String threshStr = "门限值：";


    // 缓存
    private SharedPreferences mSharedPreferences;
    // 本地语法文件
    private String mLocalGrammar = null;
    // 本地词典
    private String mLocalLexicon = null;
    // 云端语法文件
    private String mCloudGrammar = null;

    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String GRAMMAR_TYPE_BNF = "bnf";

    private String mEngineType = null;
    // 语记安装助手类
    //ApkInstaller mInstaller ;


    public String ip;
    public int port;
    public boolean isFirst = true;
    private Button open;
    private Button close;
    private Handler handler;

    private Context context;
    private Activity activity;
    private MediaPlayer mMediaPlayer;

    private String tempSayresult = "";
    private String tempDevice = "";
    private int tempSayInt;

    public void setVoiceFinishListener(VoiceFinishListener voiceFinishListener) {
        this.voiceFinishListener = voiceFinishListener;
    }

    private VoiceFinishListener voiceFinishListener;


    public VoiceUtil(Context context) {
        this.context = context;
        initData();
    }

    private void initData() {
        mEngineType = SpeechConstant.TYPE_CLOUD;
        mHandler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == EVENT_PLAY_OVER) {
                    mThread = null;
                    showTip("已唤醒,进入输入命令流程,请说话");
                    // 设置参数
                    // 开始听写
                    startRecognize();
//					startUnderstand();
                }
            }
        };
        // 加载识唤醒地资源，resPath为本地识别资源路径
        StringBuffer param = new StringBuffer();
        String resPath = ResourceUtil.generateResourcePath(context,
                ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + context.getString(R.string.app_id) + ".jet");

        param.append(ResourceUtil.IVW_RES_PATH + "=" + resPath);
        param.append("," + ResourceUtil.ENGINE_START + "=" + SpeechConstant.ENG_IVW);
        boolean ret = SpeechUtility.getUtility().setParameter(
                ResourceUtil.ENGINE_START, param.toString());
        if (!ret) {
            Log.d(TAG, "启动本地引擎失败！");
        }
        // 初始化唤醒对象
        mVoiceWakeuper = VoiceWakeuper.createWakeuper(context, null);
        // 初始化对象
        mSpeechUnderstander = SpeechUnderstander.createUnderstander(context, null);
        // 初始化识别对象
        mSpeechRecognizer = SpeechRecognizer.createRecognizer(context, mInitListener);
        // 初始化合成对象
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(context, null);


        handler = new Handler();

/********************************
 * 在线命令词识别 相关贴。
 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=13522&fromuid=68584
 (出处: 语音云社区)
 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=12540&extra=page%3D1
 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=11994&extra=page%3D1
 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=14116&extra=page%3D4
 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=13612&extra=page%3D5
 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=12168&extra=page%3D8
 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=10394&extra=page%3D11
 * ******************************/

        // 初始化语法、命令词
        //mLocalLexicon = "张海羊\n刘婧\n王锋\n";
        mLocalGrammar = FucUtil.readFile(context, "call.bnf", "utf-8"); //离线命令词
        //关于离线命令词识别， 一个APP, 只能 有一个 bnf文档，这个bnf文档中， 只能一个!start <callstart> , start 关键字
        mCloudGrammar = FucUtil.readFile(context, "grammar_sample.abnf", "utf-8"); //在线命令词，语法文件，请参考语法规范，不同指令应该分开来些。

        // 获取联系人，本地更新词典时使用
        //	ContactManager mgr = ContactManager.createManager(IntelliTalking.this, mContactListener);
        //mgr.asyncQueryAllContactsName();
        mSharedPreferences = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);
        initProgramme();
        startWakeup();
    }

    // 语法、词典临时变量
    String mContent;
    // 函数调用返回值
    int ret = 0;

    private void initProgramme() {
        // 本地-构建语法文件，生成语法id
        if (mEngineType.equals(SpeechConstant.TYPE_LOCAL)) {

            mContent = new String(mLocalGrammar);
            mSpeechRecognizer.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
            //指定引擎类型
            mSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
            ret = mSpeechRecognizer.buildGrammar(GRAMMAR_TYPE_BNF, mContent, mLocalGrammarListener);
            if (ret != ErrorCode.SUCCESS) {
                if (ret == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                    //未安装则跳转到提示安装页面
                    //mInstaller.install();
                } else {
                    showTip("语法构建失败,错误码：" + ret);
                    palySound(R.raw.failure);
                }
            }
        }
        // 在线-构建语法文件，生成语法id，语法id
        else {

            //((EditText)findViewById(R.id.isr_text)).setText(mCloudGrammar);
            mContent = new String(mCloudGrammar);
            //指定引擎类型
            mSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
            mSpeechRecognizer.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
            ret = mSpeechRecognizer.buildGrammar(GRAMMAR_TYPE_ABNF, mContent, mCloudGrammarListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("语法构建失败,错误码：" + ret);
                palySound(R.raw.failure);
            }

        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            }
        }
    };

    private void startRecognize() {
        palySound(R.raw.open);
        iat_result = "";
        boolean result = false;
        //设置识别引擎
        mSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回结果为json格式
        mSpeechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");

        if ("cloud".equalsIgnoreCase(mEngineType)) {
            String grammarId = mSharedPreferences.getString(KEY_GRAMMAR_ABNF_ID, null);
            if (TextUtils.isEmpty(grammarId)) {
                result = false;
            } else {
                //设置云端识别使用的语法id
                mSpeechRecognizer.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
                result = true;
            }
        } else {
            //设置本地识别使用语法id
            mSpeechRecognizer.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");
            //设置本地识别的门限值
            mSpeechRecognizer.setParameter(SpeechConstant.ASR_THRESHOLD, "30");
            result = true;
        }

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/asr.wav");

        // 开始听写流程
        mSpeechRecognizer.startListening(mRecognizerListener);
    }

    private void finishStatue() {
        if (voiceFinishListener != null) {
            voiceFinishListener.onfinish();
            setVoiceFinishListener(null);
        }
    }


    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {


        @Override
        public void onBeginOfSpeech() {
//            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            mVoiceWakeuper.startListening(mWakeuperListener);
            showTip(error.getPlainDescription(true));
            palySound(R.raw.failure);
            cancelAnimation();
            finishStatue();
        }

        @Override
        public void onEndOfSpeech() {
//            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            finishStatue();
            if (null != results) {
                Log.d(TAG, "recognizer result：" + results.getResultString());
                String text;
                if ("cloud".equalsIgnoreCase(mEngineType)) {
                    text = JsonParser.parseGrammarResult(results.getResultString());
                } else {
                    text = JsonParser.parseLocalGrammarResult(results.getResultString());
                }
                //switch
                SwitchResult(text);
//                showTip(text);
                Log.d(TAG, "SwitchResult" + text);
            } else {
                Log.d(TAG, "recognizer result : null");
            }

        }

        @Override
        public void onVolumeChanged(int volume, byte[] bytes) {
//            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }


        class ResultString {
            public boolean onoff;
            public String device = "无匹配";
        }

        /**
         * 一组命令
         */
        private void groupCommand(String name) {
            List<SceneDetail> list = new ArrayList<>();
            for (Scene scene : MyApplication.getListScene()) {
                if (name.equals(scene.getScenename())) {
                    list.addAll(scene.getSceneDetail());
                    for (int i = 0; i < list.size(); i++) {
                        SceneDetail device = list.get(i);
                        if (!TextUtils.isEmpty(device.getCode())) {
                            if ("1".equals(device.getType())) {
                                if (device.isOnoff()) {
                                    MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));
                                } else {
                                    MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));
                                }
                            } else {
                                if (device.isOnoff()) {
                                    //         电视机的开
                                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000001", "0000"));
                                } else {
                                    //         电视机的关
                                    MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000000", "0000"));
                                }
                            }
                        }
                    }
                    break;
                }
            }

        }

        private void exclusiveCommand(Boolean isopen) {
//            比如主卧  然后对应主卧灯
            String str = "";
            if (!TextUtils.isEmpty(MyApplication.getGroupName())) {
                str = MyApplication.getGroupName();
                if (str.length() > 3) {
                    str = str.substring(0, 2);
                }
            }
            for (Device device : MyApplication.getExclusiveDeviceList()) {
                if ("1".equals(device.getType()) && device.getName().contains(str)) {
                    if (isopen) {
                        MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));
                    } else {
                        MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));
                    }
//                    break;
                }
              /*  if (!TextUtils.isEmpty(device.getCode())) {
                    if ("1".equals(device.getType())) {
                        if (isopen) {
                            MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));
                        } else {
                            MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));
                        }

                    } else {
                        if (isopen) {
                            //         电视机的开
                            MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000001", "0000"));
                        } else {
                            //         电视机的关
                            MyApplication.mClient.publish(BizUtil.tvCommand(device.getCode(), "000001", "000000", "0000"));
                        }

                    }
                }*/
            }
        }

        private void exclusiveCommand2(Boolean isopen, String name) {
            if (!TextUtils.isEmpty(name)) {
                for (Device device : MyApplication.getExclusiveDeviceList()) {
                    if ("1".equals(device.getType()) && device.getName().contains(name)) {
                        if (isopen) {
                            MyApplication.mClient.publish(BizUtil.OnCommand(device.getCode()));
                        } else {
                            MyApplication.mClient.publish(BizUtil.OffCommand(device.getCode()));
                        }
//                    break;
                    }
                }

            }

        }

        private Boolean isSetexclusiveCommand() {
//            比如主卧  然后对应主卧灯
            String str = "";
            if (!TextUtils.isEmpty(MyApplication.getGroupName())) {
                str = MyApplication.getGroupName();
                if (str.length() > 3) {
                    str = str.substring(0, 2);
                }
            }
            for (Device device : MyApplication.getExclusiveDeviceList()) {
                if ("1".equals(device.getType()) && device.getName().contains(str)) {
                    return true;
                }

            }
            return false;
        }

        /**
         * 是否有该设备
         * @param name
         * @return
         */
        private Boolean isSetexclusiveCommand2(String name) {
            if (!TextUtils.isEmpty(name)) {
                for (Device device : MyApplication.getExclusiveDeviceList()) {
                    if ("1".equals(device.getType()) && device.getName().contains(name)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 打开专属
         * @param resulthandle
         * @param destination
         */
        private void switchDoOpeanExclusiveResult(ResultString resulthandle, String destination, Boolean isOpen) {
            Pattern pWashroom = Pattern.compile(destination);
            Matcher mWashroom = pWashroom.matcher(resulthandle.device);
            while (mWashroom.find()) {
                try {
                    exclusiveCommand(isOpen);
//                    showTip(hint);
                }
//                catch (MqttException e)
                catch (Exception e) {
//                    showTip("mqttexception");
                    showTip("设备已离线");
                }
            }
        }

        /**
         * 打开专属
         * @param resulthandle
         * @param destination
         */
        private void switchDoOpeanExclusiveResult2(ResultString resulthandle, String destination, Boolean isOpen) {
            Pattern pWashroom = Pattern.compile(destination);
            Matcher mWashroom = pWashroom.matcher(resulthandle.device);
            while (mWashroom.find()) {
                try {
                    //           |我的台灯|我的床头灯|我的浴室灯|我的风扇|我的排气扇|我的书桌灯|我的壁灯|我的吊灯
                    String name = destination.substring(4, destination.length());//台灯
                    exclusiveCommand2(isOpen, name);
//                    showTip(hint);
                }
//                catch (MqttException e)
                catch (Exception e) {
//                    showTip("mqttexception");
                    showTip("设备已离线");
                }
            }
        }


        private void switchDoGroupResult2(String hint, ResultString resulthandle, String destination, String destination2) {
            Pattern pWashroom = Pattern.compile(destination);
            Matcher mWashroom = pWashroom.matcher(resulthandle.device);
            while (mWashroom.find()) {
                try {
                    groupCommand(destination2);
//                    showTip(hint);
                    Log.d("VoiceUtil", "hint:" + hint);
                }
//                catch (MqttException e)
                catch (Exception e) {
//                    showTip("mqttexception");
//                    如果Mqtt异常了就会报错
                    showTip("设备已离线");
                }
            }
        }

        private void switchDoGroupResult(String hint, ResultString resulthandle, String destination) {
            Pattern pWashroom = Pattern.compile(destination);
            Matcher mWashroom = pWashroom.matcher(resulthandle.device);
            while (mWashroom.find()) {
                try {
                    groupCommand(hint);
//                    showTip(hint);
                    Log.d("VoiceUtil", "hint:" + hint);
                }
//                catch (MqttException e)
                catch (Exception e) {
//                    showTip("mqttexception");
                    showTip("设备已离线");
                }
            }
        }

        private void switchDoResult(String destination, ResultString resulthandle, String hint, String command) {
            Pattern pWashroom = Pattern.compile(destination);
            Matcher mWashroom = pWashroom.matcher(resulthandle.device);
            while (mWashroom.find()) {
                try {
                    mClient.publish(command);
//                    showTip(hint);
                    Log.d("VoiceUtil", "hint:" + hint);
                }
//                catch (MqttException e)
                catch (Exception e) {
//                    showTip("mqttexception");
                    showTip("设备已离线");
                }
            }
        }

        private void SwitchResult(String text) {
            //TODO: 置信度应该大于某个值才生效，避免含糊指令
           /*
           * 【结果】 打开大厅灯 【置信度】 53
           * 【结果】 关闭大厅灯 【置信度】 45
           * 【结果】 关大厅灯 【置信度】 45
           * */
            //提取置信度 第一条
            //得到device handle
            ResultString resulthandle = GetFirstHandle(text);

            //实行指令open close  exp:大厅灯
            //TODO: 效率高的命令词查询算法  //ps 频道号在  channel_list.xml，查询 命令词在 device_list.xml 命令词仅供参考。
            // 最多相同五盏灯
//            String onoff = resulthandle.onoff ? BizUtil.OnCommand("71F75C4437FA3263D8CB33188008FB7C1") : BizUtil.OffCommand("71F75C4437FA3263D8CB33188008FB7C1");
            switchDoResult("台灯", resulthandle, "为您控制灯", onffCommand("台灯", resulthandle.onoff));
            switchDoResult("吊灯", resulthandle, "为您控制灯", onffCommand("吊灯", resulthandle.onoff));
            switchDoResult("射灯", resulthandle, "为您控制灯", onffCommand("射灯", resulthandle.onoff));
            switchDoResult("床头灯", resulthandle, "为您控制灯", onffCommand("床头灯", resulthandle.onoff));
            switchDoResult("书桌灯", resulthandle, "为您控制灯", onffCommand("书桌灯", resulthandle.onoff));
            switchDoResult("客厅灯", resulthandle, "为您控制灯", onffCommand("客厅灯", resulthandle.onoff));
            switchDoResult("餐厅灯", resulthandle, "为您控制灯", onffCommand("餐厅灯", resulthandle.onoff));
            switchDoResult("主卧灯", resulthandle, "为您控制灯", onffCommand("主卧灯", resulthandle.onoff));
            switchDoResult("次卧灯", resulthandle, "为您控制灯", onffCommand("次卧灯", resulthandle.onoff));
            switchDoResult("浴室灯", resulthandle, "为您控制灯", onffCommand("浴室灯", resulthandle.onoff));
            switchDoResult("壁灯", resulthandle, "为您控制灯", onffCommand("壁灯", resulthandle.onoff));
            switchDoResult("厨房灯", resulthandle, "为您控制灯", onffCommand("厨房灯", resulthandle.onoff));
            switchDoResult("廊灯", resulthandle, "为您控制灯", onffCommand("廊灯", resulthandle.onoff));
            switchDoResult("阳台灯", resulthandle, "为您控制灯", onffCommand("阳台灯", resulthandle.onoff));
            switchDoResult("风扇", resulthandle, "为您控制灯", onffCommand("风扇", resulthandle.onoff));
            switchDoResult("排气扇", resulthandle, "为您控制灯", onffCommand("排气扇", resulthandle.onoff));

            switchDoResult("台灯一", resulthandle, "为您控制灯", onffCommand("台灯一", resulthandle.onoff));
            switchDoResult("吊灯一", resulthandle, "为您控制灯", onffCommand("吊灯一", resulthandle.onoff));
            switchDoResult("射灯一", resulthandle, "为您控制灯", onffCommand("射灯一", resulthandle.onoff));
            switchDoResult("床头灯一", resulthandle, "为您控制灯", onffCommand("床头灯一", resulthandle.onoff));
            switchDoResult("书桌灯一", resulthandle, "为您控制灯", onffCommand("书桌灯一", resulthandle.onoff));
            switchDoResult("客厅灯一", resulthandle, "为您控制灯", onffCommand("客厅灯一", resulthandle.onoff));
            switchDoResult("餐厅灯一", resulthandle, "为您控制灯", onffCommand("餐厅灯一", resulthandle.onoff));
            switchDoResult("主卧灯一", resulthandle, "为您控制灯", onffCommand("主卧灯一", resulthandle.onoff));
            switchDoResult("次卧灯一", resulthandle, "为您控制灯", onffCommand("次卧灯一", resulthandle.onoff));
            switchDoResult("浴室灯一", resulthandle, "为您控制灯", onffCommand("浴室灯一", resulthandle.onoff));
            switchDoResult("壁灯一", resulthandle, "为您控制灯", onffCommand("壁灯一", resulthandle.onoff));
            switchDoResult("厨房灯一", resulthandle, "为您控制灯", onffCommand("厨房灯一", resulthandle.onoff));
            switchDoResult("廊灯一", resulthandle, "为您控制灯", onffCommand("廊灯一", resulthandle.onoff));
            switchDoResult("阳台灯一", resulthandle, "为您控制灯", onffCommand("阳台灯一", resulthandle.onoff));
            switchDoResult("风扇一", resulthandle, "为您控制灯", onffCommand("风扇一", resulthandle.onoff));
            switchDoResult("排气扇一", resulthandle, "为您控制灯", onffCommand("排气扇一", resulthandle.onoff));

            switchDoResult("台灯二", resulthandle, "为您控制灯", onffCommand("台灯二", resulthandle.onoff));
            switchDoResult("吊灯二", resulthandle, "为您控制灯", onffCommand("吊灯二", resulthandle.onoff));
            switchDoResult("射灯二", resulthandle, "为您控制灯", onffCommand("射灯二", resulthandle.onoff));
            switchDoResult("床头灯二", resulthandle, "为您控制灯", onffCommand("床头灯二", resulthandle.onoff));
            switchDoResult("书桌灯二", resulthandle, "为您控制灯", onffCommand("书桌灯二", resulthandle.onoff));
            switchDoResult("客厅灯二", resulthandle, "为您控制灯", onffCommand("客厅灯二", resulthandle.onoff));
            switchDoResult("餐厅灯二", resulthandle, "为您控制灯", onffCommand("餐厅灯二", resulthandle.onoff));
            switchDoResult("主卧灯二", resulthandle, "为您控制灯", onffCommand("主卧灯二", resulthandle.onoff));
            switchDoResult("次卧灯二", resulthandle, "为您控制灯", onffCommand("次卧灯二", resulthandle.onoff));
            switchDoResult("浴室灯二", resulthandle, "为您控制灯", onffCommand("浴室灯二", resulthandle.onoff));
            switchDoResult("壁灯二", resulthandle, "为您控制灯", onffCommand("壁灯二", resulthandle.onoff));
            switchDoResult("厨房灯二", resulthandle, "为您控制灯", onffCommand("厨房灯二", resulthandle.onoff));
            switchDoResult("廊灯二", resulthandle, "为您控制灯", onffCommand("廊灯二", resulthandle.onoff));
            switchDoResult("阳台灯二", resulthandle, "为您控制灯", onffCommand("阳台灯二", resulthandle.onoff));
            switchDoResult("风扇二", resulthandle, "为您控制灯", onffCommand("风扇二", resulthandle.onoff));
            switchDoResult("排气扇二", resulthandle, "为您控制灯", onffCommand("排气扇二", resulthandle.onoff));

            switchDoResult("台灯三", resulthandle, "为您控制灯", onffCommand("台灯三", resulthandle.onoff));
            switchDoResult("吊灯三", resulthandle, "为您控制灯", onffCommand("吊灯三", resulthandle.onoff));
            switchDoResult("射灯三", resulthandle, "为您控制灯", onffCommand("射灯三", resulthandle.onoff));
            switchDoResult("床头灯三", resulthandle, "为您控制灯", onffCommand("床头灯三", resulthandle.onoff));
            switchDoResult("书桌灯三", resulthandle, "为您控制灯", onffCommand("书桌灯三", resulthandle.onoff));
            switchDoResult("客厅灯三", resulthandle, "为您控制灯", onffCommand("客厅灯三", resulthandle.onoff));
            switchDoResult("餐厅灯三", resulthandle, "为您控制灯", onffCommand("餐厅灯三", resulthandle.onoff));
            switchDoResult("主卧灯三", resulthandle, "为您控制灯", onffCommand("主卧灯三", resulthandle.onoff));
            switchDoResult("次卧灯三", resulthandle, "为您控制灯", onffCommand("次卧灯三", resulthandle.onoff));
            switchDoResult("浴室灯三", resulthandle, "为您控制灯", onffCommand("浴室灯三", resulthandle.onoff));
            switchDoResult("壁灯三", resulthandle, "为您控制灯", onffCommand("壁灯三", resulthandle.onoff));
            switchDoResult("厨房灯三", resulthandle, "为您控制灯", onffCommand("厨房灯三", resulthandle.onoff));
            switchDoResult("廊灯三", resulthandle, "为您控制灯", onffCommand("廊灯三", resulthandle.onoff));
            switchDoResult("阳台灯三", resulthandle, "为您控制灯", onffCommand("阳台灯三", resulthandle.onoff));
            switchDoResult("风扇三", resulthandle, "为您控制灯", onffCommand("风扇三", resulthandle.onoff));
            switchDoResult("排气扇三", resulthandle, "为您控制灯", onffCommand("排气扇三", resulthandle.onoff));


            switchDoResult("台灯四", resulthandle, "为您控制灯", onffCommand("台灯四", resulthandle.onoff));
            switchDoResult("吊灯四", resulthandle, "为您控制灯", onffCommand("吊灯四", resulthandle.onoff));
            switchDoResult("射灯四", resulthandle, "为您控制灯", onffCommand("射灯四", resulthandle.onoff));
            switchDoResult("床头灯四", resulthandle, "为您控制灯", onffCommand("床头灯四", resulthandle.onoff));
            switchDoResult("书桌灯四", resulthandle, "为您控制灯", onffCommand("书桌灯四", resulthandle.onoff));
            switchDoResult("客厅灯四", resulthandle, "为您控制灯", onffCommand("客厅灯四", resulthandle.onoff));
            switchDoResult("餐厅灯四", resulthandle, "为您控制灯", onffCommand("餐厅灯四", resulthandle.onoff));
            switchDoResult("主卧灯四", resulthandle, "为您控制灯", onffCommand("主卧灯四", resulthandle.onoff));
            switchDoResult("次卧灯四", resulthandle, "为您控制灯", onffCommand("次卧灯四", resulthandle.onoff));
            switchDoResult("浴室灯四", resulthandle, "为您控制灯", onffCommand("浴室灯四", resulthandle.onoff));
            switchDoResult("壁灯四", resulthandle, "为您控制灯", onffCommand("壁灯四", resulthandle.onoff));
            switchDoResult("厨房灯四", resulthandle, "为您控制灯", onffCommand("厨房灯四", resulthandle.onoff));
            switchDoResult("廊灯四", resulthandle, "为您控制灯", onffCommand("廊灯四", resulthandle.onoff));
            switchDoResult("阳台灯四", resulthandle, "为您控制灯", onffCommand("阳台灯四", resulthandle.onoff));
            switchDoResult("风扇四", resulthandle, "为您控制灯", onffCommand("风扇四", resulthandle.onoff));
            switchDoResult("排气扇四", resulthandle, "为您控制灯", onffCommand("排气扇四", resulthandle.onoff));
// 最多相同五盏灯
            switchDoResult("电视", resulthandle, "为您打开电视", tvCommand("电视", "000001", "000001", "0000"));
            switchDoResult("中央一台", resulthandle, "为您打开中央一台", tvCommand("电视", "010000", "000000", Integer.toHexString(9001)));
            switchDoResult("央视一套", resulthandle, "为您打开央视一套", tvCommand("电视", "010000", "000000", Integer.toHexString(9001)));
            switchDoResult("中央二台", resulthandle, "为您打开中央二台", tvCommand("电视", "010000", "000000", Integer.toHexString(9002)));
            switchDoResult("央视二套", resulthandle, "为您打开央视二套", tvCommand("电视", "010000", "000000", Integer.toHexString(9002)));
            switchDoResult("中央三台", resulthandle, "为您打开中央三台", tvCommand("电视", "010000", "000000", Integer.toHexString(9003)));
            switchDoResult("央视三套", resulthandle, "为您打开央视三套", tvCommand("电视", "010000", "000000", Integer.toHexString(9003)));
            switchDoResult("中央四台", resulthandle, "为您打开中央四台", tvCommand("电视", "010000", "000000", Integer.toHexString(9004)));
            switchDoResult("央视四套", resulthandle, "为您打开央视四套", tvCommand("电视", "010000", "000000", Integer.toHexString(9004)));
            switchDoResult("中央五台", resulthandle, "为您打开中央五台", tvCommand("电视", "010000", "000000", Integer.toHexString(9005)));
            switchDoResult("央视五套", resulthandle, "为您打开央视五套", tvCommand("电视", "010000", "000000", Integer.toHexString(9005)));
            switchDoResult("中央六台", resulthandle, "为您打开中央六台", tvCommand("电视", "010000", "000000", Integer.toHexString(9006)));
            switchDoResult("央视六套", resulthandle, "为您打开央视六套", tvCommand("电视", "010000", "000000", Integer.toHexString(9006)));
            switchDoResult("中央七台", resulthandle, "为您打开中央七台", tvCommand("电视", "010000", "000000", Integer.toHexString(9007)));
            switchDoResult("央视七套", resulthandle, "为您打开央视七套", tvCommand("电视", "010000", "000000", Integer.toHexString(9007)));
            switchDoResult("中央八台", resulthandle, "为您打开中央八台", tvCommand("电视", "010000", "000000", Integer.toHexString(9008)));
            switchDoResult("央视八套", resulthandle, "为您打开央视八套", tvCommand("电视", "010000", "000000", Integer.toHexString(9008)));
            switchDoResult("中央九台", resulthandle, "为您打开中央九台", tvCommand("电视", "010000", "000000", Integer.toHexString(9009)));
            switchDoResult("央视九套", resulthandle, "为您打开央视九套", tvCommand("电视", "010000", "000000", Integer.toHexString(9009)));
            switchDoResult("中央十台", resulthandle, "为您打开中央十台", tvCommand("电视", "010000", "000000", Integer.toHexString(9010)));
            switchDoResult("央视十套", resulthandle, "为您打开央视十套", tvCommand("电视", "010000", "000000", Integer.toHexString(9010)));
            switchDoResult("中央十二台", resulthandle, "为您打开中央十二台", tvCommand("电视", "010000", "000000", Integer.toHexString(9012)));
            switchDoResult("央视十二套", resulthandle, "为您打开央视十二套", tvCommand("电视", "010000", "000000", Integer.toHexString(9012)));
            switchDoResult("央视少儿台", resulthandle, "为您打开央视少儿台", tvCommand("电视", "010000", "000000", Integer.toHexString(9014)));
            switchDoResult("体育赛事频道", resulthandle, "为您打开体育赛事频道", tvCommand("电视", "010000", "000000", Integer.toHexString(9016)));
//            switchDoResult("CCTV5+", resulthandle, "为您打开CCTV5+", tvCommand("电视", "010000", "000000", Integer.toHexString(9016)));
            switchDoResult("北京卫视台", resulthandle, "为您打开北京卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9021)));
            switchDoResult("天津卫视台", resulthandle, "为您打开天津卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9022)));
            switchDoResult("东方卫视台", resulthandle, "为您打开东方卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9023)));
            switchDoResult("湖南卫视台", resulthandle, "为您打开湖南卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9024)));
            switchDoResult("浙江卫视台", resulthandle, "为您打开浙江卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9025)));
            switchDoResult("江苏卫视台", resulthandle, "为您打开江苏卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9026)));
            switchDoResult("山东卫视台", resulthandle, "为您打开山东卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9027)));
            switchDoResult("广东卫视台", resulthandle, "为您打开广东卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9028)));
            switchDoResult("深圳卫视台", resulthandle, "为您打开深圳卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9029)));
            switchDoResult("湖北卫视台", resulthandle, "为您打开湖北卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9031)));
            switchDoResult("河北卫视台", resulthandle, "为您打开河北卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9032)));
            switchDoResult("福建东南卫视台", resulthandle, "为您打开福建东南卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9033)));
            switchDoResult("福建海峡卫视台", resulthandle, "为您打开福建海峡卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9034)));
            switchDoResult("黑龙江卫视台", resulthandle, "为您打开黑龙江卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9035)));
            switchDoResult("辽宁卫视台", resulthandle, "为您打开辽宁卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9036)));
            switchDoResult("江西卫视台", resulthandle, "为您打开江西卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9038)));
            switchDoResult("陕西卫视台", resulthandle, "为您打开陕西卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9039)));
            switchDoResult("重庆卫视台", resulthandle, "为您打开重庆卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(9040)));
            switchDoResult("中国交通", resulthandle, "为您打开中国交通", tvCommand("电视", "010000", "000000", Integer.toHexString(9088)));
            switchDoResult("中央十一台", resulthandle, "为您打开中央十一台", tvCommand("电视", "010000", "000000", Integer.toHexString(11)));
            switchDoResult("央视十一套", resulthandle, "为您打开央视十一套", tvCommand("电视", "010000", "000000", Integer.toHexString(11)));
            switchDoResult("央视新闻", resulthandle, "为您打开央视新闻", tvCommand("电视", "010000", "000000", Integer.toHexString(13)));
            switchDoResult("CCTV音乐", resulthandle, "为您打开CCTV音乐", tvCommand("电视", "010000", "000000", Integer.toHexString(15)));
            switchDoResult("国防军事", resulthandle, "为您打开国防军事", tvCommand("电视", "010000", "000000", Integer.toHexString(17)));
            switchDoResult("女性时尚", resulthandle, "为您打开女性时尚", tvCommand("电视", "010000", "000000", Integer.toHexString(18)));
            switchDoResult("央视精品", resulthandle, "为您打开央视精品", tvCommand("电视", "010000", "000000", Integer.toHexString(19)));
            switchDoResult("央视台球", resulthandle, "为您打开央视台球", tvCommand("电视", "010000", "000000", Integer.toHexString(20)));
            switchDoResult("电视指南", resulthandle, "为您打开电视指南", tvCommand("电视", "010000", "000000", Integer.toHexString(21)));
            switchDoResult("第一剧场", resulthandle, "为您打开第一剧场", tvCommand("电视", "010000", "000000", Integer.toHexString(22)));
            switchDoResult("怀旧剧场", resulthandle, "为您打开怀旧剧场", tvCommand("电视", "010000", "000000", Integer.toHexString(23)));
            switchDoResult("高尔夫网球", resulthandle, "为您打开高尔夫网球", tvCommand("电视", "010000", "000000", Integer.toHexString(24)));
            switchDoResult("风云音乐", resulthandle, "为您打开风云音乐", tvCommand("电视", "010000", "000000", Integer.toHexString(25)));
            switchDoResult("风云足球", resulthandle, "为您打开风云足球", tvCommand("电视", "010000", "000000", Integer.toHexString(26)));
            switchDoResult("世界地理", resulthandle, "为您打开世界地理", tvCommand("电视", "010000", "000000", Integer.toHexString(27)));
            switchDoResult("风云剧场", resulthandle, "为您打开风云剧场", tvCommand("电视", "010000", "000000", Integer.toHexString(28)));
            switchDoResult("证券资讯", resulthandle, "为您打开证券资讯", tvCommand("电视", "010000", "000000", Integer.toHexString(29)));
            switchDoResult("发现之旅", resulthandle, "为您打开发现之旅", tvCommand("电视", "010000", "000000", Integer.toHexString(30)));
            switchDoResult("央视四套(欧)", resulthandle, "为您打开央视四套(欧)", tvCommand("电视", "010000", "000000", Integer.toHexString(31)));
            switchDoResult("央视四套(美)", resulthandle, "为您打开央视四套(美)", tvCommand("电视", "010000", "000000", Integer.toHexString(32)));
//            switchDoResult("CCTV Document", resulthandle, "为您打开CCTV Document", tvCommand("电视", "010000", "000000", Integer.toHexString(33)));
//            switchDoResult("CCTV News", resulthandle, "为您打开CCTV News", tvCommand("电视", "010000", "000000", Integer.toHexString(34)));
            switchDoResult("央视新科动漫", resulthandle, "为您打开央视新科动漫", tvCommand("电视", "010000", "000000", Integer.toHexString(40)));
            switchDoResult("央视西语频道", resulthandle, "为您打开央视西语频道", tvCommand("电视", "010000", "000000", Integer.toHexString(41)));
            switchDoResult("央视法语频道", resulthandle, "为您打开央视法语频道", tvCommand("电视", "010000", "000000", Integer.toHexString(42)));
            switchDoResult("央视俄语频道", resulthandle, "为您打开央视俄语频道", tvCommand("电视", "010000", "000000", Integer.toHexString(43)));
            switchDoResult("央视阿语频道", resulthandle, "为您打开央视阿语频道", tvCommand("电视", "010000", "000000", Integer.toHexString(44)));
            switchDoResult("央视中学生", resulthandle, "为您打开央视中学生", tvCommand("电视", "010000", "000000", Integer.toHexString(46)));

            switchDoResult("广西卫视台", resulthandle, "为您打开广西卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(120)));
            switchDoResult("内蒙古卫视台", resulthandle, "为您打开内蒙古卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(121)));
            switchDoResult("海南旅游卫视台", resulthandle, "为您打开海南旅游卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(122)));
            switchDoResult("云南卫视台", resulthandle, "为您打开云南卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(123)));
            switchDoResult("贵州卫视台", resulthandle, "为您打开贵州卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(124)));
            switchDoResult("青海卫视台", resulthandle, "为您打开青海卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(125)));
            switchDoResult("宁夏卫视台", resulthandle, "为您打开宁夏卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(126)));
            switchDoResult("甘肃卫视台", resulthandle, "为您打开甘肃卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(127)));
            switchDoResult("吉林卫视台", resulthandle, "为您打开吉林卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(129)));
            switchDoResult("西藏卫视台", resulthandle, "为您打开西藏卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(131)));
            switchDoResult("新疆卫视台", resulthandle, "为您打开新疆卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(132)));
            switchDoResult("新疆兵团卫视台", resulthandle, "为您打开新疆兵团卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(133)));
            switchDoResult("福建厦门卫视台", resulthandle, "为您打开福建厦门卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(134)));
            switchDoResult("福建海峡卫视台", resulthandle, "为您打开福建海峡卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(135)));
            switchDoResult("广东南方卫视台", resulthandle, "为您打开广东南方卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(137)));
            switchDoResult("广东珠江卫视台", resulthandle, "为您打开广东珠江卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(138)));
            switchDoResult("陕西农林卫视台", resulthandle, "为您打开陕西农林卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(140)));
            switchDoResult("海南三沙卫视台", resulthandle, "为您打开海南三沙卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(141)));
            switchDoResult("健康卫视台", resulthandle, "为您打开健康卫视台", tvCommand("电视", "010000", "000000", Integer.toHexString(145)));

            switchDoResult("北京文艺频道", resulthandle, "为您打开北京文艺频道", tvCommand("电视", "010000", "000000", Integer.toHexString(301)));
            switchDoResult("北京科教频道", resulthandle, "为您打开北京科教频道", tvCommand("电视", "010000", "000000", Integer.toHexString(302)));
            switchDoResult("北京影视频道", resulthandle, "为您打开北京影视频道", tvCommand("电视", "010000", "000000", Integer.toHexString(303)));
            switchDoResult("北京财经频道", resulthandle, "为您打开北京财经频道", tvCommand("电视", "010000", "000000", Integer.toHexString(304)));
            switchDoResult("北京体育频道", resulthandle, "为您打开北京体育频道", tvCommand("电视", "010000", "000000", Integer.toHexString(305)));
            switchDoResult("北京生活频道", resulthandle, "为您打开北京生活频道", tvCommand("电视", "010000", "000000", Integer.toHexString(306)));
            switchDoResult("北京青年频道", resulthandle, "为您打开北京青年频道", tvCommand("电视", "010000", "000000", Integer.toHexString(307)));
            switchDoResult("北京新闻频道", resulthandle, "为您打开北京新闻频道", tvCommand("电视", "010000", "000000", Integer.toHexString(308)));
            switchDoResult("北京纪实频道", resulthandle, "为您打开北京纪实频道", tvCommand("电视", "010000", "000000", Integer.toHexString(309)));
            switchDoResult("上海新闻综合", resulthandle, "为您打开上海新闻综合", tvCommand("电视", "010000", "000000", Integer.toHexString(501)));
            switchDoResult("上海电视剧", resulthandle, "为您打开上海电视剧", tvCommand("电视", "010000", "000000", Integer.toHexString(502)));
            switchDoResult("东方电影频道", resulthandle, "为您打开东方电影频道", tvCommand("电视", "010000", "000000", Integer.toHexString(503)));
            switchDoResult("上海五星体育", resulthandle, "为您打开上海五星体育", tvCommand("电视", "010000", "000000", Integer.toHexString(504)));
            switchDoResult("上海艺术人文", resulthandle, "为您打开上海艺术人文", tvCommand("电视", "010000", "000000", Integer.toHexString(506)));
            switchDoResult("上海娱乐频道", resulthandle, "为您打开上海娱乐频道", tvCommand("电视", "010000", "000000", Integer.toHexString(507)));
            switchDoResult("上海纪实频道", resulthandle, "为您打开上海纪实频道", tvCommand("电视", "010000", "000000", Integer.toHexString(510)));
            switchDoResult("上海星尚频道", resulthandle, "为您打开上海星尚频道", tvCommand("电视", "010000", "000000", Integer.toHexString(511)));
            switchDoResult("上海外语频道", resulthandle, "为您打开上海外语频道", tvCommand("电视", "010000", "000000", Integer.toHexString(514)));
            switchDoResult("上海教育频道", resulthandle, "为您打开上海教育频道", tvCommand("电视", "010000", "000000", Integer.toHexString(515)));
            switchDoResult("广东国际频道", resulthandle, "为您打开广东国际频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1100)));
            switchDoResult("广东新闻频道", resulthandle, "为您打开广东新闻频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1103)));
            switchDoResult("广东公共频道", resulthandle, "为您打开广东公共频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1104)));
            switchDoResult("广东体育频道", resulthandle, "为您打开广东体育频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1105)));
            switchDoResult("广东经济科教台", resulthandle, "为您打开广东经济科教台", tvCommand("电视", "010000", "000000", Integer.toHexString(1106)));
            switchDoResult("广东综艺频道", resulthandle, "为您打开广东综艺频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1107)));
            switchDoResult("广东影视频道", resulthandle, "为您打开广东影视频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1108)));
            switchDoResult("广东少儿频道", resulthandle, "为您打开广东少儿频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1109)));
            switchDoResult("广东现代教育", resulthandle, "为您打开广东现代教育", tvCommand("电视", "010000", "000000", Integer.toHexString(1110)));
            switchDoResult("广东房产频道", resulthandle, "为您打开广东房产频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1111)));
            switchDoResult("广东会展频道", resulthandle, "为您打开广东会展频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1112)));
            switchDoResult("深圳都市频道", resulthandle, "为您打开深圳都市频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1119)));
            switchDoResult("深圳电视剧频道", resulthandle, "为您打开深圳电视剧频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1120)));
            switchDoResult("深圳体育健康台", resulthandle, "为您打开深圳体育健康台", tvCommand("电视", "010000", "000000", Integer.toHexString(1121)));
            switchDoResult("深圳财经生活台", resulthandle, "为您打开深圳财经生活台", tvCommand("电视", "010000", "000000", Integer.toHexString(1122)));
            switchDoResult("深圳娱乐频道", resulthandle, "为您打开深圳娱乐频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1123)));
            switchDoResult("深圳公共频道", resulthandle, "为您打开深圳公共频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1124)));
            switchDoResult("深圳少儿频道", resulthandle, "为您打开深圳少儿频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1125)));
            switchDoResult("深圳法治频道", resulthandle, "为您打开深圳法治频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1126)));
            switchDoResult("广州综合频道", resulthandle, "为您打开广州综合频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1131)));
            switchDoResult("广州新闻频道", resulthandle, "为您打开广州新闻频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1132)));
            switchDoResult("广州影视频道", resulthandle, "为您打开广州影视频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1133)));
            switchDoResult("广州竞赛频道", resulthandle, "为您打开广州竞赛频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1134)));
            switchDoResult("广州经济频道", resulthandle, "为您打开广州经济频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1135)));
            switchDoResult("广州少儿频道", resulthandle, "为您打开广州少儿频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1136)));
            switchDoResult("广州生活频道", resulthandle, "为您打开广州生活频道", tvCommand("电视", "010000", "000000", Integer.toHexString(1137)));


//            针对于场景
            switchDoGroupResult("回家", resulthandle, "回家");
            switchDoGroupResult("睡觉", resulthandle, "睡觉");
            switchDoGroupResult("离家", resulthandle, "离家");
            switchDoGroupResult("起床", resulthandle, "我起来了");
            switchDoGroupResult("起夜", resulthandle, "起夜");
            switchDoGroupResult("会客", resulthandle, "见客人");
            switchDoGroupResult("影院", resulthandle, "看电影");
            switchDoGroupResult("就餐", resulthandle, "就餐");
            switchDoGroupResult("回房", resulthandle, "回房");
            switchDoGroupResult("离房", resulthandle, "离房");
            switchDoGroupResult("全开", resulthandle, "打开全部灯");
            switchDoGroupResult("全开", resulthandle, "打开所有灯");
            switchDoGroupResult("全关", resulthandle, "关闭全部灯");
            switchDoGroupResult("全关", resulthandle, "关闭所有灯");
            switchDoGroupResult("就餐", resulthandle, "去吃饭");

            //            针对于专属设备
          /*  switchDoOpeanExclusiveResult(resulthandle, "打开我的灯");
            switchDoOpeanExclusiveResult(resulthandle, "关闭我的灯");
            switchDoOpeanExclusiveResult(resulthandle, "打开灯");
            switchDoOpeanExclusiveResult(resulthandle, "关闭灯");*/
//            switchDoOpeanExclusiveResult(resulthandle, "我的灯");
//            switchDoOpeanExclusiveResult(resulthandle, "我的灯");
//            switchDoOpeanExclusiveResult(resulthandle, "灯");
//            switchDoOpeanExclusiveResult(resulthandle, "灯");

            switchDoOpeanExclusiveResult(resulthandle, "打开我的灯", true);
            switchDoOpeanExclusiveResult(resulthandle, "关闭我的灯", false);

            switchDoOpeanExclusiveResult2(resulthandle, "打开我的台灯", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的台灯", false);
            switchDoOpeanExclusiveResult2(resulthandle, "打开我的床头灯", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的床头灯", false);
            switchDoOpeanExclusiveResult2(resulthandle, "打开我的浴室灯", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的浴室灯", false);
            switchDoOpeanExclusiveResult2(resulthandle, "打开我的风扇", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的风扇", false);
            switchDoOpeanExclusiveResult2(resulthandle, "打开我的排气扇", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的排气扇", false);
            switchDoOpeanExclusiveResult2(resulthandle, "打开我的书桌灯", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的书桌灯", false);
            switchDoOpeanExclusiveResult2(resulthandle, "打开我的壁灯", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的壁灯", false);
            switchDoOpeanExclusiveResult2(resulthandle, "打开我的吊灯", true);
            switchDoOpeanExclusiveResult2(resulthandle, "关闭我的吊灯", false);

        }

        /**
         *
         * @param name
         * @return
         */
        private String tvCommand(String name, String flags, String data_before, String data_after) {
            String command = "";
            String code = getCode(name);
            if (!TextUtils.isEmpty(code)) {
                int len = data_after.length();
                if (len < 4) {
                    int cha = 4 - len;
                    for (int i = 0; i < cha; i++) {
                        data_after = "0" + data_after;
                    }
                }
//              保证data_after不少于4位
                command = BizUtil.tvCommand(code, flags, data_before, data_after);
                Log.d("VoiceUtil", name + "设备码" + command);
            } else {
                Log.d("VoiceUtil", name + "没有找到设备码");
            }
            return command;
        }

        /**
         * 控制开关
         * @param name
         * @param onoff
         * @return
         */
        private String onffCommand(String name, Boolean onoff) {
            String command = "";
            String code = getCode(name);
            if (!TextUtils.isEmpty(code)) {
                command = onoff ? BizUtil.OnCommand(code) : BizUtil.OffCommand(code);
            } else {
                Log.d("VoiceUtil", "没有找到设备码");
            }
            return command;
        }

        /**
         * 根据名字来获取具体设备号码 也就是SSID
         * @param name
         * @return
         */
        private String getCode(String name) {
            String code = "";
            for (Device d : MyApplication.getListDevice()) {
                if (name.equals(d.getName())) {
                    return d.getCode();
                }
            }
            return code;
        }

        private ResultString GetFirstHandle(String result) {

            //TODO: 效率高的命令词查询算法
            //TODO: sayresult 换成音效
            //TODO: 识别成功，发出类似微波炉热好东西的声音，但声音要短一些小一些
            //TODO: 识别失败，发出类似哒哒，很多应用都会发出的那种失败或者拒绝点击的声音。
            String[] str = result.split("\n");
            String tempStr = "";
            if (str.length > 0) {
                tempStr = str[0];
                tempStr = tempStr.replace("【结果】", "");
                int index = 0;
                if (tempStr.contains("【")) {
                    index = tempStr.indexOf("【");
                }
//                有一次遇到数组越界
                tempStr = tempStr.replace(tempStr.substring(index, tempStr.length()), "");
//                tempStr = tempStr.replace("【置信度】", "");
            }
            ResultString content = new ResultString();
//            String sayresult = "识别错误";
            String sayresult = "";
            Pattern p = Pattern.compile("【结果】(.*)【置信度】");
            Pattern pOpen = Pattern.compile("开");
            Pattern pClose = Pattern.compile("关");
            Pattern pTurn = Pattern.compile("到");
            Pattern pWashRoom01 = Pattern.compile("台灯"); //Todo add some other devices
            Pattern pWashRoom02 = Pattern.compile("吊灯"); //Todo add some other devices
            Pattern pWashRoom03 = Pattern.compile("射灯"); //Todo add some other devices
            Pattern pWashRoom04 = Pattern.compile("床头灯"); //Todo add some other devices
            Pattern pWashRoom05 = Pattern.compile("书桌灯"); //Todo add some other devices
            Pattern pWashRoom06 = Pattern.compile("客厅灯"); //Todo add some other devices
            Pattern pWashRoom07 = Pattern.compile("餐厅灯"); //Todo add some other devices
            Pattern pWashRoom08 = Pattern.compile("主卧灯"); //Todo add some other devices
            Pattern pWashRoom09 = Pattern.compile("次卧灯"); //Todo add some other devices
            Pattern pWashRoome = Pattern.compile("浴室灯"); //Todo add some other devices
            Pattern pWashRoomf = Pattern.compile("壁灯"); //Todo add some other devices
            Pattern pWashRoomg = Pattern.compile("厨房灯"); //Todo add some other devices
            Pattern pWashRoomh = Pattern.compile("廊灯"); //Todo add some other devices
            Pattern pWashRoomi = Pattern.compile("阳台灯"); //Todo add some other devices
            Pattern pWashRoomj = Pattern.compile("风扇"); //Todo add some other devices
            Pattern pWashRoomk = Pattern.compile("排气扇"); //Todo add some other devices

            Pattern pWashRoom41 = Pattern.compile("台灯一"); //Todo add some other devices
            Pattern pWashRoom42 = Pattern.compile("吊灯一"); //Todo add some other devices
            Pattern pWashRoom43 = Pattern.compile("射灯一"); //Todo add some other devices
            Pattern pWashRoom44 = Pattern.compile("床头灯一"); //Todo add some other devices
            Pattern pWashRoom45 = Pattern.compile("书桌灯一"); //Todo add some other devices
            Pattern pWashRoom46 = Pattern.compile("客厅灯一"); //Todo add some other devices
            Pattern pWashRoom47 = Pattern.compile("餐厅灯一"); //Todo add some other devices
            Pattern pWashRoom48 = Pattern.compile("主卧灯一"); //Todo add some other devices
            Pattern pWashRoom49 = Pattern.compile("次卧灯一"); //Todo add some other devices
            Pattern pWashRoom4e = Pattern.compile("浴室灯一"); //Todo add some other devices
            Pattern pWashRoom4f = Pattern.compile("壁灯一"); //Todo add some other devices
            Pattern pWashRoom4g = Pattern.compile("厨房灯一"); //Todo add some other devices
            Pattern pWashRoom4h = Pattern.compile("廊灯一"); //Todo add some other devices
            Pattern pWashRoom4i = Pattern.compile("阳台灯一"); //Todo add some other devices
            Pattern pWashRoom4j = Pattern.compile("风扇一"); //Todo add some other devices
            Pattern pWashRoom4k = Pattern.compile("排气扇一"); //Todo add some other devices

            Pattern pWashRoom11 = Pattern.compile("台灯二"); //Todo add some other devices
            Pattern pWashRoom12 = Pattern.compile("吊灯二"); //Todo add some other devices
            Pattern pWashRoom13 = Pattern.compile("射灯二"); //Todo add some other devices
            Pattern pWashRoom14 = Pattern.compile("床头灯二"); //Todo add some other devices
            Pattern pWashRoom15 = Pattern.compile("书桌灯二"); //Todo add some other devices
            Pattern pWashRoom16 = Pattern.compile("客厅灯二"); //Todo add some other devices
            Pattern pWashRoom17 = Pattern.compile("餐厅灯二"); //Todo add some other devices
            Pattern pWashRoom18 = Pattern.compile("主卧灯二"); //Todo add some other devices
            Pattern pWashRoom19 = Pattern.compile("次卧灯二"); //Todo add some other devices
            Pattern pWashRoom1e = Pattern.compile("浴室灯二"); //Todo add some other devices
            Pattern pWashRoom1f = Pattern.compile("壁灯二"); //Todo add some other devices
            Pattern pWashRoom1g = Pattern.compile("厨房灯二"); //Todo add some other devices
            Pattern pWashRoom1h = Pattern.compile("廊灯二"); //Todo add some other devices
            Pattern pWashRoom1i = Pattern.compile("阳台灯二"); //Todo add some other devices
            Pattern pWashRoom1j = Pattern.compile("风扇二"); //Todo add some other devices
            Pattern pWashRoom1k = Pattern.compile("排气扇二"); //Todo add some other devices

            Pattern pWashRoom21 = Pattern.compile("台灯三"); //Todo add some other devices
            Pattern pWashRoom22 = Pattern.compile("吊灯三"); //Todo add some other devices
            Pattern pWashRoom23 = Pattern.compile("射灯三"); //Todo add some other devices
            Pattern pWashRoom24 = Pattern.compile("床头灯三"); //Todo add some other devices
            Pattern pWashRoom25 = Pattern.compile("书桌灯三"); //Todo add some other devices
            Pattern pWashRoom26 = Pattern.compile("客厅灯三"); //Todo add some other devices
            Pattern pWashRoom27 = Pattern.compile("餐厅灯三"); //Todo add some other devices
            Pattern pWashRoom28 = Pattern.compile("主卧灯三"); //Todo add some other devices
            Pattern pWashRoom29 = Pattern.compile("次卧灯三"); //Todo add some other devices
            Pattern pWashRoom2e = Pattern.compile("浴室灯三"); //Todo add some other devices
            Pattern pWashRoom2f = Pattern.compile("壁灯三"); //Todo add some other devices
            Pattern pWashRoom2g = Pattern.compile("厨房灯三"); //Todo add some other devices
            Pattern pWashRoom2h = Pattern.compile("廊灯三"); //Todo add some other devices
            Pattern pWashRoom2i = Pattern.compile("阳台灯三"); //Todo add some other devices
            Pattern pWashRoom2j = Pattern.compile("风扇三"); //Todo add some other devices
            Pattern pWashRoom2k = Pattern.compile("排气扇三"); //Todo add some other devices

            Pattern pWashRoom31 = Pattern.compile("台灯四"); //Todo add some other devices
            Pattern pWashRoom32 = Pattern.compile("吊灯四"); //Todo add some other devices
            Pattern pWashRoom33 = Pattern.compile("射灯四"); //Todo add some other devices
            Pattern pWashRoom34 = Pattern.compile("床头灯四"); //Todo add some other devices
            Pattern pWashRoom35 = Pattern.compile("书桌灯四"); //Todo add some other devices
            Pattern pWashRoom36 = Pattern.compile("客厅灯四"); //Todo add some other devices
            Pattern pWashRoom37 = Pattern.compile("餐厅灯四"); //Todo add some other devices
            Pattern pWashRoom38 = Pattern.compile("主卧灯四"); //Todo add some other devices
            Pattern pWashRoom39 = Pattern.compile("次卧灯四"); //Todo add some other devices
            Pattern pWashRoom3e = Pattern.compile("浴室灯四"); //Todo add some other devices
            Pattern pWashRoom3f = Pattern.compile("壁灯四"); //Todo add some other devices
            Pattern pWashRoom3g = Pattern.compile("厨房灯四"); //Todo add some other devices
            Pattern pWashRoom3h = Pattern.compile("廊灯四"); //Todo add some other devices
            Pattern pWashRoom3i = Pattern.compile("阳台灯四"); //Todo add some other devices
            Pattern pWashRoom3j = Pattern.compile("风扇四"); //Todo add some other devices
            Pattern pWashRoom3k = Pattern.compile("排气扇四"); //Todo add some other devices


            Pattern pTV = Pattern.compile("电视"); //Todo add some other devices
            Pattern pch1 = Pattern.compile("中央一台"); //Todo add some other devices
            Pattern pch2 = Pattern.compile("央视一套"); //Todo add some other devices
            Pattern pch3 = Pattern.compile("中央二台");
            Pattern pch4 = Pattern.compile("央视二套");
            Pattern pch5 = Pattern.compile("中央三台");
            Pattern pch6 = Pattern.compile("央视三套");
            Pattern pch8 = Pattern.compile("中央四台");
            Pattern pch9 = Pattern.compile("央视四套");
            Pattern pch10 = Pattern.compile("中央五台");
            Pattern pch11 = Pattern.compile("央视五套");
            Pattern pch12 = Pattern.compile("中央六台");
            Pattern pch13 = Pattern.compile("央视六套");
            Pattern pch14 = Pattern.compile("中央七台");
            Pattern pch15 = Pattern.compile("央视七套");
            Pattern pch16 = Pattern.compile("中央八台");
            Pattern pch17 = Pattern.compile("央视八套");
            Pattern pch18 = Pattern.compile("中央九台");
            Pattern pch19 = Pattern.compile("央视九套");
            Pattern pch20 = Pattern.compile("中央十台");
            Pattern pch21 = Pattern.compile("央视十套");
            Pattern pch22 = Pattern.compile("中央十二台");
            Pattern pch23 = Pattern.compile("央视十二套");
            Pattern pch24 = Pattern.compile("体育赛事频道");
//            Pattern pch25= Pattern.compile("CCTV5+");
            Pattern pch26 = Pattern.compile("北京卫视台");
            Pattern pch27 = Pattern.compile("天津卫视台");
            Pattern pch28 = Pattern.compile("东方卫视台");
            Pattern pch29 = Pattern.compile("湖南卫视台");
            Pattern pch30 = Pattern.compile("浙江卫视台");
            Pattern pch31 = Pattern.compile("江苏卫视台");
            Pattern pch32 = Pattern.compile("山东卫视台");
            Pattern pch33 = Pattern.compile("广东卫视台");
            Pattern pch34 = Pattern.compile("深圳卫视台");
            Pattern pch35 = Pattern.compile("湖北卫视台");
            Pattern pch36 = Pattern.compile("河北卫视台");
            Pattern pch37 = Pattern.compile("福建东南卫视台");
            Pattern pch38 = Pattern.compile("福建海峡卫视台");
            Pattern pch39 = Pattern.compile("黑龙江卫视台");
            Pattern pch40 = Pattern.compile("辽宁卫视台");
            Pattern pch41 = Pattern.compile("江西卫视台");
            Pattern pch42 = Pattern.compile("陕西卫视台");
            Pattern pch43 = Pattern.compile("重庆卫视台");
            Pattern pch44 = Pattern.compile("中国交通");
            Pattern pch45 = Pattern.compile("中央十一台");
            Pattern pch46 = Pattern.compile("央视十一套");
            Pattern pch47 = Pattern.compile("央视新闻");
            Pattern pch48 = Pattern.compile("央视音乐");
            Pattern pch49 = Pattern.compile("国防军事");
            Pattern pch50 = Pattern.compile("女性时尚");
            Pattern pch51 = Pattern.compile("央视精品");
            Pattern pch52 = Pattern.compile("央视台球");
            Pattern pch53 = Pattern.compile("电视指南");
            Pattern pch54 = Pattern.compile("第一剧场");
            Pattern pch55 = Pattern.compile("怀旧剧场");
            Pattern pch56 = Pattern.compile("高尔夫网球");
            Pattern pch57 = Pattern.compile("风云音乐");
            Pattern pch58 = Pattern.compile("世界地理");
            Pattern pch59 = Pattern.compile("风云剧场");
            Pattern pch60 = Pattern.compile("证券资讯");
            Pattern pch61 = Pattern.compile("发现之旅");
            Pattern pch62 = Pattern.compile("央视四套(欧)");
            Pattern pch63 = Pattern.compile("央视四套(美)");
//            Pattern pch64= Pattern.compile("CCTV Document");
//            Pattern pch65= Pattern.compile("CCTV News");
            Pattern pch66 = Pattern.compile("央视新科动漫");
            Pattern pch67 = Pattern.compile("央视法语频道");
            Pattern pch68 = Pattern.compile("央视俄语频道");
            Pattern pch69 = Pattern.compile("央视阿语频道");
            Pattern pch70 = Pattern.compile("广西卫视台");
            Pattern pch71 = Pattern.compile("内蒙古卫视台");
            Pattern pch72 = Pattern.compile("海南旅游卫视台");
            Pattern pch73 = Pattern.compile("云南卫视台");
            Pattern pch74 = Pattern.compile("贵州卫视台");
            Pattern pch75 = Pattern.compile("青海卫视台");
            Pattern pch76 = Pattern.compile("宁夏卫视台");
            Pattern pch77 = Pattern.compile("甘肃卫视台");
            Pattern pch78 = Pattern.compile("吉林卫视台");
            Pattern pch79 = Pattern.compile("西藏卫视台");
            Pattern pch80 = Pattern.compile("新疆卫视台");
            Pattern pch81 = Pattern.compile("新疆兵团卫视台");
            Pattern pch82 = Pattern.compile("福建厦门卫视台");
            Pattern pch83 = Pattern.compile("福建海峡卫视台");
            Pattern pch84 = Pattern.compile("广东南方卫视台");
            Pattern pch85 = Pattern.compile("陕西农林卫视台");
            Pattern pch86 = Pattern.compile("海南三沙卫视台");
            Pattern pch87 = Pattern.compile("健康卫视台");
            Pattern pch88 = Pattern.compile("北京文艺频道");
            Pattern pch89 = Pattern.compile("北京科教频道");
            Pattern pch90 = Pattern.compile("北京影视频道");
            Pattern pch91 = Pattern.compile("北京财经频道");
            Pattern pch92 = Pattern.compile("北京体育频道");
            Pattern pch93 = Pattern.compile("北京生活频道");
            Pattern pch94 = Pattern.compile("北京青年频道");
            Pattern pch95 = Pattern.compile("北京新闻频道");
            Pattern pch96 = Pattern.compile("北京纪实频道");
            Pattern pch97 = Pattern.compile("上海新闻综合");
            Pattern pch98 = Pattern.compile("上海电视剧");
            Pattern pch99 = Pattern.compile("东方电影频道");
            Pattern pch100 = Pattern.compile("上海五星体育");
            Pattern pch101 = Pattern.compile("上海艺术人文");
            Pattern pch102 = Pattern.compile("上海娱乐频道");
            Pattern pch103 = Pattern.compile("上海星尚频道");
            Pattern pch104 = Pattern.compile("上海外语频道");
            Pattern pch105 = Pattern.compile("上海教育频道");
            Pattern pch106 = Pattern.compile("广东国际频道");
            Pattern pch107 = Pattern.compile("广东新闻频道");
            Pattern pch108 = Pattern.compile("广东公共频道");
            Pattern pch109 = Pattern.compile("广东体育频道");
            Pattern pch110 = Pattern.compile("广东经济科教台");
            Pattern pch111 = Pattern.compile("广东综艺频道");
            Pattern pch112 = Pattern.compile("广东影视频道");
            Pattern pch113 = Pattern.compile("广东房产频道");
            Pattern pch114 = Pattern.compile("广东会展频道");
            Pattern pch115 = Pattern.compile("深圳都市频道");
            Pattern pch116 = Pattern.compile("深圳电视剧频道");
            Pattern pch117 = Pattern.compile("深圳体育健康台");
            Pattern pch118 = Pattern.compile("深圳财经生活台");
            Pattern pch119 = Pattern.compile("深圳娱乐频道");
            Pattern pch120 = Pattern.compile("深圳公共频道");
            Pattern pch130 = Pattern.compile("深圳少儿频道");
            Pattern pch131 = Pattern.compile("深圳法治频道");
            Pattern pch132 = Pattern.compile("广州综合频道");
            Pattern pch133 = Pattern.compile("广州新闻频道");
            Pattern pch134 = Pattern.compile("广州影视频道");
            Pattern pch135 = Pattern.compile("广州竞赛频道");
            Pattern pch136 = Pattern.compile("广州经济频道");
            Pattern pch137 = Pattern.compile("广州少儿频道");
            Pattern pch138 = Pattern.compile("广州生活频道");


            Pattern pSecne1 = Pattern.compile("回家"); //Todo add some other devices
            Pattern pSecne2 = Pattern.compile("睡觉"); //Todo add some other devices
            Pattern pSecne3 = Pattern.compile("离家"); //Todo add some other devices
            Pattern pSecne4 = Pattern.compile("我起来了"); //Todo add some other devices
            Pattern pSecne5 = Pattern.compile("起夜"); //Todo add some other devices
            Pattern pSecne6 = Pattern.compile("见客人"); //Todo add some other devices
            Pattern pSecne7 = Pattern.compile("看电影"); //Todo add some other devices
            Pattern pSecne8 = Pattern.compile("就餐"); //Todo add some other devices
            Pattern pSecne9 = Pattern.compile("回房"); //Todo add some other devices
            Pattern pSecne10 = Pattern.compile("离房"); //Todo add some other devices
            Pattern pSecne11 = Pattern.compile("全部灯"); //Todo add some other devices
            Pattern pSecne12 = Pattern.compile("所有灯"); //Todo add some other devices
            Pattern pSecne13 = Pattern.compile("去吃饭"); //Todo add some other devices


            Pattern pExEquipment1 = Pattern.compile("我的灯");

            Pattern pExEquipment02 = Pattern.compile("我的台灯");
            Pattern pExEquipment03 = Pattern.compile("我的床头灯");
            Pattern pExEquipment04 = Pattern.compile("我的浴室灯");
            Pattern pExEquipment05 = Pattern.compile("我的风扇");
            Pattern pExEquipment06 = Pattern.compile("我的排气扇");
            Pattern pExEquipment07 = Pattern.compile("我的书桌灯");
            Pattern pExEquipment08 = Pattern.compile("我的壁灯");
            Pattern pExEquipment09 = Pattern.compile("我的吊灯");
//            Pattern pExEquipment12 = Pattern.compile("我的台灯一");
//            Pattern pExEquipment13 = Pattern.compile("我的床头灯一");
//            Pattern pExEquipment14 = Pattern.compile("我的浴室灯一");
//            Pattern pExEquipment15 = Pattern.compile("我的风扇一");
//            Pattern pExEquipment16 = Pattern.compile("我的排气扇一");
//            Pattern pExEquipment17 = Pattern.compile("我的书桌灯一");
//            Pattern pExEquipment18 = Pattern.compile("我的壁灯一");
//            Pattern pExEquipment19 = Pattern.compile("我的吊灯一");
//            Pattern pExEquipment22 = Pattern.compile("我的台灯二");
//            Pattern pExEquipment23 = Pattern.compile("我的床头灯二");
//            Pattern pExEquipment24 = Pattern.compile("我的浴室灯二");
//            Pattern pExEquipment25 = Pattern.compile("我的风扇二");
//            Pattern pExEquipment26 = Pattern.compile("我的排气扇二");
//            Pattern pExEquipment27 = Pattern.compile("我的书桌灯二");
//            Pattern pExEquipment28 = Pattern.compile("我的壁灯二");
//            Pattern pExEquipment29 = Pattern.compile("我的吊灯二");
//            Pattern pExEquipment32 = Pattern.compile("我的台灯三");
//            Pattern pExEquipment33 = Pattern.compile("我的床头灯三");
//            Pattern pExEquipment34 = Pattern.compile("我的浴室灯三");
//            Pattern pExEquipment35 = Pattern.compile("我的风扇三");
//            Pattern pExEquipment36 = Pattern.compile("我的排气扇三");
//            Pattern pExEquipment37 = Pattern.compile("我的书桌灯三");
//            Pattern pExEquipment38 = Pattern.compile("我的壁灯三");
//            Pattern pExEquipment39 = Pattern.compile("我的吊灯三");
//            Pattern pExEquipment42 = Pattern.compile("我的台灯四");
//            Pattern pExEquipment43 = Pattern.compile("我的床头灯四");
//            Pattern pExEquipment44 = Pattern.compile("我的浴室灯四");
//            Pattern pExEquipment45 = Pattern.compile("我的风扇四");
//            Pattern pExEquipment46 = Pattern.compile("我的排气扇四");
//            Pattern pExEquipment47 = Pattern.compile("我的书桌灯四");
//            Pattern pExEquipment48 = Pattern.compile("我的壁灯四");
//            Pattern pExEquipment49 = Pattern.compile("我的吊灯四");

            Matcher m = p.matcher(result);
            int syaInt = over;
            while (m.find()) {
//                sayresult = "好的，为您";
                Matcher mOpen = pOpen.matcher(m.group(0));
                Matcher mClose = pClose.matcher(m.group(0));
                Matcher mTurn = pTurn.matcher(m.group(0));

                Matcher mWashRoom01 = pWashRoom01.matcher(m.group(0));
                Matcher mWashRoom02 = pWashRoom02.matcher(m.group(0));
                Matcher mWashRoom03 = pWashRoom03.matcher(m.group(0));
                Matcher mWashRoom04 = pWashRoom04.matcher(m.group(0));
                Matcher mWashRoom05 = pWashRoom05.matcher(m.group(0));
                Matcher mWashRoom06 = pWashRoom06.matcher(m.group(0));
                Matcher mWashRoom07 = pWashRoom07.matcher(m.group(0));
                Matcher mWashRoom08 = pWashRoom08.matcher(m.group(0));
                Matcher mWashRoom09 = pWashRoom09.matcher(m.group(0));
                Matcher mWashRoome = pWashRoome.matcher(m.group(0));
                Matcher mWashRoomf = pWashRoomf.matcher(m.group(0));
                Matcher mWashRoomg = pWashRoomg.matcher(m.group(0));
                Matcher mWashRoomh = pWashRoomh.matcher(m.group(0));
                Matcher mWashRoomi = pWashRoomi.matcher(m.group(0));
                Matcher mWashRoomj = pWashRoomj.matcher(m.group(0));
                Matcher mWashRoomk = pWashRoomk.matcher(m.group(0));

                Matcher mWashRoom41 = pWashRoom41.matcher(m.group(0));
                Matcher mWashRoom42 = pWashRoom42.matcher(m.group(0));
                Matcher mWashRoom43 = pWashRoom43.matcher(m.group(0));
                Matcher mWashRoom44 = pWashRoom44.matcher(m.group(0));
                Matcher mWashRoom45 = pWashRoom45.matcher(m.group(0));
                Matcher mWashRoom46 = pWashRoom46.matcher(m.group(0));
                Matcher mWashRoom47 = pWashRoom47.matcher(m.group(0));
                Matcher mWashRoom48 = pWashRoom48.matcher(m.group(0));
                Matcher mWashRoom49 = pWashRoom49.matcher(m.group(0));
                Matcher mWashRoom4e = pWashRoom4e.matcher(m.group(0));
                Matcher mWashRoom4f = pWashRoom4f.matcher(m.group(0));
                Matcher mWashRoom4g = pWashRoom4g.matcher(m.group(0));
                Matcher mWashRoom4h = pWashRoom4h.matcher(m.group(0));
                Matcher mWashRoom4i = pWashRoom4i.matcher(m.group(0));
                Matcher mWashRoom4j = pWashRoom4j.matcher(m.group(0));
                Matcher mWashRoom4k = pWashRoom4k.matcher(m.group(0));

                Matcher mWashRoom11 = pWashRoom11.matcher(m.group(0));
                Matcher mWashRoom12 = pWashRoom12.matcher(m.group(0));
                Matcher mWashRoom13 = pWashRoom13.matcher(m.group(0));
                Matcher mWashRoom14 = pWashRoom14.matcher(m.group(0));
                Matcher mWashRoom15 = pWashRoom15.matcher(m.group(0));
                Matcher mWashRoom16 = pWashRoom16.matcher(m.group(0));
                Matcher mWashRoom17 = pWashRoom17.matcher(m.group(0));
                Matcher mWashRoom18 = pWashRoom18.matcher(m.group(0));
                Matcher mWashRoom19 = pWashRoom19.matcher(m.group(0));
                Matcher mWashRoom1e = pWashRoom1e.matcher(m.group(0));
                Matcher mWashRoom1f = pWashRoom1f.matcher(m.group(0));
                Matcher mWashRoom1g = pWashRoom1g.matcher(m.group(0));
                Matcher mWashRoom1h = pWashRoom1h.matcher(m.group(0));
                Matcher mWashRoom1i = pWashRoom1i.matcher(m.group(0));
                Matcher mWashRoom1j = pWashRoom1j.matcher(m.group(0));
                Matcher mWashRoom1k = pWashRoom1k.matcher(m.group(0));

                Matcher mWashRoom21 = pWashRoom21.matcher(m.group(0));
                Matcher mWashRoom22 = pWashRoom22.matcher(m.group(0));
                Matcher mWashRoom23 = pWashRoom23.matcher(m.group(0));
                Matcher mWashRoom24 = pWashRoom24.matcher(m.group(0));
                Matcher mWashRoom25 = pWashRoom25.matcher(m.group(0));
                Matcher mWashRoom26 = pWashRoom26.matcher(m.group(0));
                Matcher mWashRoom27 = pWashRoom27.matcher(m.group(0));
                Matcher mWashRoom28 = pWashRoom28.matcher(m.group(0));
                Matcher mWashRoom29 = pWashRoom29.matcher(m.group(0));
                Matcher mWashRoom2e = pWashRoom2e.matcher(m.group(0));
                Matcher mWashRoom2f = pWashRoom2f.matcher(m.group(0));
                Matcher mWashRoom2g = pWashRoom2g.matcher(m.group(0));
                Matcher mWashRoom2h = pWashRoom2h.matcher(m.group(0));
                Matcher mWashRoom2i = pWashRoom2i.matcher(m.group(0));
                Matcher mWashRoom2j = pWashRoom2j.matcher(m.group(0));
                Matcher mWashRoom2k = pWashRoom2k.matcher(m.group(0));

                Matcher mWashRoom31 = pWashRoom31.matcher(m.group(0));
                Matcher mWashRoom32 = pWashRoom32.matcher(m.group(0));
                Matcher mWashRoom33 = pWashRoom33.matcher(m.group(0));
                Matcher mWashRoom34 = pWashRoom34.matcher(m.group(0));
                Matcher mWashRoom35 = pWashRoom35.matcher(m.group(0));
                Matcher mWashRoom36 = pWashRoom36.matcher(m.group(0));
                Matcher mWashRoom37 = pWashRoom37.matcher(m.group(0));
                Matcher mWashRoom38 = pWashRoom38.matcher(m.group(0));
                Matcher mWashRoom39 = pWashRoom39.matcher(m.group(0));
                Matcher mWashRoom3e = pWashRoom3e.matcher(m.group(0));
                Matcher mWashRoom3f = pWashRoom3f.matcher(m.group(0));
                Matcher mWashRoom3g = pWashRoom3g.matcher(m.group(0));
                Matcher mWashRoom3h = pWashRoom3h.matcher(m.group(0));
                Matcher mWashRoom3i = pWashRoom3i.matcher(m.group(0));
                Matcher mWashRoom3j = pWashRoom3j.matcher(m.group(0));
                Matcher mWashRoom3k = pWashRoom3k.matcher(m.group(0));

                Matcher mTV = pTV.matcher(m.group(0));
                Matcher mch1 = pch1.matcher(m.group(0));
                Matcher mch2 = pch2.matcher(m.group(0));
                Matcher mch3 = pch3.matcher(m.group(0));
                Matcher mch4 = pch4.matcher(m.group(0));
                Matcher mch5 = pch5.matcher(m.group(0));
                Matcher mch6 = pch6.matcher(m.group(0));
                Matcher mch8 = pch8.matcher(m.group(0));
                Matcher mch9 = pch9.matcher(m.group(0));
                Matcher mch10 = pch10.matcher(m.group(0));
                Matcher mch11 = pch11.matcher(m.group(0));
                Matcher mch12 = pch12.matcher(m.group(0));
                Matcher mch13 = pch13.matcher(m.group(0));
                Matcher mch14 = pch14.matcher(m.group(0));
                Matcher mch15 = pch15.matcher(m.group(0));
                Matcher mch16 = pch16.matcher(m.group(0));
                Matcher mch17 = pch17.matcher(m.group(0));
                Matcher mch18 = pch18.matcher(m.group(0));
                Matcher mch19 = pch19.matcher(m.group(0));
                Matcher mch20 = pch20.matcher(m.group(0));
                Matcher mch21 = pch21.matcher(m.group(0));
                Matcher mch22 = pch22.matcher(m.group(0));
                Matcher mch23 = pch23.matcher(m.group(0));
                Matcher mch24 = pch24.matcher(m.group(0));
//                Matcher mch25 = pch25.matcher(m.group(0));
                Matcher mch26 = pch26.matcher(m.group(0));
                Matcher mch27 = pch27.matcher(m.group(0));
                Matcher mch28 = pch28.matcher(m.group(0));
                Matcher mch29 = pch29.matcher(m.group(0));
                Matcher mch30 = pch30.matcher(m.group(0));
                Matcher mch31 = pch31.matcher(m.group(0));
                Matcher mch32 = pch32.matcher(m.group(0));
                Matcher mch33 = pch33.matcher(m.group(0));
                Matcher mch34 = pch34.matcher(m.group(0));
                Matcher mch35 = pch35.matcher(m.group(0));
                Matcher mch36 = pch36.matcher(m.group(0));
                Matcher mch37 = pch37.matcher(m.group(0));
                Matcher mch38 = pch38.matcher(m.group(0));
                Matcher mch39 = pch39.matcher(m.group(0));
                Matcher mch40 = pch40.matcher(m.group(0));
                Matcher mch41 = pch41.matcher(m.group(0));
                Matcher mch42 = pch42.matcher(m.group(0));
                Matcher mch43 = pch43.matcher(m.group(0));
                Matcher mch44 = pch44.matcher(m.group(0));
                Matcher mch45 = pch45.matcher(m.group(0));
                Matcher mch46 = pch46.matcher(m.group(0));
                Matcher mch47 = pch47.matcher(m.group(0));
                Matcher mch48 = pch48.matcher(m.group(0));
                Matcher mch49 = pch49.matcher(m.group(0));
                Matcher mch50 = pch50.matcher(m.group(0));
                Matcher mch51 = pch51.matcher(m.group(0));
                Matcher mch52 = pch52.matcher(m.group(0));
                Matcher mch53 = pch53.matcher(m.group(0));
                Matcher mch54 = pch54.matcher(m.group(0));
                Matcher mch55 = pch55.matcher(m.group(0));
                Matcher mch56 = pch56.matcher(m.group(0));
                Matcher mch57 = pch57.matcher(m.group(0));
                Matcher mch58 = pch58.matcher(m.group(0));
                Matcher mch59 = pch59.matcher(m.group(0));
                Matcher mch60 = pch60.matcher(m.group(0));
                Matcher mch61 = pch61.matcher(m.group(0));
                Matcher mch62 = pch62.matcher(m.group(0));
                Matcher mch63 = pch63.matcher(m.group(0));
//                Matcher mch64 = pch64.matcher(m.group(0));
//                Matcher mch65 = pch65.matcher(m.group(0));
                Matcher mch66 = pch66.matcher(m.group(0));
                Matcher mch67 = pch67.matcher(m.group(0));
                Matcher mch68 = pch68.matcher(m.group(0));
                Matcher mch69 = pch69.matcher(m.group(0));
                Matcher mch70 = pch70.matcher(m.group(0));
                Matcher mch71 = pch71.matcher(m.group(0));
                Matcher mch72 = pch72.matcher(m.group(0));
                Matcher mch73 = pch73.matcher(m.group(0));
                Matcher mch74 = pch74.matcher(m.group(0));
                Matcher mch75 = pch75.matcher(m.group(0));
                Matcher mch76 = pch76.matcher(m.group(0));
                Matcher mch77 = pch77.matcher(m.group(0));
                Matcher mch78 = pch78.matcher(m.group(0));
                Matcher mch79 = pch79.matcher(m.group(0));
                Matcher mch80 = pch80.matcher(m.group(0));
                Matcher mch81 = pch81.matcher(m.group(0));
                Matcher mch82 = pch82.matcher(m.group(0));
                Matcher mch83 = pch83.matcher(m.group(0));
                Matcher mch84 = pch84.matcher(m.group(0));
                Matcher mch85 = pch85.matcher(m.group(0));
                Matcher mch86 = pch86.matcher(m.group(0));
                Matcher mch87 = pch87.matcher(m.group(0));
                Matcher mch88 = pch88.matcher(m.group(0));
                Matcher mch89 = pch89.matcher(m.group(0));
                Matcher mch90 = pch90.matcher(m.group(0));
                Matcher mch91 = pch91.matcher(m.group(0));
                Matcher mch92 = pch92.matcher(m.group(0));
                Matcher mch93 = pch93.matcher(m.group(0));
                Matcher mch94 = pch94.matcher(m.group(0));
                Matcher mch95 = pch95.matcher(m.group(0));
                Matcher mch96 = pch96.matcher(m.group(0));
                Matcher mch97 = pch97.matcher(m.group(0));
                Matcher mch98 = pch98.matcher(m.group(0));
                Matcher mch99 = pch99.matcher(m.group(0));
                Matcher mch100 = pch100.matcher(m.group(0));
                Matcher mch101 = pch101.matcher(m.group(0));
                Matcher mch102 = pch102.matcher(m.group(0));
                Matcher mch103 = pch103.matcher(m.group(0));
                Matcher mch104 = pch104.matcher(m.group(0));
                Matcher mch105 = pch105.matcher(m.group(0));
                Matcher mch106 = pch106.matcher(m.group(0));
                Matcher mch107 = pch107.matcher(m.group(0));
                Matcher mch108 = pch108.matcher(m.group(0));
                Matcher mch109 = pch109.matcher(m.group(0));
                Matcher mch110 = pch110.matcher(m.group(0));
                Matcher mch111 = pch111.matcher(m.group(0));
                Matcher mch112 = pch112.matcher(m.group(0));
                Matcher mch113 = pch113.matcher(m.group(0));
                Matcher mch114 = pch114.matcher(m.group(0));
                Matcher mch115 = pch115.matcher(m.group(0));
                Matcher mch116 = pch116.matcher(m.group(0));
                Matcher mch117 = pch117.matcher(m.group(0));
                Matcher mch118 = pch118.matcher(m.group(0));
                Matcher mch119 = pch119.matcher(m.group(0));
                Matcher mch120 = pch120.matcher(m.group(0));
                Matcher mch130 = pch130.matcher(m.group(0));
                Matcher mch131 = pch131.matcher(m.group(0));
                Matcher mch132 = pch132.matcher(m.group(0));
                Matcher mch133 = pch133.matcher(m.group(0));
                Matcher mch134 = pch134.matcher(m.group(0));
                Matcher mch135 = pch135.matcher(m.group(0));
                Matcher mch136 = pch136.matcher(m.group(0));
                Matcher mch137 = pch137.matcher(m.group(0));
                Matcher mch138 = pch138.matcher(m.group(0));


                Matcher mSecne1 = pSecne1.matcher(m.group(0));
                Matcher mSecne2 = pSecne2.matcher(m.group(0));
                Matcher mSecne3 = pSecne3.matcher(m.group(0));
                Matcher mSecne4 = pSecne4.matcher(m.group(0));
                Matcher mSecne5 = pSecne5.matcher(m.group(0));
                Matcher mSecne6 = pSecne6.matcher(m.group(0));
                Matcher mSecne7 = pSecne7.matcher(m.group(0));
                Matcher mSecne8 = pSecne8.matcher(m.group(0));
                Matcher mSecne9 = pSecne9.matcher(m.group(0));
                Matcher mSecne10 = pSecne10.matcher(m.group(0));
                Matcher mSecne11 = pSecne11.matcher(m.group(0));
                Matcher mSecne12 = pSecne12.matcher(m.group(0));
                Matcher mSecne13 = pSecne13.matcher(m.group(0));


                Matcher mExEquipment1 = pExEquipment1.matcher(m.group(0));

                Matcher mExEquipment02 = pExEquipment02.matcher(m.group(0));
                Matcher mExEquipment03 = pExEquipment03.matcher(m.group(0));
                Matcher mExEquipment04 = pExEquipment04.matcher(m.group(0));
                Matcher mExEquipment05 = pExEquipment05.matcher(m.group(0));
                Matcher mExEquipment06 = pExEquipment06.matcher(m.group(0));
                Matcher mExEquipment07 = pExEquipment07.matcher(m.group(0));
                Matcher mExEquipment08 = pExEquipment08.matcher(m.group(0));
                Matcher mExEquipment09 = pExEquipment09.matcher(m.group(0));

            /*    Matcher mExEquipment12 = pExEquipment12.matcher(m.group(0));
                Matcher mExEquipment13 = pExEquipment13.matcher(m.group(0));
                Matcher mExEquipment14 = pExEquipment14.matcher(m.group(0));
                Matcher mExEquipment15 = pExEquipment15.matcher(m.group(0));
                Matcher mExEquipment16 = pExEquipment16.matcher(m.group(0));
                Matcher mExEquipment17 = pExEquipment17.matcher(m.group(0));
                Matcher mExEquipment18 = pExEquipment18.matcher(m.group(0));
                Matcher mExEquipment19 = pExEquipment19.matcher(m.group(0));

                Matcher mExEquipment22 = pExEquipment22.matcher(m.group(0));
                Matcher mExEquipment23 = pExEquipment23.matcher(m.group(0));
                Matcher mExEquipment24 = pExEquipment24.matcher(m.group(0));
                Matcher mExEquipment25 = pExEquipment25.matcher(m.group(0));
                Matcher mExEquipment26 = pExEquipment26.matcher(m.group(0));
                Matcher mExEquipment27 = pExEquipment27.matcher(m.group(0));
                Matcher mExEquipment28 = pExEquipment28.matcher(m.group(0));
                Matcher mExEquipment29 = pExEquipment29.matcher(m.group(0));

                Matcher mExEquipment32 = pExEquipment32.matcher(m.group(0));
                Matcher mExEquipment33 = pExEquipment33.matcher(m.group(0));
                Matcher mExEquipment34 = pExEquipment34.matcher(m.group(0));
                Matcher mExEquipment35 = pExEquipment35.matcher(m.group(0));
                Matcher mExEquipment36 = pExEquipment36.matcher(m.group(0));
                Matcher mExEquipment37 = pExEquipment37.matcher(m.group(0));
                Matcher mExEquipment38 = pExEquipment38.matcher(m.group(0));
                Matcher mExEquipment39 = pExEquipment39.matcher(m.group(0));*/

                if (mOpen.find()) { //open?
                    content.onoff = true;
//                    sayresult += "打开";
                    sayresult = "好的，为您打开";
                }
                if (mClose.find()) {//close?.
                    content.onoff = false;
//                    sayresult += "关闭";
                    sayresult = "好的，为您关闭";
                }
                if (mWashRoom01.find() && !result.contains("我的")) {
//                    sayresult += "台灯";
               /*     if (isGetDevice("台灯")) {
                        sayresult = "为您控制台灯";
                        content.device = mWashRoom01.group(0);
                    } else {
                        sayresult = hint("台灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "台灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom01.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }

                }
                if (mWashRoom02.find() && !result.contains("我的")) {
//                    sayresult += "吊灯";

                 /*   if (isGetDevice("吊灯")) {
                        sayresult = "为您控制吊灯";
                        content.device = mWashRoom02.group(0);
                    } else {
                        sayresult = hint("吊灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "吊灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom02.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoom03.find()) {
                    String nameTemp = "射灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制射灯";
                                content.device = mWashRoom03.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }

                }
                if (mWashRoom04.find() && !result.contains("我的")) {
//                    sayresult += "床头灯";

                  /*  if (isGetDevice("床头灯")) {
                        sayresult = "为您控制床头灯";
                        content.device = mWashRoom04.group(0);
                    } else {
                        sayresult = hint("床头灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "床头灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom04.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoom05.find() && !result.contains("我的")) {
//                    sayresult += "书桌灯";
//                    加了一个判断是因为防止和mExEquipment07  冲突  我的专属设备这块的语音冲突  所以加上&&!result.contains("我的")

                   /* if (isGetDevice("书桌灯")) {
                        sayresult = "为您控制书桌灯";
                        content.device = mWashRoom05.group(0);
                    } else {
                        sayresult = hint("书桌灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "书桌灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom05.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoom06.find()) {
//                    sayresult += "客厅灯";

                  /*  if (isGetDevice("客厅灯")) {
                        sayresult = "为您控制客厅灯";
                        content.device = mWashRoom06.group(0);
                    } else {
                        sayresult = hint("客厅灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "客厅灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom06.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoom07.find()) {
//                    sayresult += "餐厅灯";
                /*    if (isGetDevice("餐厅灯")) {
                        sayresult = "为您控制餐厅灯";
                        content.device = mWashRoom07.group(0);
                    } else {
                        sayresult = hint("餐厅灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "餐厅灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom07.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoom08.find()) {
//                    sayresult += "主卧灯";
                  /*  if (isGetDevice("主卧灯")) {
                        sayresult = "为您控制主卧灯";
                        content.device = mWashRoom08.group(0);
                    } else {
                        sayresult = hint("主卧灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "主卧灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom08.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoom09.find()) {
//                    sayresult += "次卧灯";

                  /*  if (isGetDevice("次卧灯")) {
                        sayresult = "为您控制次卧灯";
                        content.device = mWashRoom09.group(0);
                    } else {
                        sayresult = hint("次卧灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "次卧灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoom09.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoome.find()) {
//                    sayresult += "浴室灯";
                /*    if (isGetDevice("浴室灯")) {
                        sayresult = "为您控制浴室灯";
                        content.device = mWashRoome.group(0);
                    } else {
                        sayresult = hint("浴室灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "浴室灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoome.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoomf.find() && !result.contains("我的")) {
//                    sayresult += "壁灯";
                  /*  if (isGetDevice("壁灯")) {
                        sayresult = "为您控制壁灯";
                        content.device = mWashRoomf.group(0);
                    } else {
                        sayresult = hint("壁灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "壁灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoomf.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoomg.find()) {
//                    sayresult += "厨房灯";
                  /*  if (isGetDevice("厨房灯")) {
                        sayresult = "为您控制厨房灯";
                        content.device = mWashRoomg.group(0);
                    } else {
                        sayresult = hint("厨房灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "厨房灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoomg.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoomh.find()) {
//                    sayresult += "廊灯";
                   /* if (isGetDevice("廊灯")) {
                        sayresult = "为您控制廊灯";
                        content.device = mWashRoomh.group(0);
                    } else {
                        sayresult = hint("廊灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "廊灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoomh.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoomi.find()) {
//                    sayresult += "阳台灯";
                 /*   if (isGetDevice("阳台灯")) {
                        sayresult = "为您控制阳台灯";
                        content.device = mWashRoomi.group(0);
                    } else {
                        sayresult = hint("阳台灯");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "阳台灯";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoomi.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoomj.find() && !result.contains("我的")) {
//                    sayresult += "阳台灯";
                  /*  if (isGetDevice("风扇")) {
                        sayresult = "为您控制风扇";
                        content.device = mWashRoomj.group(0);
                    } else {
                        sayresult = hint("风扇");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "风扇";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoomj.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
                if (mWashRoomk.find() && !result.contains("我的")) {
//                    sayresult += "阳台灯";
                  /*  if (isGetDevice("排气扇")) {
                        sayresult = "为您控制排气扇";
                        content.device = mWashRoomk.group(0);
                    } else {
                        sayresult = hint("排气扇");
                        syaInt = R.raw.failure;
                    }*/
                    String nameTemp = "排气扇";
                    if (!TextUtils.isEmpty(tempStr)) {
                        if (nameTemp.equals(tempStr.substring(tempStr.length() - nameTemp.length(), tempStr.length()))) {
                            if (isGetDevice(nameTemp)) {
                                sayresult = "为您控制" + nameTemp;
                                content.device = mWashRoomk.group(0);
                                break;
                            } else {
                                sayresult = hint(nameTemp);
                                syaInt = R.raw.failure;
                                break;
                            }
                        }
                    }
                }
              /*  addSayresult(sayresult, content.device, mWashRoom01, "台灯");
                addSayresult(sayresult, content.device, mWashRoom02, "吊灯");
                addSayresult(sayresult, content.device, mWashRoom03, "射灯");
                addSayresult(sayresult, content.device, mWashRoom04, "床头灯");
                addSayresult(sayresult, content.device, mWashRoom05, "书桌灯");
                addSayresult(sayresult, content.device, mWashRoom06, "客厅灯");
                addSayresult(sayresult, content.device, mWashRoom07, "餐厅灯");
                addSayresult(sayresult, content.device, mWashRoom08, "主卧灯");
                addSayresult(sayresult, content.device, mWashRoom09, "次卧灯");
                addSayresult(sayresult, content.device, mWashRoome, "浴室灯");
                addSayresult(sayresult, content.device, mWashRoomf, "壁灯");
                addSayresult(sayresult, content.device, mWashRoomg, "厨房灯");
                addSayresult(sayresult, content.device, mWashRoomh, "廊灯");
                addSayresult(sayresult, content.device, mWashRoomi, "阳台灯");*/
                tempSayresult = "";
                tempDevice = "";
                tempSayInt = R.raw.over;
                if (addresult(tempStr, mWashRoom41, "台灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom42, "吊灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom43, "射灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom44, "床头灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom45, "书桌灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom46, "客厅灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom47, "餐厅灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom48, "主卧灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom49, "次卧灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom4e, "浴室灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom4f, "壁灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom4g, "厨房灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom4h, "廊灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom4i, "阳台灯一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom4j, "风扇一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom4k, "排气扇一")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom11, "台灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom12, "吊灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom13, "射灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom14, "床头灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom15, "书桌灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom16, "客厅灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom17, "餐厅灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom18, "主卧灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom19, "次卧灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom1e, "浴室灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom1f, "壁灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom1g, "厨房灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom1h, "廊灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom1i, "阳台灯二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom1j, "风扇二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom1k, "排气扇二")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom21, "台灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom22, "吊灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom23, "射灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom24, "床头灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom25, "书桌灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom26, "客厅灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom27, "餐厅灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom28, "主卧灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom29, "次卧灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom2e, "浴室灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom2f, "壁灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom2g, "厨房灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom2h, "廊灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom2i, "阳台灯三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom2j, "风扇三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom2k, "排气扇三")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }

                if (addresult(tempStr, mWashRoom31, "台灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom32, "吊灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom33, "射灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom34, "床头灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom35, "书桌灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom36, "客厅灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom37, "餐厅灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom38, "主卧灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom39, "次卧灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom3e, "浴室灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom3f, "壁灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom3g, "厨房灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom3h, "廊灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom3i, "阳台灯四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom3j, "风扇四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
                if (addresult(tempStr, mWashRoom3k, "排气扇四")) {
                    if (!TextUtils.isEmpty(tempSayresult)) {
                        sayresult = tempSayresult;
                    }
                    if (!TextUtils.isEmpty(tempDevice)) {
                        content.device = tempDevice;
                    }
                    if (tempSayInt == R.raw.failure) {
                        syaInt = tempSayInt;
                    }
                    break;
                }
          /*      if (!TextUtils.isEmpty(tempSayresult)) {
                    sayresult = tempSayresult;
                }
                if (!TextUtils.isEmpty(tempDevice)) {
                    content.device = tempDevice;
                }
                if (tempSayInt == R.raw.failure) {
                    syaInt = tempSayInt;
                }*/
                if (mTV.find()) {//Washroom?
//                    sayresult += "电视";
                    if (isGetDevice("电视")) {
                        sayresult = "为您控制电视";
                        content.device = mTV.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("电视")) break;
                }
                if (mch1.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央一台";
                        content.device = mch1.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央一台")) break;
                }

                if (mch2.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视一套";
                        content.device = mch2.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视一套")) break;
                }
                if (mch3.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央二台";
                        content.device = mch3.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央二台")) break;
                }
                if (mch4.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视二套";
                        content.device = mch4.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视二套")) break;
                }
                if (mch5.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央三台";
                        content.device = mch5.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央三台")) break;
                }
                if (mch6.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视三套";
                        content.device = mch6.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视三套")) break;
                }

                if (mch8.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央四台";
                        content.device = mch8.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央四台")) break;
                }
                if (mch9.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视四套";
                        content.device = mch9.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视四套")) break;
                }
                if (mch10.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央五台";
                        content.device = mch10.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央五台")) break;
                }
                if (mch11.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视五套";
                        content.device = mch11.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视五套")) break;
                }

                if (mch12.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央六台";
                        content.device = mch12.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央六台")) break;
                }
                if (mch13.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视六套";
                        content.device = mch13.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视六套")) break;
                }
                if (mch14.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央七台";
                        content.device = mch14.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央七台")) break;
                }
                if (mch15.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视七套";
                        content.device = mch15.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视七套")) break;
                }
                if (mch16.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央八台";
                        content.device = mch16.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央八台")) break;
                }
                if (mch17.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视八套";
                        content.device = mch17.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视八套")) break;
                }
                if (mch18.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央九台";
                        content.device = mch18.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央九台")) break;
                }
                if (mch19.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视九套";
                        content.device = mch19.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视九套")) break;
                }
                if (mch20.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央十台";
                        content.device = mch20.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央十台")) break;
                }

                if (mch21.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视十套";
                        content.device = mch21.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视十套")) break;
                }

                if (mch22.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央十二台";
                        content.device = mch22.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央十二台")) break;
                }
                if (mch23.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视十二套";
                        content.device = mch23.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视十二套")) break;
                }
                if (mch24.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到体育赛事频道";
                        content.device = mch24.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("体育赛事频道")) break;
                }
              /*  if (mch25.find()) {
                    sayresult = "好的，转到CCTV5+";
                    content.device = mch25.group(0);
                }*/
                if (mch26.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京卫视台";
                        content.device = mch26.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京卫视台")) break;
                }
                if (mch27.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到天津卫视台";
                        content.device = mch27.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("天津卫视台")) break;
                }
                if (mch28.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到东方卫视台";
                        content.device = mch28.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("东方卫视台")) break;
                }
                if (mch29.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到湖南卫视台";
                        content.device = mch29.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("湖南卫视台")) break;
                }
                if (mch30.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到浙江卫视台";
                        content.device = mch30.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("浙江卫视台")) break;
                }

                if (mch31.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到江苏卫视台";
                        content.device = mch31.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("江苏卫视台")) break;
                }

                if (mch32.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到山东卫视台";
                        content.device = mch32.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("山东卫视台")) break;
                }
                if (mch33.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东卫视台";
                        content.device = mch33.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东卫视台")) break;
                }
                if (mch34.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳卫视台";
                        content.device = mch34.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳卫视台")) break;
                }
                if (mch35.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到湖北卫视台";
                        content.device = mch35.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("湖北卫视台")) break;
                }
                if (mch36.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到河北卫视台";
                        content.device = mch36.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("河北卫视台")) break;
                }
                if (mch37.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到福建东南卫视台";
                        content.device = mch37.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("福建东南卫视台")) break;
                }
                if (mch38.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到福建海峡卫视台";
                        content.device = mch38.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("福建海峡卫视台")) break;
                }
                if (mch39.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到黑龙江卫视台";
                        content.device = mch39.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("黑龙江卫视台")) break;
                }
                if (mch40.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到辽宁卫视台";
                        content.device = mch40.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("辽宁卫视台")) break;
                }

                if (mch41.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到江西卫视台";
                        content.device = mch41.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("江西卫视台")) break;
                }

                if (mch42.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到陕西卫视台";
                        content.device = mch42.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("陕西卫视台")) break;
                }
                if (mch43.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到重庆卫视台";
                        content.device = mch43.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("重庆卫视台")) break;
                }
                if (mch44.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中国交通";
                        content.device = mch44.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中国交通")) break;
                }
                if (mch45.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到中央十一台";
                        content.device = mch45.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("中央十一台")) break;
                }
                if (mch46.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视十一套";
                        content.device = mch46.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视十一套")) break;
                }
                if (mch47.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视新闻";
                        content.device = mch47.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视新闻")) break;
                }
                if (mch48.find()) {


                    if (isGetDevice("电视")) {

                        sayresult = "好的，转到央视音乐";
                        content.device = mch48.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视音乐")) break;
                }
                if (mch49.find()) {

                    if (isGetDevice("电视")) {

                        sayresult = "好的，转到国防军事";
                        content.device = mch49.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("国防军事")) break;
                }
                if (mch50.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到女性时尚";
                        content.device = mch50.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("女性时尚")) break;
                }

                if (mch51.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视精品";
                        content.device = mch51.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视精品")) break;
                }

                if (mch52.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视台球";
                        content.device = mch52.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视台球")) break;
                }
                if (mch53.find()) {


                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到电视指南";
                        content.device = mch53.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("电视指南")) break;
                }
                if (mch54.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到第一剧场";
                        content.device = mch54.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("第一剧场")) break;
                }
                if (mch55.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到怀旧剧场";
                        content.device = mch55.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("怀旧剧场")) break;
                }
                if (mch56.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到高尔夫网球";
                        content.device = mch56.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("高尔夫网球")) break;
                }
                if (mch57.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到风云音乐";
                        content.device = mch57.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("风云音乐")) break;
                }
                if (mch58.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到世界地理";
                        content.device = mch58.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("世界地理")) break;
                }
                if (mch59.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到风云剧场";
                        content.device = mch59.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("风云剧场")) break;
                }
                if (mch60.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到证券资讯";
                        content.device = mch60.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("证券资讯")) break;
                }
                if (mch61.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到发现之旅";
                        content.device = mch61.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("发现之旅")) break;
                }

                if (mch62.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视四套(欧)";
                        content.device = mch62.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视四套(欧)")) break;
                }
                if (mch63.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视四套(美)";
                        content.device = mch63.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视四套(美)")) break;
                }
//                if (mch64.find()) {
//                    sayresult = "好的，转到CCTV Document";
//                    content.device = mch64.group(0);
//                }
               /* if (mch65.find()) {
                    sayresult = "好的，转到CCTV News";
                    content.device = mch65.group(0);
                }*/
                if (mch66.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视新科动漫";
                        content.device = mch66.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视新科动漫")) break;
                }
                if (mch67.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视法语频道";
                        content.device = mch67.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视法语频道")) break;
                }
                if (mch68.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视俄语频道";
                        content.device = mch68.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视俄语频道")) break;
                }
                if (mch69.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到央视阿语频道";
                        content.device = mch69.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("央视阿语频道")) break;
                }
                if (mch70.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广西卫视台";
                        content.device = mch70.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广西卫视台")) break;
                }
                if (mch71.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到内蒙古卫视台";
                        content.device = mch71.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("内蒙古卫视台")) break;
                }

                if (mch72.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到海南旅游卫视台";
                        content.device = mch72.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("海南旅游卫视台")) break;
                }
                if (mch73.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到云南卫视台";
                        content.device = mch73.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("云南卫视台")) break;
                }
                if (mch74.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到贵州卫视台";
                        content.device = mch74.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("贵州卫视台")) break;
                }
                if (mch75.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到青海卫视台";
                        content.device = mch75.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("青海卫视台")) break;
                }
                if (mch76.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到宁夏卫视台";
                        content.device = mch76.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("宁夏卫视台")) break;
                }
                if (mch77.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到甘肃卫视台";
                        content.device = mch77.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("甘肃卫视台")) break;
                }
                if (mch78.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到吉林卫视台";
                        content.device = mch78.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("吉林卫视台")) break;
                }
                if (mch79.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到西藏卫视台";
                        content.device = mch79.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("西藏卫视台")) break;
                }
                if (mch80.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到新疆卫视台";
                        content.device = mch80.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("新疆卫视台")) break;
                }

                if (mch81.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到新疆兵团卫视台";
                        content.device = mch81.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("新疆兵团卫视台")) break;
                }

                if (mch82.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到福建厦门卫视台";
                        content.device = mch82.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("福建厦门卫视台")) break;
                }
                if (mch83.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到福建海峡卫视台";
                        content.device = mch83.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("福建海峡卫视台")) break;
                }
                if (mch84.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东南方卫视台";
                        content.device = mch84.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东南方卫视台")) break;
                }
                if (mch85.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到陕西农林卫视台";
                        content.device = mch85.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("陕西农林卫视台")) break;
                }
                if (mch86.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到海南三沙卫视台";
                        content.device = mch86.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("海南三沙卫视台")) break;
                }
                if (mch87.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到健康卫视台";
                        content.device = mch87.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("健康卫视台")) break;
                }
                if (mch88.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京文艺频道";
                        content.device = mch88.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京文艺频道")) break;
                }
                if (mch89.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京科教频道";
                        content.device = mch89.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京科教频道")) break;
                }
                if (mch90.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京影视频道";
                        content.device = mch90.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京影视频道")) break;
                }

                if (mch91.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京财经频道";
                        content.device = mch91.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京财经频道")) break;
                }

                if (mch92.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京体育频道";
                        content.device = mch92.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京体育频道")) break;
                }
                if (mch93.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京生活频道";
                        content.device = mch93.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京生活频道")) break;
                }
                if (mch94.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京青年频道";
                        content.device = mch94.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京青年频道")) break;
                }
                if (mch95.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京新闻频道";
                        content.device = mch95.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京新闻频道")) break;
                }
                if (mch96.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到北京纪实频道";
                        content.device = mch96.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("北京纪实频道")) break;
                }
                if (mch97.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海新闻综合";
                        content.device = mch97.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海新闻综合")) break;
                }
                if (mch98.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海电视剧";
                        content.device = mch98.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海电视剧")) break;
                }
                if (mch99.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到东方电影频道";
                        content.device = mch99.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("东方电影频道")) break;
                }
                if (mch100.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海五星体育";
                        content.device = mch100.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海五星体育")) break;
                }

                if (mch101.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海艺术人文";
                        content.device = mch101.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海艺术人文")) break;
                }

                if (mch102.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海娱乐频道";
                        content.device = mch102.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海娱乐频道")) break;
                }
                if (mch103.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海星尚频道";
                        content.device = mch103.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海星尚频道")) break;
                }
                if (mch104.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海外语频道";
                        content.device = mch104.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海外语频道")) break;
                }
                if (mch105.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到上海教育频道";
                        content.device = mch105.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("上海教育频道")) break;
                }
                if (mch106.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东国际频道";
                        content.device = mch6.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东国际频道")) break;
                }
                if (mch107.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东新闻频道";
                        content.device = mch107.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东新闻频道")) break;
                }
                if (mch108.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东公共频道";
                        content.device = mch108.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东公共频道")) break;
                }
                if (mch109.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东体育频道";
                        content.device = mch109.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东体育频道")) break;
                }
                if (mch110.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东经济科教台";
                        content.device = mch110.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东经济科教台")) break;
                }

                if (mch111.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东综艺频道";
                        content.device = mch111.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东综艺频道")) break;
                }

                if (mch112.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东影视频道";
                        content.device = mch112.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东影视频道")) break;
                }
                if (mch113.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东房产频道";
                        content.device = mch113.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东房产频道")) break;
                }
                if (mch114.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广东会展频道";
                        content.device = mch114.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广东会展频道")) break;
                }
                if (mch115.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳都市频道";
                        content.device = mch115.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳都市频道")) break;
                }
                if (mch116.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳电视剧频道";
                        content.device = mch116.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳电视剧频道")) break;
                }
                if (mch117.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳体育健康台";
                        content.device = mch117.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳体育健康台")) break;
                }
                if (mch118.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳财经生活台";
                        content.device = mch118.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳财经生活台")) break;
                }
                if (mch119.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳娱乐频道";
                        content.device = mch119.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳娱乐频道")) break;
                }
                if (mch120.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳公共频道";
                        content.device = mch120.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳公共频道")) break;
                }
                if (mch130.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳少儿频道";
                        content.device = mch130.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("少儿频道")) break;
                }
                if (mch131.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到深圳法治频道";
                        content.device = mch131.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("深圳法治频道")) break;
                }

                if (mch132.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广州综合频道";
                        content.device = mch132.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广州综合频道")) break;
                }
                if (mch133.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广州新闻频道";
                        content.device = mch133.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广州新闻频道")) break;
                }
                if (mch134.find()) {

                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广州影视频道";
                        content.device = mch134.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广州影视频道")) break;
                }
                if (mch135.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广州竞赛频道";
                        content.device = mch135.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广州竞赛频道")) break;
                }
                if (mch136.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广州经济频道";
                        content.device = mch136.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广州经济频道")) break;
                }
                if (mch137.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广州少儿频道";
                        content.device = mch137.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广州少儿频道")) break;
                }
                if (mch138.find()) {
                    if (isGetDevice("电视")) {
                        sayresult = "好的，转到广州生活频道";
                        content.device = mch138.group(0);
                    } else {
                        sayresult = hint("电视");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("广州生活频道")) break;
                }


                if (mSecne1.find()) {
                    sayresult = "好的，为您启动回家场景";
                    content.device = mSecne1.group(0);
                }
                if (mSecne2.find()) {
                    sayresult = "好的，为您启动睡觉场景";
                    content.device = mSecne2.group(0);
                }
                if (mSecne3.find()) {
                    sayresult = "好的，为您启动离家场景";
                    content.device = mSecne3.group(0);
                }
                if (mSecne4.find()) {
                    sayresult = "好的，为您启动起床场景";
                    content.device = mSecne4.group(0);
                }
                if (mSecne5.find()) {
                    sayresult = "好的，为您启动起夜场景";
                    content.device = mSecne5.group(0);
                }
                if (mSecne6.find()) {
                    sayresult = "好的，为您启动会客场景";
                    content.device = mSecne6.group(0);
                }
                if (mSecne7.find()) {
                    sayresult = "好的，为您启动影院场景";
                    content.device = mSecne7.group(0);
                }
                if (mSecne8.find()) {
                    sayresult = "好的，为您启动就餐场景";
                    content.device = mSecne8.group(0);
                }
                if (mSecne9.find()) {
                    sayresult = "好的，为您启动回房场景";
                    content.device = mSecne9.group(0);
                }
                if (mSecne10.find()) {
                    sayresult = "好的，为您启动离房场景";
                    content.device = mSecne10.group(0);
                }
                if (mSecne11.find()) {
                    if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                        content.device = "关闭" + mSecne11.group(0);
                        sayresult = "好的，为您启动全关场景";
                    } else {
                        content.device = "打开" + mSecne11.group(0);
                        sayresult = "好的，为您启动全开场景";
                    }
                }
                if (mSecne12.find()) {
                    if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                        content.device = "关闭" + mSecne12.group(0);
                        sayresult = "好的，为您启动全关场景";
                    } else {
                        content.device = "打开" + mSecne12.group(0);
                        sayresult = "好的，为您启动全开场景";
                    }
                }
                if (mSecne13.find()) {
                    sayresult = "好的，为您启动就餐场景";
                    content.device = mSecne13.group(0);
                }


                if (mSecne13.find()) {
                    sayresult = "好的，为您启动就餐场景";
                    content.device = mSecne13.group(0);
                }

// 优化 其实可以把sayresult 换成result去做对比可能更好些
                if (mExEquipment1.find()) {
                    if (isSetexclusiveCommand()) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "专属设备";
                            content.device = "关闭" + mExEquipment1.group(0);
                        } else {
                            sayresult = "为您打开专属设备";
                            content.device = "打开" + mExEquipment1.group(0);
                        }

                    } else {
                        sayresult = hint1();
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的灯")) break;
                }
                if (mExEquipment02.find()) {
                    if (isSetexclusiveCommand2("台灯")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的台灯";
                            content.device = "关闭" + mExEquipment02.group(0);
                        } else {
                            sayresult = sayresult + "您的台灯";
                            content.device = "打开" + mExEquipment02.group(0);
                        }

                    } else {
                        sayresult = hint2("台灯");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的台灯")) break;
                }
                if (mExEquipment03.find()) {

                    if (isSetexclusiveCommand2("床头灯")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的床头灯";
                            content.device = "关闭" + mExEquipment03.group(0);
                        } else {
                            sayresult = sayresult + "您的床头灯";
                            content.device = "打开" + mExEquipment03.group(0);
                        }
                    } else {
                        sayresult = hint2("床头灯");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的床头灯")) break;
                }
                if (mExEquipment04.find()) {
                    if (isSetexclusiveCommand2("浴室灯")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的浴室灯";
                            content.device = "关闭" + mExEquipment04.group(0);
                        } else {
                            sayresult = sayresult + "您的浴室灯";
                            content.device = "打开" + mExEquipment04.group(0);
                        }
                    } else {
                        sayresult = hint2("浴室灯");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的浴室灯")) break;
                }
                if (mExEquipment05.find()) {

                    if (isSetexclusiveCommand2("风扇")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的风扇";
                            content.device = "关闭" + mExEquipment05.group(0);
                        } else {
                            sayresult = sayresult + "您的风扇";
                            content.device = "打开" + mExEquipment05.group(0);
                        }
                    } else {
                        sayresult = hint2("风扇");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的风扇")) break;
                }
                if (mExEquipment06.find()) {

                    if (isSetexclusiveCommand2("排气扇")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的排气扇";
                            content.device = "关闭" + mExEquipment06.group(0);
                        } else {
                            sayresult = sayresult + "您的排气扇";
                            content.device = "打开" + mExEquipment06.group(0);
                        }
                    } else {
                        sayresult = hint2("排气扇");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的排气扇")) break;
                }
                if (mExEquipment07.find()) {

                    if (isSetexclusiveCommand2("书桌灯")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的书桌灯";
                            content.device = "关闭" + mExEquipment07.group(0);
                        } else {
                            sayresult = sayresult + "您的书桌灯";
                            content.device = "打开" + mExEquipment07.group(0);
                        }
                    } else {
                        sayresult = hint2("书桌灯");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的书桌灯")) break;
                }
                if (mExEquipment08.find()) {
                    if (isSetexclusiveCommand2("壁灯")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的壁灯";
                            content.device = "关闭" + mExEquipment08.group(0);
                        } else {
                            sayresult = sayresult + "您的壁灯";
                            content.device = "打开" + mExEquipment08.group(0);
                        }
                    } else {
                        sayresult = hint2("壁灯");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的壁灯")) break;
                }
                if (mExEquipment09.find()) {
                    if (isSetexclusiveCommand2("吊灯")) {
                        if (!TextUtils.isEmpty(sayresult) && sayresult.contains("关闭")) {
                            sayresult = sayresult + "您的吊灯";
                            content.device = "关闭" + mExEquipment09.group(0);
                        } else {
                            sayresult = sayresult + "您的吊灯";
                            content.device = "打开" + mExEquipment09.group(0);
                        }
                    } else {
                        sayresult = hint2("吊灯");
                        syaInt = R.raw.failure;
                    }
                    if (tempStr.contains("我的吊灯")) break;

                }

                //	((EditText)findViewById(R.id.isr_text)).setText(m.group(0));

//                // 反馈结果
//                startSynthesize(sayresult);
//                palySound(syaInt);
            }
            /**
             * 如果没有找到命令那么就直接是
             */
            if (TextUtils.isEmpty(sayresult)) {
                sayresult = "识别错误";
                startSynthesize(sayresult);
                palySound(R.raw.failure);
            } else {
                // 反馈结果
                startSynthesize(sayresult);
                palySound(syaInt);
            }

            return content;
        }
    };

    private void addSayresult(String sayresult, String device, Matcher ma, String name, int syaInt) {
        if (ma.find()) {
            if (isGetDevice(name)) {
                tempSayresult = "为您控制" + name;
                tempDevice = ma.group(0);
            } else {
                tempSayresult = hint(name);
                tempSayInt = R.raw.failure;
            }
        }
    }

    private Boolean addresult(String tempStr, Matcher ma, String name) {
        if (ma.find()) {
            if (!TextUtils.isEmpty(name)) {
                if (name.equals(tempStr.substring(tempStr.length() - name.length(), tempStr.length()))) {
                    if (isGetDevice(name)) {
                        tempSayresult = "为您控制" + name;
                        tempDevice = ma.group(0);

                    } else {
                        tempSayresult = hint(name);
                        tempSayInt = R.raw.failure;

                    }
                    return true;
                }
            }
        }
        return false;
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            //TODO: “你好”换成音效，类似facebook APP的按键点击声音，稍微清脆点的嗒嗒声
            data = FucUtil.readAudioFile(context, "nihao.pcm");
            mVoiceWakeuper.stopListening();
            mThread = new Thread(new MyThread(data, mHandler));
            mThread.start();
            Log.d(TAG, result.getResultString() + ">>result.getResultString()");
            showAnimation();
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
            palySound(R.raw.failure);
            cancelAnimation();
        }

        @Override
        public void onBeginOfSpeech() {
//            showTip("开始说话");
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
        }

        @Override
        public void onVolumeChanged(int i) {

        }
    };

    private void startSynthesize(String text) {
        if (!PreferencesUtil.getVoicedback()) {
            return;
        }
        // 根据合成引擎设置相应参数
        // 清空参数
        mSpeechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        //设置云端合成引擎
        mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人:xiaoyan
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置合成语速:50
        mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调:50
        mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量:50
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        // 开始合成流程
        mSpeechSynthesizer.startSpeaking(text, mTtsListener);
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
//            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
//            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
//            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
//                showTip("播放完成");
                cancelAnimation();
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
                palySound(R.raw.failure);
                cancelAnimation();
            }
            // 合成结束，启动唤醒
            mVoiceWakeuper.startListening(mWakeuperListener);
            startWakeup();
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private void startWakeup() {
        // 设置唤醒参数
        // 清空参数
        mVoiceWakeuper.setParameter(SpeechConstant.PARAMS, null);
        // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
        mVoiceWakeuper.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
        // 设置唤醒模式
        mVoiceWakeuper.setParameter(SpeechConstant.IVW_SST, "wakeup");
        // 设置持续不进行唤醒，即一次唤醒成功后，结束唤醒业务
        mVoiceWakeuper.setParameter(SpeechConstant.KEEP_ALIVE, "1");
        // 开始唤醒流程
        mVoiceWakeuper.startListening(mWakeuperListener);
    }

    private void startUnderstand() {
        mSpeechUnderstander.setParameter(SpeechConstant.PARAMS, null);
        // 设置语言
        mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号，默认：1（有标点）
        mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, "0");

        mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
    }


    /**
     * 按住话筒
     */
    public void holdVoiceButton() {
        startRecognize();
    }

    /**
     * 按住话筒
     */
    public void holdVoiceButton(VoiceFinishListener listener) {
        this.voiceFinishListener = listener;
        holdVoiceButton();
    }

    /**
     * 比如说点击中止了 那么重新开始唤醒
     */
    public void openVoiceWakeuper() {
        mVoiceWakeuper = VoiceWakeuper.getWakeuper();
        if (mVoiceWakeuper != null) {
            ivw_result = "";
            startWakeup();
        } else {
            showTip("唤醒未初始化");
        }
    }

    /**
     * 终止唤醒
     */
    public void stopVoiceWakeuper() {
        mVoiceWakeuper = VoiceWakeuper.getWakeuper();
        if (mVoiceWakeuper != null) {
            mVoiceWakeuper.stopListening();
        } else {
            showTip("唤醒未初始化");
        }
    }

    /**
     * 本地构建语法监听器。
     */
    private GrammarListener mLocalGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
//                showTip("语法构建成功：" + grammarId);
            } else {
                showTip("语法构建失败,错误码：" + error.getErrorCode());
                palySound(R.raw.failure);
            }
        }
    };
    /**
     * 云端构建语法监听器。
     */
    private GrammarListener mCloudGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                String grammarID = new String(grammarId);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (!TextUtils.isEmpty(grammarId))
                    editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID);
                editor.commit();
            } else {
                showTip("语法构建失败,错误码：" + error.getErrorCode());
                palySound(R.raw.failure);
            }
        }
    };
    /**
     * 语义理解回调。
     */
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                Log.d(TAG, result.getResultString());
                // 显示
                String text = result.getResultString();
                if (!TextUtils.isEmpty(text)) {
                    Log.i("Test", text);
                    showTip(JsonParser.parseUnderstandResult(text));
                    startSynthesize(JsonParser.parseUnderstandResult(text));
                }
            } else {
                mVoiceWakeuper.startListening(mWakeuperListener);
                showTip("识别结果不正确。");
                palySound(R.raw.failure);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] bytes) {
//            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, data.length + "");
        }

        @Override
        public void onEndOfSpeech() {
        }


        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onError(SpeechError error) {
            mVoiceWakeuper.startListening(mWakeuperListener);
            //showTip(error.getPlainDescription(true));
            if (error.getErrorCode() == 10118) {
                startSynthesize("你说什么，我没有听清。");
            }
            palySound(R.raw.failure);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };

    private void showTip(final String str) {
        ToastUtil.showToast(context, str);
    }

    public void onDestroy() {
        mVoiceWakeuper = VoiceWakeuper.getWakeuper();
        if (mVoiceWakeuper != null) {
            mVoiceWakeuper.destroy();
        }
        mMediaPlayer = null;
    }

    public void setWakeUpAnimation(VoiceFinishListener voiceFinishListener, Activity activity) {
        setVoiceFinishListener(voiceFinishListener);
        this.activity = activity;
    }

    private void showAnimation() {
        if (activity != null) {
            if (activity instanceof MainActivity) {
                MainActivity a = (MainActivity) activity;
                a.showAnimation();
            } else if (activity instanceof DeskLampActivity) {
                DeskLampActivity a = (DeskLampActivity) activity;
                a.showAnimation();
            } else if (activity instanceof DeviceActivity) {
                DeviceActivity a = (DeviceActivity) activity;
                a.showAnimation();
            } else if (activity instanceof TelevisionActivity) {
                TelevisionActivity a = (TelevisionActivity) activity;
                a.showAnimation();
            }
        }
    }

    private void cancelAnimation() {
        if (activity != null) {
            if (activity instanceof MainActivity) {
                MainActivity a = (MainActivity) activity;
                a.cancalWaveAnimation();
            } else if (activity instanceof DeskLampActivity) {
                DeskLampActivity a = (DeskLampActivity) activity;
                a.cancalWaveAnimation();
            } else if (activity instanceof DeviceActivity) {
                DeviceActivity a = (DeviceActivity) activity;
                a.cancalWaveAnimation();
            } else if (activity instanceof TelevisionActivity) {
                TelevisionActivity a = (TelevisionActivity) activity;
                a.cancalWaveAnimation();
            }
        }
    }

    /**
     * 播放语音  开启了语音反馈才播放音效提示
     *
     * @param id
     */
    private void palySound(int id) {
        if (!PreferencesUtil.getVoicedback()) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            AssetFileDescriptor file = context.getResources().openRawResourceFd(
                    id);
            try {
                mMediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                mMediaPlayer.prepare();
                file.close();
                mMediaPlayer.setLooping(false);
                mMediaPlayer.start();
            } catch (IOException e) {
                mMediaPlayer = null;
            }
        }
    }

    /**
     * 语音的时候判断是不是有该设备
     *
     * @return
     */
    private Boolean isGetDevice(String name) {
        Boolean b = false;
        List<Device> list = MyApplication.getListDevice();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(name)) {
                return true;
            }
        }
        return b;
    }

    private String hint(String name) {
        String str = "请确定是否添加了" + name + "设备";
        return str;
    }

    private String hint1() {
        String str = "您还没有设置专属于您的主卧灯或次卧灯";
        return str;
    }

    private String hint2(String name) {
        String str = "您还没有设置专属于您的+" + name;
        return str;
    }
}
