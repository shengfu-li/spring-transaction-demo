package com.shengfuli.demo;

import com.shengfuli.demo.propogation.BookingNested;
import com.shengfuli.demo.service.BookingNestedService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Description:
 * @Author: lishengfu
 * @Date: 17:13 2019/08/25
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionPropagationNestedTest {
    Logger logger = LoggerFactory.getLogger(MultiThreadExceptionTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BookingNestedService bookingNestedServiceOne;

    @Autowired
    private BookingNestedService bookingNestedServiceTwo;

    @Autowired
    private BookingNested bookingNested;

    public List<String> findAllBookings() {
        return jdbcTemplate.query("select NAME from BOOKINGS",
                (rs, rowNum) -> rs.getString("NAME"));
    }

    /**
     * 外围方法没有开启事务,并且抛出RuntimeException, 事务传播行为：propagation_nested
     * propagation_nested表示如果封装事务存在，并且外围事务抛出异常回滚，那么内层事务必须回滚，反之，内层事务并不影响外围事务。如果封装事务不存在，则同PROPAGATION_REQUIRED的一样
     *
     * Q: bookingNestedServiceOne.addNested() 与 bookingNestedServiceTwo.addNested()没有抛出异常，外围方法抛出了异常，运行结果是什么？
     * A: “张三”、“李四”两条记录都会插入成功。因为外围方法没有开启事务，所以bookingNestedServiceOne.addNested()与bookingNestedServiceTwo.addNested() 会分别开启一个事务。
     *    外围方法抛出异常不会影响bookingNestedServiceOne.addNested()、bookingNestedServiceTwo.addNested()两者的独立事务
     */
    @Test
    public void  propagation_nested(){
        bookingNestedServiceOne.addNested("张三");
        bookingNestedServiceTwo.addNested("李四");
        throw new RuntimeException();
    }
    @Test
    public void invoke_propagation_nested(){
        try {
            propagation_nested();
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 2, "the number of record must be two.");
            logger.info(e.getMessage(),e);
        }
    }

    /**
     * 外围方法开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_nested
     *  propagation_nested表示如果封装事务存在，并且外围事务抛出异常回滚，那么内层事务必须回滚，反之，内层事务并不影响外围事务。如果封装事务不存在，则同PROPAGATION_REQUIRED的一样
     *
     * Q: bookingNestedServiceOne.addNested() 与 bookingNestedServiceTwo.addNested()没有抛出异常，外围方法抛出了异常，运行结果是什么？
     * A: “张三”、“李四”两条记录都会插入失败。外围方法抛出异常会导致bookingNestedServiceOne.addNested()、bookingNestedServiceTwo.addNested()都回滚
     */
    @Test
    public void invoke_insert_propagation_nested_exception(){
        try {
            bookingNested.insert_propagation_nested_exception("张三","李四");
        }catch (RuntimeException e){
            Assert.isTrue(findAllBookings().size() == 0, "the number of record must be zero.");
            logger.info(e.getMessage(),e);
        }
    }

    /**
     * 外围方法开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_nested
     *  propagation_nested表示如果封装事务存在，并且外围事务抛出异常回滚，那么内层事务必须回滚，反之，内层事务并不影响外围事务(在异常不被外围事务感知的情况下)。
     *  如果封装事务不存在，则同PROPAGATION_REQUIRED的一样。
     *
     * Q: 外围方法,bookingNestedServiceOne.addNested() 没有抛出了异常，而 bookingNestedServiceTwo.addNestedException()抛出了异常，运行结果是什么？
     * A: “张三”会插入失败，“李四”会插入成功。因为外围方法开启事务，但是外围事务没有抛出了异常。bookingNestedServiceTwo.addNestedException()抛出的异常被自己捕获后，
     *    只会导致它自己回滚，不会影响外围方法
     */
    @Test
    public void invoke_insert_propagation_nested_try() {
        bookingNested.insert_propagation_nested_try("张三", "李四");
        logger.info(findAllBookings().toString());
        Assert.isTrue(findAllBookings().size() == 1, "the number of record must be one.");
    }

    /**
     * 外围方法开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_nested
     *  propagation_nested表示如果封装事务存在，并且外围事务抛出异常回滚，那么内层事务必须回滚，反之，内层事务并不影响外围事务(在异常不被外围事务感知的情况下)。
     *  如果封装事务不存在，则同PROPAGATION_REQUIRED的一样。
     *
     * Q: 外围方法,bookingNestedServiceOne.addNested() 没有抛出了异常，而 bookingNestedServiceTwo.addNestedException()抛出了异常，运行结果是什么？
     * A: “张三”、“李四”都会插入失败。因为外围方法开启事务，但是外围事务没有抛出了异常。bookingNestedServiceTwo.addNestedException()抛出的异常被外围事务感知，导致整个外围事务以及内层事务回滚
     */
    @Test
    public void invoke_insert_propagation_nested() {
        try{
            bookingNested.insert_propagation_nested("张三", "李四");
        }catch (RuntimeException e){
            logger.info(e.getMessage());
        }finally {
            logger.info(findAllBookings().toString());
            Assert.isTrue(findAllBookings().size() == 0, "the number of record must be zero.");

        }
    }
}
