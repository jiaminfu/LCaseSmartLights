package cn.com.lcase.app.client;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.com.lcase.app.MyApplication;
import cn.com.lcase.app.model.EventBusMessage;
import cn.com.lcase.app.net.LCaseConstants;
import cn.com.lcase.app.utils.PreferencesUtil;


public class SubscribeClient {

    private String host = LCaseConstants.MQTT_HOST;
    // private String host = "tcp://192.168.1.150:1883";
    private String userName = "admin";
    private String passWord = "password";
    private String clientId;

    public MqttClient getClient() {
        return client;
    }

    private Handler handler;

    private MqttClient client;


    private String myTopicSubscribe;
    private String myTopicPublish;
    //private String reTopic = "lkbox/remote";


    private MqttConnectOptions options;

    private ScheduledExecutorService scheduler;


    public SubscribeClient(String clientId, String SDID) {
        this.clientId = clientId;
        this.userName = PreferencesUtil.getUserName(MyApplication.instance);
        this.passWord = PreferencesUtil.getUserPassword(MyApplication.instance);
//  myTopicSubscribe = "agents/"+SDID+"/upstream";
//        张工的号
     /*   myTopicSubscribe = "agents/" + "F137A139A4EF4A79B71312AAA860FFBE" + "/upstream";//订阅调试用
        myTopicPublish = "agents/" + "F137A139A4EF4A79B71312AAA860FFBE" + "/downstream";//发布调试用*/
//      我们测试的号 13129573694 123456
       /* myTopicSubscribe = "agents/" + "51A6D89938734BA495CE91A7EC64E543" + "/upstream";//订阅调试用
        myTopicPublish = "agents/" + "51A6D89938734BA495CE91A7EC64E543" + "/downstream";//发布调试用*/
        myTopicSubscribe = "agents/" + SDID + "/upstream";//订阅调试用
        myTopicPublish = "agents/" + SDID + "/downstream";//发布调试用
        init();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {//收到消息
                    // Toast.makeText(MainActivity.this, (String) msg.obj,    Toast.LENGTH_SHORT).show();
//     System.out.println("-----------------------------");
                    String mqttStr = (String) msg.obj;
                    EventBus.getDefault().post(new EventBusMessage(MyApplication.busAction, mqttStr));//busAction 是区分哪个页面发送的消息


                } else if (msg.what == 2) {
                    //  Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    try {
                        client.subscribe(myTopicSubscribe, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (msg.what == 3) {
                      Toast.makeText(MyApplication.instance, "连接失败，请检查下网络状态，也可以尝试清理缓存，重启手机", Toast.LENGTH_LONG).show();
                }
            }
        };

        startReconnect();

    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, clientId,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    connect();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = message.toString();
//     msg.obj = topicName+"---"+message.toString();
//     msg.obj=message;
                    handler.sendMessage(msg);
                }
            });
//          connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    client.connect(options);
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void publish(String ch) {
        try {
            final MqttTopic remoteTopic = client.getTopic(myTopicPublish);

            final MqttMessage message = new MqttMessage(ch.getBytes());
            remoteTopic.publish(message);
            Log.d("Published data. Topic: ", remoteTopic.getName() + "  Message: " + 1 + ">ch:" + ch);
        } catch (MqttException e) {
            Log.d("Published data. Topic: ", "MqttException:" + e.toString());
        }

    }


    public void onDestroy() {
        try {
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}