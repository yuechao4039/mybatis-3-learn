package com.sndj.financial;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author yuechao 2018/7/29
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
})
public class ShardTableInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        String id = ms.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        String methodName = id.substring(id.lastIndexOf(".") + 1);
        Class<?> clazz = Class.forName(className);
        Method method = null; // ReflectionUtils.findMethod(clazz, methodName, (Class[])null);
        if (method.isAnnotationPresent(ShardingTable.class)) {

            Object[] args = invocation.getArgs();

            Object parameterObject = args[1];

            String originalSQL = ms.getSqlSource().getBoundSql(parameterObject).getSql();

            ShardingTable shardingTable = method.getAnnotation(ShardingTable.class);
            ShardTableStrategy strategy = new ConcatenateStrategy();
            String shardSQL  = strategy.convert(originalSQL, parameterObject, shardingTable);


            SqlSource sqlSource =  new StaticSqlSource(ms.getConfiguration(), shardSQL,
                    ms.getSqlSource().getBoundSql(parameterObject).getParameterMappings());

            String newMsID = strategy.getNewMsId(ms.getId(), shardingTable, parameterObject);
            MappedStatement newMs = newCountMappedStatement(ms, sqlSource, newMsID);
            args[0] = newMs;
            Object result =  invocation.proceed();
            return result;
        }
        return invocation.proceed();

    }
    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList(0);

    public static MappedStatement newCountMappedStatement(MappedStatement ms, SqlSource sqlSource, String newMsId) {

        // 生成新的sqlSource
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), newMsId, sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());

        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            String[] var4 = ms.getKeyProperties();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String keyProperty = var4[var6];
                keyProperties.append(keyProperty).append(",");
            }

            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }


        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());

        // 直接从源ms拿取
//        List<ResultMap> resultMaps = new ArrayList();
//        ResultMap resultMap = (new org.apache.ibatis.mapping.ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class, EMPTY_RESULTMAPPING)).build();
//        resultMaps.add(resultMap);
//        builder.resultMaps(resultMaps);
        builder.resultMaps(ms.getResultMaps());

        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }


    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
