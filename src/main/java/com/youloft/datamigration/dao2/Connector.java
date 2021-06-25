package com.youloft.datamigration.dao2;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connector {
    public static Connection GetConnecter() {
        String sqlDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String url = "jdbc:sqlserver://rm-m5e73o88b9gh88n9l.sqlserver.rds.aliyuncs.com:3433;DatabaseName=wnlrecomdb";
        String user = "admin_wnl";
        String password = "killers9Y";
        Connection conn = null;

        try{
            Class.forName(sqlDriver).newInstance();
            conn = DriverManager.getConnection(url,user,password);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
