package com.shengfuli.demo.propogation;

import com.shengfuli.demo.service.BookingRequiredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description:
 * @Author: lishengfu
 * @Date: 19:03 2019/08/19
 **/
@Service
public class BookingRequired {

    @Autowired
    BookingRequiredService bookingRequiredServiceOne;

    @Autowired
    BookingRequiredService bookingRequiredServiceTwo;

    /**
     * 外围方法开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_required
     * propagation_requires_new表示被调用的方法会在自己的事务中运行，Spring会为该被调用的方法新建一个事务。如果存在一个现有的事务，那么在被调用方法执行期间，该事务会被挂起。
     *
     *Q: booking1Service.addRequired() 与 booking2Service.addRequired()都没有抛出异常，外围方法抛出了异常，运行结果是什么？
     *A: “张三”、“李四”两条记录都会插入失败。因为外围方法开启了事务，所以booking1Service.addRequired()与booking2Service.addRequired() 会都在外围方法的事务里。
     *   外围方法抛出异常会导致booking1Service.addRequired()、booking2Service.addRequired()两个方法回滚
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    public void insert_propagation_required(String oneName, String anotherName){
        bookingRequiredServiceOne.addRequired(oneName);
        bookingRequiredServiceTwo.addRequiredException(anotherName);
        throw new RuntimeException();
    }
}
