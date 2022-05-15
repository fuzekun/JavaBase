package leetcode;

import java.util.concurrent.Semaphore;
import java.util.function.IntConsumer;

/*
*
*   这是一个错误的实例
* */
public class FizzBuzz6 extends FizzBuzz {
    private Semaphore semaphore = new Semaphore(1);
    private int state = 0;
    private int n;
    FizzBuzz6(int n) {
        super(n);
        this.n = n;
    }
    // printFizz.run() outputs "fizz".
    public void fizz(Runnable printFizz) throws InterruptedException {
        for (int i = 3; i <= n; i += 3) {
            semaphore.acquire(1);
            try {
                if (state != 3) continue;           // 直接释放锁
                if (i % 3 == 0 && i % 5 != 0) {
                    printFizz.run();
                    state = 0;
                }
            } finally {
                semaphore.release(1);
            }
        }
    }

    // printBuzz.run() outputs "buzz".
    public void buzz(Runnable printBuzz) throws InterruptedException {
        for (int i = 5; i <= n; i++){
            semaphore.acquire(1);
            if (state != 5) continue;
            try {
                if (i % 3 != 0) {
                    printBuzz.run();
                    state = 0;
                }
            } finally {
                semaphore.release(1);
            }
        }
    }

    // printFizzBuzz.run() outputs "fizzbuzz".
    public void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
        for (int i = 15; i <= n; i += 15) {
            semaphore.acquire(1);
            try {
                if (state != 15) continue;
                printFizzBuzz.run();
                state = 0;
            } finally {
                semaphore.release(1);
            }
        }
    }

    // printNumber.accept(x) outputs "x", where x is an integer.
    public void number(IntConsumer printNumber) throws InterruptedException {
        for (int i = 1; i <= n; i++)  {
            semaphore.acquire(1);
            try {
                if (i % 3 != 0 && i % 5 != 0) {
                    System.out.println(i);
                    printNumber.accept(i);
                } else  {
                    if (i % 3 == 0 && i % 5 == 0)
                        state = 15;
                    else if (i % 3 == 0)
                        state = 3;
                    else state = 5;
                }
            } finally {
                semaphore.release(1);
            }
        }
    }

    public static void main(String[] args)throws Exception {
        String[]s = {"leetcode.FizzBuzz6", "20"} ;
        FuzzBuzzMain.main(s);
    }
}
