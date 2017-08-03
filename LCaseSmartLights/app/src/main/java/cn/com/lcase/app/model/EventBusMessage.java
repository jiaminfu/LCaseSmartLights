package cn.com.lcase.app.model;

/**
 * Created by Wxm on 2016/11/19.
 */

public class EventBusMessage {
    /**
     * finish登录界面
     */
    public static final String ACTION_FINISH_LOGIN_ACTIVITY = "finish_login_activity";
    /**
     * finish主界面
     */
    public static final String ACTION_FINISH_MAIN_ACTIVITY = "finish_main_activity";

    /**
     * 收到新的消息
     */
    public static final String RECEIVE_MESSAGE= "receive_message";


    private String action;

    private String msg;

    public EventBusMessage(String action, String msg) {
        this.action = action;
        this.msg = msg;
    }

//    public EventBusMessage(String action) {
//        this.action = action;
//    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


}
