package cn.com.lcase.app.model;

import java.io.Serializable;

public class ControlDevice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2876293986117883391L;


    private Integer id;//主键
    private String devicecode;//设备编号
    private String mqttaccount;//MQTT账户
    private String mqttpwd;//MQTT密码
    private String clientID;//ClientID
    private boolean registered;//是否被注册
    private String userid;//userid
    private String createtime;//创建时间

    private String registeredvo;

    private String phone;

    /**
     * @return the registeredvo
     */
    public String getRegisteredvo() {
        return registeredvo;
    }

    /**
     * @param registeredvo the registeredvo to set
     */
    public void setRegisteredvo(String registeredvo) {
        this.registeredvo = registeredvo;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the devicecode
     */
    public String getDevicecode() {
        return devicecode;
    }

    /**
     * @param devicecode the devicecode to set
     */
    public void setDevicecode(String devicecode) {
        this.devicecode = devicecode;
    }

    /**
     * @return the mqttaccount
     */
    public String getMqttaccount() {
        return mqttaccount;
    }

    /**
     * @param mqttaccount the mqttaccount to set
     */
    public void setMqttaccount(String mqttaccount) {
        this.mqttaccount = mqttaccount;
    }

    /**
     * @return the mqttpwd
     */
    public String getMqttpwd() {
        return mqttpwd;
    }

    /**
     * @param mqttpwd the mqttpwd to set
     */
    public void setMqttpwd(String mqttpwd) {
        this.mqttpwd = mqttpwd;
    }

    /**
     * @return the clientID
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * @param clientID the clientID to set
     */
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /**
     * @return the registered
     */
    public boolean getRegistered() {
        return registered;
    }

    /**
     * @param registered the registered to set
     */
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the createtime
     */
    public String getCreatetime() {
        return createtime;
    }

    /**
     * @param createtime the createtime to set
     */
    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }


}
