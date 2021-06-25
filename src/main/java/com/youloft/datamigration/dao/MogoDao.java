package com.youloft.datamigration.dao;

import com.youloft.datamigration.pojo.DmPojo;

import java.util.List;

public interface MogoDao {
    void saveList(List<DmPojo> list, String collectionName);
}
