package com.youloft.datamigration.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CityCode {
    // CityCode
    private String cityCode;
    // 0:不存在表; 1:有数据,没有索引; 2:已建索引
    private int status;
    public CityCode(String cityCode) {
        this.cityCode = cityCode;
    }
}
