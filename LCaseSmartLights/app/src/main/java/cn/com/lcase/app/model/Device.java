package cn.com.lcase.app.model;

import java.io.Serializable;

public class Device implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4160794200132963783L;

    private Integer id;

    //@TableField(column="name" ,desc="设备名称")
    private String name;

    //@TableField(column="code",desc="设备编号")
    private String code;

    //@TableField(column="groupid",desc="所属分组id")
    private Integer groupid;


    //@TableField(column="type",desc="设备类型")
    private String type;//1 开关  2电视

    //@TableField(column="ispublic",desc="公共权限")
    private boolean ispublic;

    private String ispublicvo;

    private int image = -1;

    public boolean ispublic() {
        return ispublic;
    }

    private String userid;

    //电话
    private String phone;

    //分组名称
    private String groupname;

    //开关状态
    private boolean onoff;

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    //是否专属设备
    private boolean unique;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    private boolean enable;


    private boolean checked = false;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isOnoff() {
        return onoff;
    }

    public void setOnoff(boolean onoff) {
        this.onoff = onoff;
    }

    /**
     * @return the ispublicvo
     */
    public String getIspublicvo() {
        return ispublicvo;
    }

    /**
     * @param ispublicvo the ispublicvo to set
     */
    public void setIspublicvo(String ispublicvo) {
        this.ispublicvo = ispublicvo;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the groupid
     */
    public Integer getGroupid() {
        return groupid;
    }

    /**
     * @param groupid the groupid to set
     */
    public void setGroupid(Integer groupid) {
        this.groupid = groupid;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the ispublic
     */
    public boolean isIspublic() {
        return ispublic;
    }

    /**
     * @param ispublic the ispublic to set
     */
    public void setIspublic(boolean ispublic) {
        this.ispublic = ispublic;
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
     * @return the image
     */
    public int getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(int image) {
        this.image = image;
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

    public void toggle() {
        this.checked = !this.checked;
    }


}
