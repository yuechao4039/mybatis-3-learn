package com.sndj.fs;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LoggingCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.SynchronizedCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 通过本段代码了解MyBatis内部的一些实现
 *
 * @author liuzh
 * @since 2015-11-24 23:04
 */
public class SimpleMyBatis {

    public final Logger logger = Logger.getLogger(SimpleMyBatis.class);

    @Test
    public void test() throws IOException, SQLException {
        //创建配置文件
        final Configuration config = new Configuration();
        config.setCacheEnabled(true);
        config.setLazyLoadingEnabled(false);
        config.setAggressiveLazyLoading(true);

        //为了后续说明拦截器，这里添加两个简单例子
        config.addInterceptor(new SampleExecutorInterceptor());
        config.addInterceptor(new SampleResultSetHandlerInterceptor());

        //创建DataSource
        Properties props = Resources.getResourceAsProperties("jdbc.properties");
        UnpooledDataSource dataSource = new UnpooledDataSource();
        dataSource.setDriver(props.getProperty("driver"));
        dataSource.setUrl(props.getProperty("url"));
        dataSource.setUsername(props.getProperty("username"));
        dataSource.setPassword(props.getProperty("password"));

        //创建Executor
        //<transactionManager type="JDBC"/>
        Transaction transaction = new JdbcTransaction(dataSource, null, false);

        //config.newExecutor会将符合条件的拦截器添加到Executor代理链上
        final Executor executor = config.newExecutor(transaction);

        //cache是一个多层代理【装饰模式】的缓存对象，通过一级一级代理使得一个简单的缓存拥有了复杂的功能
        //<cache/>
        final Cache countryCache =
                new SynchronizedCache(//同步缓存
                        new SerializedCache(//序列化缓存
                                new LoggingCache(//日志缓存
                                        new LruCache(//最少使用缓存
                                                new PerpetualCache("country_cache")//持久缓存
                                        ))));


        //类型处理注册器
        //自己写TypeHandler的时候可以参考该注册器中已经存在的大量实现
        final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

        //================== 下面的步骤相当于解析XML或者解析接口注解方法生成ms =====================

        //创建静态sqlSource
        //最简单的，相当于从xml或接口注解获取SQL，创建合适的sqlSource对象
        StaticSqlSource sqlSource = new StaticSqlSource(config, "SELECT * FROM country WHERE id = ?");

        //由于上面的SQL有个参数id，这里需要提供ParameterMapping(参数映射)
        List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
        //通过ParameterMapping.Builder创建ParameterMapping
        parameterMappings.add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
        //通过ParameterMap.Builder创建ParameterMap
        ParameterMap.Builder paramBuilder = new ParameterMap.Builder(config, "defaultParameterMap", Country.class, parameterMappings);

        //创建ms
        MappedStatement.Builder msBuilder = new MappedStatement.Builder(config, "tk.mybatis.selectCountry", sqlSource, SqlCommandType.SELECT);
        msBuilder.parameterMap(paramBuilder.build());

        //<resultMap>
        final ResultMap resultMap = new ResultMap.Builder(config, "defaultResultMap", Country.class,
                new ArrayList<ResultMapping>() {
                    {
                        add(new ResultMapping.Builder(config, "id", "id", int.class).build());
                        add(new ResultMapping.Builder(config, "countryname", "countryname", String.class).build());
                        add(new ResultMapping.Builder(config, "countrycode", "countrycode", registry.getTypeHandler(String.class)
                        ).build());
                    }
                }).build();

        //2：不设置具体的映射，只是用类型，相当于只配置resultType="tk.mybatis.sample1.Country"
        //final ResultMap resultMap = new ResultMap.Builder(config, "defaultResultMap", Country.class, new ArrayList<ResultMapping>()).build();

        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        resultMaps.add(resultMap);
        //设置返回值的resultMap和resultType
        msBuilder.resultMaps(resultMaps);
        //设置缓存
        msBuilder.cache(countryCache);

        //创建ms
        MappedStatement ms = msBuilder.build();

        //第一种使用executor执行
        List<Country> countries = executor.query(ms, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);

        for (Country country : countries) {
            logger.info(country);
        }

        //第二种
        //首先添加ms到config
        config.addMappedStatement(ms);
        //创建sqlSession
        SqlSession sqlSession = new DefaultSqlSession(config, executor, false);
        //查询
        Country country = sqlSession.selectOne("selectCountry", 35);
        logger.info(country);
        //关闭
        sqlSession.close();
    }
}
