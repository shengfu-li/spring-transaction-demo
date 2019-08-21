package com.shengfuli.demo.service;

import com.shengfuli.demo.domain.Foo;

/**
 * @Description:
 * @Author: lishengfu
 * @Date: 09:46 2019/08/21
 **/
public interface FooService {
    Foo getFoo(String fooName);

    Foo getFoo(String fooName, String barName);

    void insertFoo(Foo foo);

    void updateFoo(Foo foo);
}
