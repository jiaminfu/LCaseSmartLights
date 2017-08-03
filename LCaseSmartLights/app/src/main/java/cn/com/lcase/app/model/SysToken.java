package cn.com.lcase.app.model;

import java.io.Serializable;

public class SysToken implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 5178460183248946491L;


    private String token;


    private Integer userid;

    private String createtime;

    private int devicecount;

//    private String SDID;//中控编码

    private String username;
    private String sdid;

//    public String getSdid() {
//        return sdid;
//    }

    public void setSdid(String sdid) {
        this.sdid = sdid;
    }

    public String getSDID() {
        return sdid;
    }

//    public void setSDID(String SDID) {
//        this.SDID = SDID;
//    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String phone;

    private boolean mainaccount;


    public boolean isMainaccount() {
        return mainaccount;
    }

    public void setMainaccount(boolean mainaccount) {
        this.mainaccount = mainaccount;
    }

    public int getDevicecount() {
        return devicecount;
    }

    public void setDevicecount(int devicecount) {
        this.devicecount = devicecount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
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


}
