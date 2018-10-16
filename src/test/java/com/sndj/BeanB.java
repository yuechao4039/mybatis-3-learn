package com.sndj;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author yuechao 2018/7/31
 */
public interface BeanB {
    void test(Statement statement) throws SQLException;
}
