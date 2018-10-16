package com.sndj.fs;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * Executor中有两个query方法，为什么下面的这个query方法无法拦截?(为方便区分第一个为query1，第二个query2)
 *
 * 假设代理类为A,实际类为B，调用A.query1的时候实际是：
 * A.query1(...){
 *     前置操作方法...
 *     Object result = B.query1(...);
 *     后置方法...
 *     return result;
 * }
 * 在B中query1方法中调用自己的另一个query方法的时候，是this.query2(...)，
 * 也就是B.query2(...)，并不是调用的A.query2(...)，所以A中针对query2的代理方法无效
 */
@Intercepts(@Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
//@Intercepts(@Signature(type = Executor.class, method = "query",
//        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}))
public class SampleExecutorInterceptor implements Interceptor {

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
        //了解ms是因为Executor最常拦截的方法就是query，在另一个update方法中的第一个参数也是ms
        //由于一个方法对应一个ms，所以如果需要修改ms，在高并发的情况下，所有调用该方法的ms都会受影响
        //如果是需要统一修改ms的情况，那就不需要考虑这种情况，例如通用Mapper就是对ms做了修改
        MappedStatement ms = (MappedStatement) args[0];
        //在分页插件中，由于需要靠PageHelper.startPage来决定是否分页，就会出现有的ms不需要分页，有的需要分页
        //这种情况必须解决上面这种问题
        //解决办法就是创建一个新的newMs，然后将args[0]=newMs就能解决
        //为什么修改args能解决?
        //我们看看proceed()方法
        return invocation.proceed();
    }

    /**
     * 只拦截Executor
     *
     * @param target
     * @return
     */
    public Object plugin(Object target) {
        if (target instanceof Executor) {
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
    }
}
