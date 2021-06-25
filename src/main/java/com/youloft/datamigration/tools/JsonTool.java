package com.youloft.datamigration.tools;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

public class JsonTool {
    public static JSONObject getMessJson(String mess){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgtype", "text");
        HashMap<String, Object> text = new HashMap<>();
        text.put("content", "业务报警:" + mess);
        jsonObject.put("text",text);
        return jsonObject;
    }
}
