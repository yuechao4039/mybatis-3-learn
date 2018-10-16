package com.sndj;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author yuechao 2018/7/30
 */
public class BaseTest {
    public static SqlSessionFactory getSessionFactory() {
        String resource = "configuration.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        return sqlSessionFactory;
    }

    SqlSession session = null;

    @Before
    public void before() {
        System.out.println("before invoked");
        session = getSessionFactory().openSession();
    }

    @Test
    public void updateTest() {
        Country country = new Country();
        country.setCountrycode("SH");
        country.setCountryname("上海");
        country.setAccountID(1003);
        country.setId(5);
        CountryMapper countryMapper = session.getMapper(CountryMapper.class);
        int i = countryMapper.update(country);
        System.out.println(i);
//        session.commit();

    }

    @After
    public void after() {
        System.out.println("after invoked");
        session.close();
    }

    @Test
    public void testQuery() {
        try {

            Country country = new Country();
            country.setId(1);
            country.setCountrycode("222");
            country.setCountryname("222");
            country.setAccountID(1001);

            CountryMapper mapper = session.getMapper(CountryMapper.class);
            List<Country> countries = mapper.query(country);
            System.out.println(countries);

//            Country country1 = new Country();
//            country1.setId(4);
//            country1.setCountrycode("111");
//            country1.setCountryname("222");
//            country1.setAccountID(1003);
//            countries = mapper.query(country1);
//            System.out.println(countries);
            session.commit();
        } finally {
            session.close();
        }
    }

    @Test
    public void testQueryMultiTables() {
        Country country = new Country();
        country.setId(1);
        country.setCountrycode("222");
        country.setCountryname("222");
        country.setAccountID(1003);

        CountryMapper mapper = session.getMapper(CountryMapper.class);
        List<Country> countries = mapper.queryMultiTables(country);
        System.out.println(countries);

    }
}
