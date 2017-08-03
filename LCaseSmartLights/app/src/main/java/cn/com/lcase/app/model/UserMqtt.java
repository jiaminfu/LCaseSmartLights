package cn.com.lcase.app.model;

import java.io.Serializable;

public class UserMqtt implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4558813876955136392L;
	//主键
	private Integer id;
	//mqtt账号
	private String mqttaccount;
	//mqtt密码
	private String mqttpassword;
	//Client ID
	private String mqttclientid;
	//创建时间
	private String createtime;
	//状态
	private boolean status;
	//用户ID
	private Integer userid;
	
	private String usercode;
	
	private String phone;
	private String statusvo;
	
	
	
	/**
	 * @return the usercode
	 */
	public String getUsercode() {
		return usercode;
	}
	/**
	 * @param usercode the usercode to set
	 */
	public void setUsercode(String usercode) {
		this.usercode = usercode;
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
	 * @return the mqttpassword
	 */
	public String getMqttpassword() {
		return mqttpassword;
	}
	/**
	 * @param mqttpassword the mqttpassword to set
	 */
	public void setMqttpassword(String mqttpassword) {
		this.mqttpassword = mqttpassword;
	}
	/**
	 * @return the mqttclientid
	 */
	public String getMqttclientid() {
		return mqttclientid;
	}
	/**
	 * @param mqttclientid the mqttclientid to set
	 */
	public void setMqttclientid(String mqttclientid) {
		this.mqttclientid = mqttclientid;
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
	 * @return the status
	 */
	public boolean isStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}
	/**
	 * @return the userid
	 */
	public Integer getUserid() {
		return userid;
	}
	/**
	 * @param userid the userid to set
	 */
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	/**
	 * @return the statusvo
	 */
	public String getStatusvo() {
		return statusvo;
	}
	/**
	 * @param statusvo the statusvo to set
	 */
	public void setStatusvo(String statusvo) {
		this.statusvo = statusvo;
	}
	
	
	
}
