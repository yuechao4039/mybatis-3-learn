package com.sndj;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author yuechao 2018/7/31
 */
public class BeanC implements BeanB {
    public void test(Statement statement) throws SQLException {
        System.out.println("TSSSS");
        ResultSet ts = statement.getResultSet();
        while (ts.next()) {
            System.out.println(ts.getInt(1));
        }

    }



}