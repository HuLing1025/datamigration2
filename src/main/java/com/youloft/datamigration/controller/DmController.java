package com.youloft.datamigration.controller;

import com.youloft.datamigration.main.Job;
import com.youloft.datamigration.service.DmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class DmController {
    @Autowired
    private DmService dmService;
    @Autowired
    private Job job;

    @GetMapping("/maxid")
    public HashMap<String, Object> count(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("data",dmService.count());
        result.put("message", "成功获取ID最大值!");
        result.put("code", 200);
        return result;
    }

    @GetMapping("/migration")
    public HashMap<String, Object> data() throws InterruptedException, SQLException {
        HashMap<String, Object> result = new HashMap<>();
        job.autoJob();
        result.put("data","阿巴阿巴");
        result.put("message", "服务启动,请等待!");
        result.put("code", 200);
        return result;
    }
}
