package com.shengfuli.demo;

import com.shengfuli.demo.domain.Foo;
import com.shengfuli.demo.service.DefaultFooService;
import com.shengfuli.demo.service.FooService;
import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

/**
 * @Description: 按照官方手册，从xml开始配置，更好地理解事务。
 * Spring Transaction link: https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/transaction.html
 * Spring AOP link: https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/aop.html#aop-proxying
 * @Author: lishengfu
 * @Date: 10:16 2019/08/21
 **/
public class TransactionAnalyzeTest {

    /**
     * Q：FooService由Spring Container托管，调用fooService.insertFoo(foo)时开启了事务吗？
     * A: 从日志中可以看出，开启了事务
     */
    @Test
    public void test_transaction_from_xml(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        FooService fooService = (FooService) ctx.getBean("fooService");
        Foo foo = new Foo("张三",18);
        fooService.insertFoo(foo);
    }

    /**
     *  Q:FooService由我们手动new构建，调用fooService.insertFoo(foo)时还会开启事务吗？
     *  A:从日志中可以看出，没有开启事务。why?因为没有对应的Proxy Class，
     */
    @Test
    public void test_transaction_by_new(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        DefaultFooService fooService = new DefaultFooService();
        JdbcTemplate jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbcTemplate");
        fooService.setJdbcTemplate(jdbcTemplate);
        Foo foo = new Foo("张三",18);
        fooService.insertFoo(foo);
    }


//    @Test
//    public void test(){
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//        Advice advice = (Advice) ctx.getBean("txAdvice");
//
//        ProxyFactory factory = new ProxyFactory(new DefaultFooService());
//        factory.addInterface(FooService.class);
//        factory.addAdvice(advice);
//        DefaultFooService fooService = (DefaultFooService) factory.getProxy();
//        JdbcTemplate jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbcTemplate");
//        fooService.setJdbcTemplate(jdbcTemplate);
//        Foo foo = new Foo("张三",18);
//        fooService.insertFoo(foo);
//
//    }
}
