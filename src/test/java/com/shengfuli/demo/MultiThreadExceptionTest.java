package com.shengfuli.demo;

import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Description: 该类用来测试在多线环境下能否捕捉异常
 * @Author: lishengfu
 * @Date: 14:59 2019/08/19
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiThreadExceptionTest {
    private Logger logger = LoggerFactory.getLogger(MultiThreadExceptionTest.class);

    private ThreadPoolExecutor executor;

    @Before
    public void prepareThreadPool() {
        executor = new ThreadPoolExecutor(1, 3, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new ThreadPoolExecutor.AbortPolicy());
    }


    /**
     * Q: 通过实现Runnable接口的线程抛出的异常能否被捕捉？
     * A: 不能
     */
    @Test
    public void runnable_throw_Exception() {
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    //抛出 RuntimeException 异常
                    throw new RuntimeException();
                }
            });
        }catch (Exception e){
            logger.error("try to catch exception in thread pool.",e);
        }
    }


    /**
     * Q：通过实现Callable接口的线程抛出的异常能否被捕捉?
     * A：能
     */
    @Test
    public void callable_throw_Exception() {
        Future<String> future = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                //抛出 RuntimeException 异常
               throw new RuntimeException();
            }
        });
        try {
            String result = future.get();
        }catch (Exception e){
            logger.error("try to catch exception in thread pool.",e);
        }
    }
}
