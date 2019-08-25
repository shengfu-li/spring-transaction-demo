package com.shengfuli.demo;

import com.shengfuli.demo.propogation.BookingRequired;
import com.shengfuli.demo.service.BookingRequiredService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Description: 测试Spring Propagation.Required事务传播在单线程下的行为
 * @Author: lishengfu
 * @Date: 15:24 2019/08/19
 **/
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionPropagationRequiredTest {
    private Logger logger = LoggerFactory.getLogger(MultiThreadExceptionTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void prepareDataSource(){
    }

    @Autowired
    BookingRequiredService bookingRequiredServiceOne;

    @Autowired
    BookingRequiredService bookingRequiredServiceTwo;

    @Autowired
    BookingRequired bookingRequired;

    public List<String> findAllBookings() {
        return jdbcTemplate.query("select NAME from BOOKINGS",
                (rs, rowNum) -> rs.getString("NAME"));
    }

    /**
     * 外围方法没有开启事务,并且抛出RuntimeException, 事务传播行为：propagation_required
     * propagation_required表示被调用的方法必须在一个具有事务的上下文中运行，如果调用者有事务在进行，那么被调用的方法将在该事务中运行，否则的话重新开启一个事务。
     *
     * Q: bookingRequiredServiceOne.addRequired() 与 bookingRequiredServiceTwo.addRequired()没有抛出异常，外围方法抛出了异常，运行结果是什么？
     * A: “张三”、“李四”两条记录都会插入成功。因为外围方法没有开启事务，所以booking1ServiceOne.addRequired()与booking1ServiceTwo.addRequired() 会分别开启一个事务。
     *    外围方法抛出异常不会影响booking1ServiceOne.addRequired()、bookingRequiredServiceTwo.addRequired()两者的独立事务
     */
    @Test
    public void  propagation_required(){
        bookingRequiredServiceOne.addRequired("张三");
        bookingRequiredServiceTwo.addRequired("李四");
        throw new RuntimeException();
    }
    @Test
    public void invoke_propagation_required(){
        try {
            propagation_required();
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 2, "the number of record must be two.");
            logger.info(e.getMessage(),e);
        }
    }


    /**
     * 外围方法没有开启事务,并且 bookingRequiredServiceTwo.addRequiredException方法抛出RuntimeException, 事务传播行为：propagation_required
     * propagation_required表示被调用的方法必须在一个具有事务的上下文中运行，如果调用者有事务在进行，那么被调用的方法将在该事务中运行，否则的话重新开启一个事务。
     *
     * Q: bookingRequiredServiceTwo.addRequiredException()抛出异常了，运行结果是什么？
     * A: “张三”会插入成功、“李四”会插入失败。因为外围方法没有开启事务，所以booking1ServiceOne.addRequired()与booking1ServiceTwo.addRequired() 会分别开启一个事务。
     *    bookingRequiredServiceTwo.addRequiredException()方法抛出异常,不会影响booking1ServiceOne.addRequired()的独立事务
     */
    @Test
    public void  propagation_required_exception(){
        bookingRequiredServiceOne.addRequired("张三");
        bookingRequiredServiceTwo.addRequiredException("李四");
    }
    @Test
    public void invoke_propagation_required_exception(){
        try {
            propagation_required_exception();
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 1, "the number of record must be one.");
            logger.info(e.getMessage(),e);
        }
    }


    /**
     *
     * 外围方法开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_required
     * propagation_required表示被调用的方法必须在一个具有事务的上下文中运行，如果调用者有事务在进行，那么被调用的方法将在该事务中运行，否则的话重新开启一个事务。
     * （如果被调用端发生异常，那么调用端和被调用端事务都将回滚）
     *
     *Q: bookingRequiredServiceOne.addRequired() 与 bookingRequiredServiceTwo.addRequired()都没有抛出异常，外围方法抛出了异常，运行结果是什么？
     *A: “张三”、“李四”两条记录都会插入失败。因为外围方法开启了事务，所以booking1ServiceOne.addRequired()与booking1ServiceTwo.addRequired() 会都在外围方法的事务里。
     *   外围方法抛出异常会导致booking1ServiceOne.addRequired()、bookingRequiredServiceTwo.addRequired()两个方法回滚
     *
     */

    /**
     * 为什么下面invoke_propagation_required_with_transaction方法的结果和我们预期的不一致？与invoke_insert_propagation_required有什么不同之处？
     * 提示：TransactionPropagation类中调用内部的@Transactional注解方法，能生效吗？为什么？
     */
//    @Test
//    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = RuntimeException.class)
//    public void  propagation_required_with_transaction(){
//        bookingRequiredServiceOne.addRequired("张三");
//        bookingRequiredServiceTwo.addRequiredException("李四");
//        throw new RuntimeException();
//    }
//    @Test
//    public void invoke_propagation_required_with_transaction(){
//        try{
//            propagation_required_with_transaction();
//        }catch (RuntimeException e){
//            Assert.isTrue(findAllBookings().size() == 0, "the number of record must be zero.");
//            logger.info(e.getMessage(),e);
//        }
//    }
    @Test
    public void invoke_insert_propagation_required(){
        String oneName = "张三";
        String anotherName = "李四";
        try{
            bookingRequired.insert_propagation_required(oneName,anotherName);
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 0,"the number of record must be zero.");
            logger.info(findAllBookings().toString());
        }
    }
}
