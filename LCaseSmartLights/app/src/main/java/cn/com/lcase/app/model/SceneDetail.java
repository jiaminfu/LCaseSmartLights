package cn.com.lcase.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class SceneDetail implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4132789286119198365L;

	private Integer id;
	private Integer sceneid;
	private Integer deviceid;
	private boolean onoff;
	
	
	private String devicename;
	private String groupname;
	private int  image;
	private String code;
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	private String name;

	
	private String onoffvo;
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
	 * @return the sceneid
	 */
	public Integer getSceneid() {
		return sceneid;
	}
	/**
	 * @param sceneid the sceneid to set
	 */
	public void setSceneid(Integer sceneid) {
		this.sceneid = sceneid;
	}
	/**
	 * @return the deviceid
	 */
	public Integer getDeviceid() {
		return deviceid;
	}
	/**
	 * @param deviceid the deviceid to set
	 */
	public void setDeviceid(Integer deviceid) {
		this.deviceid = deviceid;
	}
	/**
	 * @return the status
	 */
	
	/**
	 * @return the onoffvo
	 */
	public String getOnoffvo() {
		return onoffvo;
	}
	/**
	 * @param onoffvo the onoffvo to set
	 */
	public void setOnoffvo(String onoffvo) {
		this.onoffvo = onoffvo;
	}
	/**
	 * @return the onoff
	 */
	public boolean isOnoff() {
		return onoff;
	}
	/**
	 * @param onoff the onoff to set
	 */
	public void setOnoff(boolean onoff) {
		this.onoff = onoff;
	}
	/**
	 * @return the devicename
	 */
	public String getDevicename() {
		return devicename;
	}
	/**
	 * @param devicename the devicename to set
	 */
	public void setDevicename(String devicename) {
		this.devicename = devicename;
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


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(this.id);
		dest.writeValue(this.sceneid);
		dest.writeValue(this.deviceid);
		dest.writeByte(this.onoff ? (byte) 1 : (byte) 0);
		dest.writeString(this.devicename);
		dest.writeString(this.groupname);
		dest.writeString(this.onoffvo);
	}

	public SceneDetail() {
	}

	protected SceneDetail(Parcel in) {
		this.id = (Integer) in.readValue(Integer.class.getClassLoader());
		this.sceneid = (Integer) in.readValue(Integer.class.getClassLoader());
		this.deviceid = (Integer) in.readValue(Integer.class.getClassLoader());
		this.onoff = in.readByte() != 0;
		this.devicename = in.readString();
		this.groupname = in.readString();
		this.onoffvo = in.readString();
	}

	public static final Creator<SceneDetail> CREATOR = new Creator<SceneDetail>() {
		@Override
		public SceneDetail createFromParcel(Parcel source) {
			return new SceneDetail(source);
		}

		@Override
		public SceneDetail[] newArray(int size) {
			return new SceneDetail[size];
		}
	};
}
