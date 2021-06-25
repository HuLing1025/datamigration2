package com.youloft.datamigration.daoImp;

import com.youloft.datamigration.dao.MogoDao;
import com.youloft.datamigration.pojo.DmPojo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MogoDaoImp implements MogoDao {
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void saveList(List<DmPojo> list, String collectionName) {
        mongoTemplate.insert(list,collectionName);
    }

}
