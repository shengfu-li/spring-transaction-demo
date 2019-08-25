package com.shengfuli.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 使用Propagation.REQUIRED 事务传播
 * @Author: lishengfu
 * @Date: 16:17 2019/08/19
 **/
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookingRequiredServiceImp implements BookingRequiredService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 插入一条记录，并且设置事务传播行为：Propagation.REQUIRED
     * @param name
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addRequired(String name) {
        jdbcTemplate.update("insert into BOOKINGS(NAME) values (?)", name);
    }

    /**
     * 插入一条记录，抛出RuntimeException,并且设置事务传播行为：Propagation.REQUIRED
     * @param name
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addRequiredException(String name) {
        jdbcTemplate.update("insert into BOOKINGS(NAME) values (?)", name);
        throw new RuntimeException();
    }
}
