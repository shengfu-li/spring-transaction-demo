package com.shengfuli.demo.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description:
 * @Author: lishengfu
 * @Date: 10:03 2019/08/21
 **/
@Getter
@Setter
public class Foo {
    String name;
    Integer age;

    public Foo(){
        name ="";
        age = 0;
    }
    public Foo(String name, Integer age){
        this.name = name;
        this.age = age;
    }
}
