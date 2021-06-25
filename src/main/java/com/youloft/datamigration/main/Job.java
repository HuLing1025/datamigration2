package com.youloft.datamigration.main;

import com.youloft.datamigration.Global;
import com.youloft.datamigration.dao.MogoDao;
import com.youloft.datamigration.dao2.Connector;
import com.youloft.datamigration.pojo.DmPojo;
import com.youloft.datamigration.service.DmService;
import com.youloft.datamigration.tools.DDRobot;
import com.youloft.datamigration.tools.JsonTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Job {
    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private DmService dmService;
    @Autowired
    private MogoDao mogoDao;
    @Autowired
    private DDRobot ddRobot;

    // collection hashmap, the integer is a flag
    // and it used to determine whether to create a collection or a table
    private HashMap<String, Integer> cityCodes;

    public boolean initCityCodes() throws SQLException {
        cityCodes = new HashMap<>();
        Connection con = Connector.GetConnecter();
        if (con == null) {
            return false;
        }
        Statement statement = con.createStatement();
        String sql = "SELECT CityCode FROM GeoCity";
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()){
            cityCodes.put(rs.getString("CityCode"), 0);
        }
        return cityCodes.size() > 1;
    }

    public void autoJob(){
        // 0.初始化CityCode(HashMap)
        try{
            if(!initCityCodes()){
                log("CityCode初始化异常: CityCodes长度 " + cityCodes.size());
            }
        }catch (SQLException e){
            log("sqlserver异常(cityCode):" +e.getMessage());
        }
        log("MongoDB集合总数: " + cityCodes.size());

        // 1.查询记录总数(其实是根据ID范围来查的,查找的指标是ID的最大值)
        long total = dmService.count() + 1;
        // 2.设置单次查询记录总条数
        long onceDealNum = 10000;
        // 3.设置单个线程处理页数
        long onceExeDealNum = 500;
        // 4.计算总页数
        long pageNum = total%onceDealNum == 0 ? total/onceDealNum : total/onceDealNum + 1;
        // 5.划分线程数
        long exeNum = pageNum%onceExeDealNum == 0 ? pageNum/onceExeDealNum : pageNum/onceExeDealNum + 1;

        // log
        log("总记录数:" + total + ", 总页数:" + pageNum + ", 所需线程数:" + exeNum);
        // 发送到钉钉
        ddRobot.post(JsonTool.getMessJson("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + "开始处理:" + "MAX(ID)=" + (total -1) + ",page=" + pageNum + ",onceDeal=" + onceDealNum + ",onceExeDealNum=" + onceExeDealNum + ",exe=" + exeNum));

        // 6.创建一个会根据需要创建新线程的线程池
        ExecutorService es3 = Executors.newCachedThreadPool();
        for (long  i = 0; i < exeNum; ++i) {
            // 线程安全处理，避免直接使用i
            long exeId = i;
            es3.submit(() -> {
                // log
                log("开始执行任务!");

                // 存入数据暂存区
                HashMap<String, List<DmPojo>> store = new HashMap<>();
                // 7.根据线程分配的页总数,一页一页地处理
                for(long page = exeId * onceExeDealNum; page < (exeId + 1) * onceExeDealNum && page <= pageNum; ++page){
                    // 7.1从sqlserver查询结果集
                    List<DmPojo> list;
                    try{
                        list = dmService.selectPushData(page * onceDealNum, (page + 1) * onceDealNum);
                    }catch (Exception e){
                        log("sqlserver异常:" + e.toString());
                        // 发送到钉钉
                        // ddRobot.post(JsonTool.getMessJson("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + " sqlserver异常:" + e.getMessage()));
                        // 回溯重试
                        --page;
                        continue;
                    }
                    //log
                    log("页码:" + page + "\t单次处理规模:" + onceDealNum + "\t实际处理:" + list.size() );

                    // 7.2转储之前处理一下数据
                    for (DmPojo dmPojo : list) {
                        // 脏数据不处理,CityCode为空
                        if (dmPojo.cityCode == null || "".equals(dmPojo.cityCode)) {
                            continue;
                        }
                        List<String> tags = new LinkedList<>();
                        HashMap<String, Object> ext = new HashMap<>();
                        // make some change of VersionType(String to int)
                        int sum;
                        StringBuilder str = new StringBuilder();
                        if (dmPojo.versiontype == null || "".equals(dmPojo.versiontype) || "0".equals(dmPojo.versiontype)) {
                            sum = 300;
                        } else {
                            String[] splits = dmPojo.versiontype.split("\\.");
                            for (String s : splits) {
                                str.append(s);
                            }
                            /*
                             * debug log:
                             * 之前这里没有使用 try-catch 捕获 NumberFormatException
                             * 导致2个子线程异常终止
                             * 这里使用正则表达式判断(或者使用try-cache忽略这个脏数据)
                             * */
                            if (str.toString().matches("^[0-9]*$")) {
                                try {
                                    sum = Integer.parseInt(str.toString());
                                } catch (Exception e) {
                                    log("VersionType转换异常: " + e.toString());
                                    str = new StringBuilder("300");
                                    sum = 300;
                                }
                            } else {
                                log("VersionType非正常数据: VersionType \"" + str + "\" to int.");
                                str = new StringBuilder("300");
                                sum = 300;
                            }
                        }
                        // VersionType标签
                        tags.add(str.toString());
                        // 同时存字段
                        dmPojo.VersionType = sum;
                        // 设置Ext
                        ext.put("VersionType", sum);
                        ext.put("WnlType", dmPojo.Pushbooktype);
                        dmPojo.Ext = ext;
                        // 打城市标签
                        tags.add(dmPojo.cityCode);
                        try {
                            // 根据系统版本号打是否有标题标签
                            if (dmPojo.OsVer == null || dmPojo.OsVer.equals("") || Double.parseDouble(dmPojo.OsVer) < 10.0) {
                                tags.add(Global.TAG_NOTITLE);
                            } else {
                                tags.add(Global.TAG_HASTITLE);
                            }
                            // 标签list转数组后设置
                            dmPojo.Tags = new String[3];
                            tags.toArray(dmPojo.Tags);
                        } catch (Exception e) {
                            log("数值转换异常:" + e.toString());
                            // 发送到钉钉
                            // ddRobot.post(JsonTool.getMessJson("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + " 数值转换异常:" + e.getMessage()));
                            // 回溯重试
                            --page;
                            continue;
                        }
                        if (cityCodes.get(dmPojo.cityCode) == null) {
                            continue;
                        }
                        doStore(store, dmPojo.cityCode, dmPojo, 0);
                    }
                    /*
                    * debug log:
                    * 之前这里的逻辑只执行了一次(当 page == pageNum 时,抵达数据库右边界,但是其他线程处理的并没有结束)
                    * 逻辑左值未执行(当 page == exeId * onceExeDealNum + onceExeDealNum 时)
                    * 故左值判断page的值应该还要减一
                    * */
                    if (page == exeId * onceExeDealNum + onceExeDealNum - 1 || page == pageNum) {
                       doStore(store, null, null, 1);
                        /*
                         * debug log:
                         * 删除已经存储的数据,当某个线程结束后
                         * 会有其他线程使用，所以这里还需要清空map
                         * 不然会重复存入导致数据重复
                         * */
                        store = new HashMap<>();
                    }
                }
                // log
                log("结束任务!");
            });
        }
    }

    public void doStore(HashMap<String, List<DmPojo>> store, String key, DmPojo dmPojo, int flag) {
        if (flag == 0){
            store.computeIfAbsent(key, k -> new ArrayList<>());
            List<DmPojo> temp = store.get(key);
            temp.add(dmPojo);
            store.put(key, temp);
            // 当单个key下的List长度达到2000时,触发一次存储
            if (store.get(key).size() == 2000) {
                try{
                    mogoDao.saveList(store.get(key), key);
                    log("mongodb单次存储量: " + store.get(key).size());
                    addIndex(key);
                }catch(Exception e){
                    log("mongodb异常:" + e.toString());
                    // 发送到钉钉
                    // ddRobot.post(JsonTool.getMessJson("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + "mongodb异常:" + e.getMessage()));
                    // 重试
                    try {
                        log("存储重试:");
                        exceptionDeal(key, store.get(key));
                    }catch (Exception e1){
                        log("mongodb重试异常:" + e1.toString());
                        log("重试异常集合:" + key);
                        // 发送到钉钉
                        // ddRobot.post(JsonTool.getMessJson("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + "mongodb重试异常:" + e.getMessage()));
                    }
                }
                // 清除当前key下的数据,避免数据出现重复存入
                store.put(key, new ArrayList<>());
            }
        }else{
            for (String keys : store.keySet()) {
                try{
                    if (keys == null || "".equals(keys)){
                        continue;
                    }
                    mogoDao.saveList(store.get(keys), keys);
                    log("mongodb单次存储量: " + store.get(keys).size());
                    addIndex(keys);
                }catch(Exception e){
                    log("mongodb异常:" + e.toString());
                    // 发送到钉钉
                    // ddRobot.post(JsonTool.getMessJson("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + "mongodb异常:" + e.getMessage()));
                    // 重试
                    try {
                        log("存储重试:");
                        exceptionDeal(keys, store.get(keys));
                    }catch (Exception e1){
                        log("mongodb重试异常:" + e1.toString());
                        log("重试异常集合:" + keys);
                        // 发送到钉钉
                        // ddRobot.post(JsonTool.getMessJson("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + "mongodb重试异常:" + e.getMessage()));
                    }
                }
            }
        }
    }

    // 互斥操作
    public synchronized void changeMode(String cityCode) {
        cityCodes.put(cityCode, cityCodes.get(cityCode) + 1);
    }

    // 互斥操作
    public synchronized int getMode(String cityCode) {
        return cityCodes.get(cityCode);
    }

    public void exceptionDeal(String key, List<DmPojo> list) {
        mongoTemplate.insert(list, key);
    }

    private void log(String message){
        System.out.println("[ " + new Date().toString() + "]" + Thread.currentThread().getName() + "\t" + message);
    }

    /**
     * 加索引
     * */
    public void addIndex(String s) {
        if (getMode(s) == 0) {
            changeMode(s);
        }
        // 加索引
        if (getMode(s) == 1) {
            changeMode(s);
        }else {
            return;
        }
        if(s == null || "".equals(s)) {
            return;
        }
        try{
            Index index = new Index();
            index.on("_id", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("ix_brand", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Bd", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Did", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Token", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Tags", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Enabled", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Ext", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Ext.WnlType", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            index = new Index();
            index.on("Ext.VersionType", Sort.Direction.ASC);
            mongoTemplate.indexOps(s).ensureIndex(index);
            // log("success: " + s);
        }catch (Exception e){
            // 异常集合的索引后期手动加吧
            log("mongodb异常: 索引建立失败, 集合名" + s);
            // 重试
            //-- j;
        }
        log("建立索引成功,key: " + s);
    }
}

