package cn.com.lcase.app.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 2016/10/31.
 */

public class Us implements Parcelable {

    private String servicetel;
    private String email;
    private String publicnum;
    private String publiccode;
    private String startimage;

    public void setServicetel(String servicetel) {
        this.servicetel = servicetel;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPublicnum(String publicnum) {
        this.publicnum = publicnum;
    }

    public void setPubliccode(String publiccode) {
        this.publiccode = publiccode;
    }

    public void setStartimage(String startimage) {
        this.startimage = startimage;
    }

    public String getServicetel() {
        return servicetel;
    }

    public String getEmail() {
        return email;
    }

    public String getPublicnum() {
        return publicnum;
    }

    public String getPubliccode() {
        return publiccode;
    }

    public String getStartimage() {
        return startimage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.servicetel);
        dest.writeString(this.email);
        dest.writeString(this.publicnum);
        dest.writeString(this.publiccode);
        dest.writeString(this.startimage);
    }

    public Us() {
    }

    protected Us(Parcel in) {
        this.servicetel = in.readString();
        this.email = in.readString();
        this.publicnum = in.readString();
        this.publiccode = in.readString();
        this.startimage = in.readString();
    }

    public static final Creator<Us> CREATOR = new Creator<Us>() {
        public Us createFromParcel(Parcel source) {
            return new Us(source);
        }

        public Us[] newArray(int size) {
            return new Us[size];
        }
    };
}
