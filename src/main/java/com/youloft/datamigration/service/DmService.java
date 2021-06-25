package com.youloft.datamigration.service;

import com.youloft.datamigration.pojo.DmPojo;

import java.util.List;

public interface DmService {
    long count();
    List<DmPojo> selectPushData(long begin, long end);
}
