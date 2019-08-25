package com.shengfuli.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 使用Propagation.REQUIRES_NEW 事务传播
 * @Author: lishengfu
 * @Date: 16:29 2019/08/19
 **/
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookingRequiresNewServiceImp implements BookingRequiresNewService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void addRequiresNew(String name) {
        jdbcTemplate.update("insert into BOOKINGS(NAME) values (?)", name);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void addRequiresNewException(String name) {
        jdbcTemplate.update("insert into BOOKINGS(NAME) values (?)", name);
        throw new RuntimeException();
    }
}
