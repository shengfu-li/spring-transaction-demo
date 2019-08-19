package com.shengfuli.demo.propogation;

import com.shengfuli.demo.service.Booking1Service;
import com.shengfuli.demo.service.Booking2Service;
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
public class BookingRequiresNew {

    @Autowired
    Booking2Service booking2ServiceOne;

    @Autowired
    Booking2Service booking2ServiceTwo;

    /**
     * 外围方法开启了事务,并且抛出RuntimeException, 事务传播行为：propagation_requires_new
     * propagation_requires_new表示被调用的方法会在自己的事务中运行，Spring会为该被调用的方法新建一个事务。如果存在一个现有的事务，那么在被调用方法执行期间，该事务会被挂起。
     *
     *Q: booking2ServiceOne.addRequiresNew() 与 booking2ServiceTwo.addRequiresNew()都没有抛出异常，外围方法抛出了异常，运行结果是什么？
     *A: “张三”、“李四”两条记录都会插入成功。虽然外围方法开启了事务，但是booking2ServiceOne.addRequiresNew()与booking2ServiceTwo.addRequiresNew() 都在自己独立的事务里。
     *   外围方法抛出异常不会导致booking2ServiceOne.addRequiresNew()、booking2ServiceTwo.addRequiresNew()两个方法回滚
     **/
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insert_propagation_requires_new(String oneName, String anotherName){
        booking2ServiceOne.addRequiresNew(oneName);
        booking2ServiceTwo.addRequiresNew(anotherName);
        throw new RuntimeException();
    }
}
