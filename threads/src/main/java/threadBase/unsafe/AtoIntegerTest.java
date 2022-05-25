package threadBase.unsafe;

import leetcode.AutomaticTest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Zekun Fu
 * @date: 2022/5/25 20:36
 * @Description: 测试CAS操作
 */

public class AtoIntegerTest {

    AtomicInteger a = new AtomicInteger();

    public void test() {

        while(true) {  // 违背了空闲让进
            int pre = a.get();
            int nx = 0;
            a.compareAndSet(pre, nx);
            a.compareAndExchange(pre, nx);
        }
    }

    public static void main(String[] args) {
    }


}
