package cn.com.lcase.app.model;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -374471093527046425L;

	private Integer id;
	
//	@TableField(column="userid",desc="用户iD")
	private Integer userid;
	
//	@TableField(column="groupname",desc="分组名称")
	private String groupname;
	
//	@TableField(column="createtime",desc="创建时间")
	private String createtime;
	
//	@TableField(desc="创建时间",forkey=true)
	private String userphone;
//	@TableField(desc="设备数量",forkey=true)
	private Integer devicecount;
	
	private boolean system;
	
	
	private int groupimage;

	private boolean checked = false;

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	private List<Device> devicelist;

	public List<Device> getDevicelist() {
		return devicelist;
	}

	public void setDevicelist(List<Device> devicelist) {
		this.devicelist = devicelist;
	}

	/**
	 * @return the userphone
	 */
	public String getUserphone() {
		return userphone;
	}

	/**
	 * @param userphone the userphone to set
	 */
	public void setUserphone(String userphone) {
		this.userphone = userphone;
	}

	/**
	 * @return the devicecount
	 */
	public Integer getDevicecount() {
		return devicecount;
	}

	/**
	 * @param devicecount the devicecount to set
	 */
	public void setDevicecount(Integer devicecount) {
		this.devicecount = devicecount;
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
	 * @return the groupname
	 */
	public String getGroupname() {
		return groupname;
	}

	/**
	 * @param groupname the groupname to set
	 */
	public void setGroupname(String groupname) {
		this.groupname = groupname;
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
	 * @return the groupimage
	 */
	public int getGroupimage() {
		return groupimage;
	}

	/**
	 * @param groupimage the groupimage to set
	 */
	public void setGroupimage(int groupimage) {
		this.groupimage = groupimage;
	}

	/**
	 * @return the system
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * @param system the system to set
	 */
	public void setSystem(boolean system) {
		this.system = system;
	}

	public void toggle() {
		this.checked = !this.checked;
	}




}
