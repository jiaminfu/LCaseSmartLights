package cn.com.lcase.app.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Scene implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4980884773421655650L;
	private Integer sid;
	private Integer userid;
	private String scenename;
	private String createtime;
	private String image;
	private boolean status;
	private List<SceneDetail> sceneDetail;

	/**
	 * @return the sid
	 */
	public Integer getSid() {
		return sid;
	}

	/**
	 * @param sid the sid to set
	 */
	public void setSid(Integer sid) {
		this.sid = sid;
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
	 * @return the scenename
	 */
	public String getScenename() {
		return scenename;
	}

	/**
	 * @param scenename the scenename to set
	 */
	public void setScenename(String scenename) {
		this.scenename = scenename;
	}

	/**
	 * @return the sceneDetail
	 */
	public List<SceneDetail> getSceneDetail() {
		return sceneDetail;
	}

	/**
	 * @param sceneDetail the sceneDetail to set
	 */
	public void setSceneDetail(List<SceneDetail> sceneDetail) {
		this.sceneDetail = sceneDetail;
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
	 * @return the image
	 */
	public String getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(String image) {
		this.image = image;
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


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(this.sid);
		dest.writeValue(this.userid);
		dest.writeString(this.scenename);
		dest.writeString(this.createtime);
		dest.writeString(this.image);
		dest.writeByte(status ? (byte) 1 : (byte) 0);
		dest.writeList(this.sceneDetail);
	}

	public Scene() {
	}

	protected Scene(Parcel in) {
		this.sid = (Integer) in.readValue(Integer.class.getClassLoader());
		this.userid = (Integer) in.readValue(Integer.class.getClassLoader());
		this.scenename = in.readString();
		this.createtime = in.readString();
		this.image = in.readString();
		this.status = in.readByte() != 0;
		this.sceneDetail = new ArrayList<SceneDetail>();
		in.readList(this.sceneDetail, List.class.getClassLoader());
	}

	public static final Creator<Scene> CREATOR = new Creator<Scene>() {
		public Scene createFromParcel(Parcel source) {
			return new Scene(source);
		}

		public Scene[] newArray(int size) {
			return new Scene[size];
		}
	};
}
