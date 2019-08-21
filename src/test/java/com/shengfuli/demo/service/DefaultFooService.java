package com.shengfuli.demo.service;

import com.shengfuli.demo.domain.Foo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jca.cci.connection.NotSupportedRecordFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @Description:
 * @Author: lishengfu
 * @Date: 10:04 2019/08/21
 **/
@Setter
@Getter
public class DefaultFooService implements FooService {

    private JdbcTemplate jdbcTemplate;

    @Override
    public Foo getFoo(String fooName) throws UnsupportedOperationException{
        return null;
    }

    @Override
    public Foo getFoo(String fooName, String barName) throws UnsupportedOperationException{
        List<Foo> foos= jdbcTemplate.query("select * from FOO",((rs, rowNum) ->{
            Foo foo = new Foo();
            foo.setName(rs.getString("NAME"));
            foo.setAge(rs.getInt("AGE"));
            return foo;
        }));
        return foos.get(0);
    }

    @Override
    public void insertFoo(Foo foo) throws UnsupportedOperationException{
        jdbcTemplate.update("insert into FOO(NAME,AGE) values (?,?)",foo.getName(),foo.getAge());
//        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFoo(Foo foo) throws UnsupportedOperationException{

    }
}
