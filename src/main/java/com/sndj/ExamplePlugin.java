package com.sndj;

import lombok.Data;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})

public class ExamplePlugin implements Interceptor {

    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        SqlLog log = new SqlLog();
        Configuration configuration = ms.getConfiguration();
        Object target = invocation.getTarget();
        StatementHandler handler = configuration.newStatementHandler((Executor) target, ms,
                parameter, RowBounds.DEFAULT, null, null);
        BoundSql boundSql = handler.getBoundSql();

        //记录SQL
        log.setSqlclause(boundSql.getSql());
        //执行真正的方法
        Object result = invocation.proceed();
        //记录影响行数
        log.setResult(Integer.valueOf(Integer.parseInt(result.toString())));
        //记录时间
        log.setWhencreated(new java.sql.Date(System.currentTimeMillis()));
        //TODO 还可以记录参数，或者单表id操作时，记录数据操作前的状态
        //获取insertSqlLog方法
        ms = ms.getConfiguration().getMappedStatement("insertSqlLog");
        //替换当前的参数为新的ms
        args[0] = ms;
        //insertSqlLog 方法的参数为 log
        args[1] = log;
//        //执行insertSqlLog方法
         invocation.proceed();
        //返回真正方法执行的结果
        return result;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {

    }
}

@Data
class SqlLog {
    String sqlclause;
    int result;
    java.sql.Date whencreated;
}
