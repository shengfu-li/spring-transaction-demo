package com.shengfuli.demo;

import com.shengfuli.demo.propogation.BookingRequired;
import com.shengfuli.demo.propogation.BookingRequiresNew;
import com.shengfuli.demo.service.Booking2Service;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Description: 测试Spring Propagation.Requires_New事务传播在单线程下的行为
 * @Author: lishengfu
 * @Date: 15:24 2019/08/19
 **/
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionPropagationRequiresNewTest {
    Logger logger = LoggerFactory.getLogger(MultiThreadExceptionTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void prepareDataSource(){
    }

    @Autowired
    Booking2Service booking2ServiceOne;

    @Autowired
    Booking2Service booking2ServiceTwo;

    @Autowired
    BookingRequiresNew bookingRequiresNew;

    public List<String> findAllBookings() {
        return jdbcTemplate.query("select NAME from BOOKINGS",
                (rs, rowNum) -> rs.getString("NAME"));
    }

    /**
     * 外围方法没有开启事务,并且抛出RuntimeException, 事务传播行为：propagation_requires_new
     * propagation_requires_new表示被调用的方法会在自己的事务中运行，Spring会为该被调用的方法新建一个事务。如果存在一个现有的事务，那么在被调用方法执行期间，该事务会被挂起。
     *
     * Q: booking2ServiceOne.addRequiresNew() 与 booking2ServiceTwo.addRequiresNew()没有抛出异常，外围方法抛出了异常，运行结果是什么？
     * A: “张三”、“李四”两条记录都会插入成功。因为外围方法没有开启事务，所以booking2ServiceOne.addRequiresNew()与booking2ServiceTwo.addRequiresNew() 会分别开启一个事务。
     *    外围方法抛出异常不会影响booking2ServiceOne.addRequiresNew()、booking2ServiceTwo.addRequiresNew()两者的独立事务
     */
    @Test
    public void  propagation_requires_new(){
        booking2ServiceOne.addRequiresNew("张三");
        booking2ServiceTwo.addRequiresNew("李四");
        throw new RuntimeException();
    }
    @Test
    public void invoke_propagation_requires_new(){
        try {
            propagation_requires_new();
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 2, "the number of record must be two.");
            logger.info(e.getMessage(),e);
        }
    }


    /**
     * 外围方法没有开启事务,并且 booking2Service.addRequiresNewException方法抛出RuntimeException, 事务传播行为：propagation_requires_new
     * propagation_requires_new表示被调用的方法会在自己的事务中运行，Spring会为该被调用的方法新建一个事务。如果存在一个现有的事务，那么在被调用方法执行期间，该事务会被挂起。
     *
     * Q: booking2ServiceTwo.addRequiresNewException()抛出异常了，运行结果是什么？
     * A: “张三”会插入成功、“李四”会插入失败。因为外围方法没有开启事务，所以booking2ServiceOne.addRequiresNew()与booking2ServiceTwo.addRequiresNew() 会分别开启一个事务。
     *    booking2Service.addRequiresNewException()方法抛出异常,不会影响booking2ServiceOne.addRequiresNew()的独立事务
     */
    @Test
    public void  propagation_requires_new_exception(){
        booking2ServiceOne.addRequiresNew("张三");
        booking2ServiceTwo.addRequiresNewException("李四");
    }
    @Test
    public void invoke_propagation_requires_new_exception(){
        try {
            propagation_requires_new_exception();
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 1, "the number of record must be one.");
            logger.info(e.getMessage(),e);
        }
    }


    /**
     *
     * 外围方法开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_requires_new
     * propagation_requires_new表示被调用的方法会在自己的事务中运行，Spring会为该被调用的方法新建一个事务。如果存在一个现有的事务，那么在被调用方法执行期间，该事务会被挂起。
     *
     *Q: booking2ServiceOne.addRequiresNew() 与 booking2ServiceTwo.addRequiresNew()都没有抛出异常，外围方法抛出了异常，运行结果是什么？
     *A: “张三”、“李四”两条记录都会插入成功。虽然外围方法开启了事务，但是booking2ServiceOne.addRequiresNew()与booking2ServiceTwo.addRequiresNew() 都在自己独立的事务里。
     *   外围方法抛出异常不会导致booking2ServiceOne.addRequiresNew()、booking2ServiceTwo.addRequiresNew()两个方法回滚
     *
     */
    @Test
    public void invoke_insert_propagation_requires_new(){
        String oneName = "张三";
        String anotherName = "李四";
        try{
            bookingRequiresNew.insert_propagation_requires_new(oneName,anotherName);
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 2,"the number of record must be two.");
            logger.info(e.getMessage(),e);
        }
    }
}
