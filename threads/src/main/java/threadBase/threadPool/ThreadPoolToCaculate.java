package threadBase.threadPool;

/*
*
*
*   使用线程池去计算数组的和
* */


import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolToCaculate {
    private static final int MAXN = 100000000;
    private static int[] nums = new int[MAXN];     // 计算数组中100个数字的和
    private AtomicInteger cur = new AtomicInteger();
    static {
        Arrays.fill(nums, 1);
    }



}
