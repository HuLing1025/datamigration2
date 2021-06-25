package com.youloft.datamigration;

public class Global {
    // mongodb文档名称
    //public static final String COLLECTION_NAME = "push_com_ireadercity_zhwll_db";
    // 应用程序包名,这个是固定的,数据库里面的appid是外键,为了优化查询效率直接写死
    public static final String BD = "com.ireadercity.zhwll";
    // 渠道: apple
    public static final String BRAND = "apple";
    // 标签: notitle
    public static final String TAG_NOTITLE = "notitle";
    // 标签: hastitle
    public static final String TAG_HASTITLE = "hastitle";
    // 迁移数据的开始时间 格式: 秒 分 时 日 月 年
    public static final String CRONEXPRESS_MIGRATION =  "0 30 22 31 3 ? ";
    // 钉钉机器人webhook
    public static final String WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=cb3662947261735841724f24219db4aa17c17b39cf7958d03c58ec1f205d45e7";
}
