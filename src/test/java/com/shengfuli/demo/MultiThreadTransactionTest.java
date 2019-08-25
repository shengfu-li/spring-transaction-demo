package com.shengfuli.demo;

import com.shengfuli.demo.propogation.BookingRequired;
import com.shengfuli.demo.propogation.BookingRequiresNew;
import com.shengfuli.demo.service.BookingRequiredService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 测试Spring事务在多线程环境下的表现
 * @Author: lishengfu
 * @Date: 21:10 2019/08/19
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiThreadTransactionTest {
    private Logger logger = LoggerFactory.getLogger(MultiThreadTransactionTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ThreadPoolExecutor executor;

    @Autowired
    private BookingRequiredService bookingRequiredServiceOne;

    @Autowired
    private BookingRequiredService bookingRequiredServiceTwo;

    @Autowired
    private BookingRequired bookingRequired;

    @Autowired
    private BookingRequiresNew bookingRequiresNew;

    @Before
    public void prepareData() {
        executor = new ThreadPoolExecutor(1, 3, 30, TimeUnit.SECONDS, new LinkedBlockingDeque(), new ThreadPoolExecutor.AbortPolicy());
    }

    public List<String> findAllBookings() {
        return jdbcTemplate.query("select NAME from BOOKINGS",
                (rs, rowNum) -> rs.getString("NAME"));
    }


    /**
     * 外围方法没有开启事务,并且抛出RuntimeException, 事务传播行为：propagation_required
     * propagation_required表示被调用的方法必须在一个具有事务的上下文中运行，如果调用者有事务在进行，那么被调用的方法将在该事务中运行，否则的话重新开启一个事务。
     * <p>
     * Q: bookingRequiredServiceOne.addRequired() 与 bookingRequiredServiceTwo.addRequired()没有抛出异常，外围方法抛出了异常，运行结果是什么？
     * A: “张三”、“李四”两条记录都会插入成功。因为外围run()方法没有开启事务，所以booking1ServiceOne.addRequired()与booking1ServiceTwo.addRequired() 会分别开启一个事务。
     * 外围方法抛出异常不会影响booking1ServiceOne.addRequired()、bookingRequiredServiceTwo.addRequired()两者的独立事务
     *
     * @throws InterruptedException
     */
    @Test
    public void propagation_required_runnable() throws InterruptedException {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                bookingRequiredServiceOne.addRequired("张三");
                bookingRequiredServiceTwo.addRequired("李四");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException();
            }
        });
        Thread.sleep(3000);
    }
    @Test
    public void invoke_propagation_required_runnable() {
        try {
            propagation_required_runnable();
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
        } finally {
            logger.info(findAllBookings().toString());
            Assert.isTrue(findAllBookings().size() == 2, "the number of record must be two.");
        }
    }

    /**
     * 外围方法没有开启事务,并且booking1ServiceTwo.addRequiredException()抛出RuntimeException, 事务传播行为：propagation_required
     * propagation_required表示被调用的方法必须在一个具有事务的上下文中运行，如果调用者有事务在进行，那么被调用的方法将在该事务中运行，否则的话重新开启一个事务。
     * <p>
     * Q: bookingRequiredServiceOne.addRequired() 没有抛出异常，bookingRequiredServiceTwo.addRequiredException()抛出了异常，运行结果是什么？
     * A: “张三”都会插入成功，“李四”会插入失败。因为外围run()方法没有开启事务，所以booking1ServiceOne.addRequired()与booking1ServiceTwo.addRequired() 会分别开启一个事务。
     * bookingRequiredServiceTwo.addRequiredException()抛出异常不会影响booking1ServiceOne.addRequired()的独立事务
     *
     * @throws InterruptedException
     */
    @Test
    public void propagation_required_exception_runnable() throws InterruptedException {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                bookingRequiredServiceOne.addRequired("张三");
                bookingRequiredServiceTwo.addRequiredException("李四");
            }
        });
        Thread.sleep(2000);
    }
    @Test
    public void invoke_propagation_required_exception_runnable() {
        try {
            propagation_required_exception_runnable();
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
        } finally {
            logger.info(findAllBookings().toString());
            Assert.isTrue(findAllBookings().size() == 1, "the number of record must be one.");
        }
    }

    /**
     * 外围方法insert_propagation_required()开启了事务,并且booking1ServiceTwo.addRequiredException()抛出RuntimeException, 事务传播行为：propagation_required
     * propagation_required表示被调用的方法必须在一个具有事务的上下文中运行，如果调用者有事务在进行，那么被调用的方法将在该事务中运行，否则的话重新开启一个事务。
     *
     * Q: bookingRequiredServiceOne.addRequired() 没有抛出异常，bookingRequiredServiceTwo.addRequiredException()抛出了异常，运行结果是什么？
     * A: “张三”、“李四”都会插入失败。因为外围insert_propagation_required()方法开启了事务，所以booking1ServiceOne.addRequired()与booking1ServiceTwo.addRequired() 会共用同一个事务。
     * bookingRequiredServiceTwo.addRequiredException()抛出的异常会导致booking1ServiceOne.addRequired()、以及insert_propagation_required()方法的回滚
     *
     * @throws InterruptedException
     */

//    //直接在run()方法前面加的@Transactional未生效，why?
//    public void propagation_required_exception_runnable_transaction() throws InterruptedException{
//        executor.execute(new Runnable() {
//            @Transactional
//            @Override
//            public void run() {
//                bookingRequiredServiceOne.addRequired("张三");
//                bookingRequiredServiceTwo.addRequiredException("李四");
//                throw new RuntimeException();
//            }
//        });
//        Thread.sleep(2000);
//    }
//    @Test
//    public void invoke_propagation_required_exception_runnable_transaction(){
//        try{
//            propagation_required_exception_runnable_transaction();
//        }catch (InterruptedException e){
//            logger.warn(e.getMessage());
//        }finally {
//            logger.info(findAllBookings().toString());
//            Assert.isTrue(findAllBookings().size() == 0,"the number of record must be zero.");
//        }
//    }
    @Test
    public void invoke_insert_propagation_required() throws InterruptedException{
        executor.execute(new Runnable() {
            @Override
            public void run() {
                bookingRequired.insert_propagation_required("张三","李四");
            }
        });
        Thread.sleep(2000);
        logger.info(findAllBookings().toString());
        Assert.isTrue(findAllBookings().size() == 0,"the number of record must be zero.");
    }

    /**
     * 外围方法insert_propagation_requires_new()开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_requires_new
     * propagation_requires_new表示被调用的方法会在自己的事务中运行，Spring会为该被调用的方法新建一个事务。如果存在一个现有的事务，那么在被调用方法执行期间，该事务会被挂起。
     *
     * Q: bookingRequiresNewServiceOne.addRequiresNew() ，bookingRequiresNewServiceTwo.addRequiresNewException()都没有抛出异常，外围方法抛出了RuntimeException，运行结果是什么？
     * A: “张三”、“李四”都会插入成功。虽然外围insert_propagation_requires_new()方法开启了事务，但是booking1ServiceOne.addRequired()与booking1ServiceTwo.addRequired()都开启了各自的事务。
     *    insert_propagation_requires_new()抛出的异常不会对booking2ServiceOne.addRequiresNew()、以及booking2ServiceTwo.addRequiresNewException()方法产生影响
     * @throws InterruptedException
     */
    @Test
    public void invoke_insert_propagation_requires_new() throws InterruptedException{
        executor.execute(new Runnable() {
            @Override
            public void run() {
                bookingRequiresNew.insert_propagation_requires_new("张三","李四");
            }
        });
        Thread.sleep(2000);
        logger.info(findAllBookings().toString());
        Assert.isTrue(findAllBookings().size() == 2,"the number of record must be two.");
    }

}
