package cn.com.lcase.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 2016/11/7.
 */

public class Version implements Parcelable {

    private int id;
    private String versionName;
    private long updatetime;
    private String reason;
    private String url;
    private int versioncode;

    public void setId(int id) {
        this.id = id;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setUpdatetime(long updatetime) {
        this.updatetime = updatetime;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersioncode(int versioncode) {
        this.versioncode = versioncode;
    }

    public int getId() {
        return id;
    }

    public String getVersionName() {
        return versionName;
    }

    public long getUpdatetime() {
        return updatetime;
    }

    public String getReason() {
        return reason;
    }

    public String getUrl() {
        return url;
    }

    public int getVersioncode() {
        return versioncode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.versionName);
        dest.writeLong(this.updatetime);
        dest.writeString(this.reason);
        dest.writeString(this.url);
        dest.writeInt(this.versioncode);
    }

    public Version() {
    }

    protected Version(Parcel in) {
        this.id = in.readInt();
        this.versionName = in.readString();
        this.updatetime = in.readLong();
        this.reason = in.readString();
        this.url = in.readString();
        this.versioncode = in.readInt();
    }

    public static final Creator<Version> CREATOR = new Creator<Version>() {
        public Version createFromParcel(Parcel source) {
            return new Version(source);
        }

        public Version[] newArray(int size) {
            return new Version[size];
        }
    };
}
