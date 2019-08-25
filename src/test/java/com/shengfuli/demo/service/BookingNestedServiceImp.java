package com.shengfuli.demo.service;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author: lishengfu
 * @Date: 17:26 2019/08/25
 **/
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookingNestedServiceImp implements BookingNestedService {
    @Resource
    JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void addNested(String name) {
        jdbcTemplate.update("insert into BOOKINGS(NAME) values (?)", name);
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void addNestedException(String name) {
        jdbcTemplate.update("insert into BOOKINGS(NAME) values (?)", name);
        throw  new RuntimeException();
    }
}
