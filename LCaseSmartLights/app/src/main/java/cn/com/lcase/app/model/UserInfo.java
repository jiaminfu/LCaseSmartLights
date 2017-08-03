package cn.com.lcase.app.model;

import java.io.Serializable;

public class UserInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5027432571579789447L;

	private Integer id;
	
	//用户编号
	private String usercode;
	
//电话号码
	private String phone ;
	
	//用户名
	private String username;
	//注册时间
	private String createtime;
	//状态
	private boolean enable;
	//是否主账户
	private boolean mainaccount;

	public boolean isMainaccount() {
		return mainaccount;
	}

	public void setMainaccount(boolean mainaccount) {
		this.mainaccount = mainaccount;
	}

	//父账号ID
	private Integer parentid;
	//密码
	private String password;
	
	//中控编码
	private String controlcode;
	
	//所属主账户
	private String parenttel;
	//验证码
	private String verifyCode;
	private String email;
	
	private String enablevo;

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
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
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
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
	 * @return the enable
	 */
	public boolean isEnable() {
		return enable;
	}
	/**
	 * @param enable the enable to set
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	/**
	 * @return the parentid
	 */
	public Integer getParentid() {
		return parentid;
	}
	/**
	 * @param parentid the parentid to set
	 */
	public void setParentid(Integer parentid) {
		this.parentid = parentid;
	}
	/**
	 * @return the userpwd
	 */
	public String getUserpwd() {
		return password;
	}
	/**
	 * @param userpwd the userpwd to set
	 */
	public void setUserpwd(String userpwd) {
		this.password = userpwd;
	}
	/**
	 * @return the controlcode
	 */
	public String getControlcode() {
		return controlcode;
	}
	/**
	 * @param controlcode the controlcode to set
	 */
	public void setControlcode(String controlcode) {
		this.controlcode = controlcode;
	}
	/**
	 * @return the parenttel
	 */
	public String getParenttel() {
		return parenttel;
	}
	/**
	 * @param parenttel the parenttel to set
	 */
	public void setParenttel(String parenttel) {
		this.parenttel = parenttel;
	}
	/**
	 * @return the enablevo
	 */
	public String getEnablevo() {
		return enablevo;
	}
	/**
	 * @param enablevo the enablevo to set
	 */
	public void setEnablevo(String enablevo) {
		this.enablevo = enablevo;
	}
	
	
}
