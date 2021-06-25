package com.youloft.datamigration.serviceImp;

import com.youloft.datamigration.dao.DmDao;
import com.youloft.datamigration.pojo.DmPojo;
import com.youloft.datamigration.service.DmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Component
public class DmServiceImp implements DmService {
    @Autowired
    private DmDao dmDao;


    @Override
    public long count() {
        return dmDao.count();
    }

    @Override
    public List<DmPojo> selectPushData(long begin, long end) {
        return dmDao.selectPushData(begin, end);
    }
}
