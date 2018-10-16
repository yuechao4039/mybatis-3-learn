package com.sndj;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * @Intercepts 可以配置多个签名，可以同时拦截多个类型多个方法
 */
@Intercepts(@Signature(type = BeanB.class, method = "test",
        args = {Statement.class}))
public class SampleResultSetHandlerInterceptor implements Interceptor {

    /**
     * Mybatis拦截器方法
     *
     * @param invocation 拦截器入参
     * @return 返回执行结果
     * @throws Throwable 抛出异常
     */
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("===================拦截器-" + invocation.getMethod().getName() + "===================");
        Object[] args = invocation.getArgs();
        Statement statement = (Statement) args[0];
        ResultSet rs = statement.executeQuery("select count(*) as total from country ");
        if(rs.next()){
            int count = rs.getInt("total");
            System.out.println("Total:" + count);
        }
        //调用实际执行方法，在这个方法前后可以做任何操作
        List<Object> list = (List<Object>) invocation.proceed();

        return list;
    }

    /**
     * 只拦截Executor
     *
     * @param target
     * @return
     */
    public Object plugin(Object target) {
        if (target instanceof ResultSetHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    /**
     * 设置属性值
     *
     * @param p 属性值
     */
    public void setProperties(Properties p) {
        //可以给拦截器设置参数
    }
}
