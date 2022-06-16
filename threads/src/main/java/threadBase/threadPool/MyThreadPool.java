package threadBase.threadPool;

import java.io.File;
import java.io.FileDescriptor;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Zekun Fu
 * @date: 2022/6/10 15:09
 * @Description: 手动实现线程池
 *
 * 1.
 */
public class MyThreadPool<T> {


    // 1. 任务队列
    private Deque<T> queue = new ArrayDeque<>();

    // 2. 锁
    private ReentrantLock lock = new ReentrantLock();

    // 3. 消费者条件变量
    private Condition full = lock.newCondition();

    // 4. 生产者条件变量
    private Condition empty = lock.newCondition();

    // 5.容量
    private int capcity;

    // 阻塞获取
    public T take() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                empty.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        T ret = queue.removeFirst();
        full.signalAll();
        return ret;
    }

    // 阻塞添加
    public void put(T element) {
        lock.lock();
        try {
            while (queue.size() >= capcity) {
                full.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

        }
        queue.push(element);
        empty.signalAll();
    }
    // 获取大小
    public int size() {
        return this.capcity;
    }
}
