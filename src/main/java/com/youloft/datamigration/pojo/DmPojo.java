package com.youloft.datamigration.pojo;

import com.youloft.datamigration.Global;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.HashMap;

public class DmPojo implements Serializable {
//    @Id
//    public ObjectId id;
    public String Brand;
    public String UserId;
    public String Token;
    public String Bd;
    public String Imei;
    public String Idfa;
    public String[] Tags;
    public String PushSound;
    public String Did;
    public String Enabled;
    public String AppDeviceType;
    public HashMap<String, Object> Ext;
    // 透明字段,不存储到mongodb
//    @Transient
//    public Date lastUpdateDate;
    @Transient
    public int VersionType;
    @Transient
    public String OsVer;
    @Transient
    public String cityCode;
    @Transient
    public int Pushbooktype;
    @Transient
    public String versiontype;

    public DmPojo(){
        Bd = Global.BD;
        Brand = Global.BRAND;
        Imei = "";
        Idfa = "";
        Enabled = "true";
        Pushbooktype = 7;
    }
}
