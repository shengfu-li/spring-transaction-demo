package com.shengfuli.demo.propogation;

import com.shengfuli.demo.service.BookingNestedService;
import com.shengfuli.demo.service.BookingRequiredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description:
 * @Author: lishengfu
 * @Date: 18:29 2019/08/25
 **/
@Service
public class BookingNested {
    @Autowired
    BookingNestedService bookingNestedServiceOne;

    @Autowired
    BookingNestedService bookingNestedServiceTwo;

    @Transactional(propagation = Propagation.NESTED)
    public void insert_propagation_nested(String oneName, String anotherName){
        bookingNestedServiceOne.addNested(oneName);
        bookingNestedServiceTwo.addNestedException(anotherName);
    }

    @Transactional(propagation = Propagation.NESTED)
    public void insert_propagation_nested_try(String oneName, String anotherName){
        bookingNestedServiceOne.addNested(oneName);
        try{
            bookingNestedServiceTwo.addNestedException(anotherName);
        }catch (RuntimeException e){
            System.out.println(e.getMessage());
        }
    }
    @Transactional(propagation = Propagation.NESTED)
    public void insert_propagation_nested_exception(String oneName, String anotherName){
        bookingNestedServiceOne.addNested(oneName);
        bookingNestedServiceTwo.addNested(anotherName);
        throw new RuntimeException();
    }
}
