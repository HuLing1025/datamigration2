package com.youloft.datamigration.dao;

import com.youloft.datamigration.pojo.DmPojo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DmDao {
    @Select("SELECT MAX(ID) FROM PushServerTB")
    long count();
//    @Select("SELECT COUNT(*) FROM PushServerTB WHERE appid=4")
//    long count();

    @Select("SELECT PushDeviceToken,DeviceRealid,Userid,PushSound,VersionType,CityCode,OsVer,Idfa,PushSound" +
            " FROM PushServerTB t" +
            " WHERE" +
            " t.ID>=${begin}" +
            " AND t.ID<${end}" +
            " AND appid=4")
    @Results({
            @Result(property = "Token", column = "PushDeviceToken"),
            @Result(property = "Did", column = "DeviceRealid"),
            @Result(property = "Pushbooktype", column = "PushBookType"),
            @Result(property = "versiontype", column = "VersionType"),
    })
    List<DmPojo> selectPushData(long begin, long end);
}
