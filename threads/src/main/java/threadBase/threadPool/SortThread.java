package threadBase.threadPool;

import javafx.util.Pair;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Zekun Fu
 * @date: 2022/5/21 14:41
 * @Description: 分治排序的主线程
 *
 * 对于自动排序: 需要指定线程数量和每个线程处理数组的大小
 * 对于自动排序的2：需要执行线程数量和.. 以及分配一个和原数组一样大的tmp数组。
 *
 */
public class SortThread {


    private ExecutorService pool;           // 线程池

    public SortThread() {
        pool = Executors.newCachedThreadPool();        // 因为8核心就只有16个线程
    }

    private int[][] div(int[] arr, int d) {  // 数组划分成多少份
        int [][] ans = new int[d][];
        int n = arr.length;
        int len = n / d;
        for (int i = 0; i < d; i++) {
            int tmp[];
            if(i != d - 1) tmp = new int[len];
            else tmp = new int[n - (d - 1) * len];
            for (int j = i * len; j < (i + 1) * len; j++) {
                tmp[j - (i * len)] = arr[j];
            }
            ans[i] = tmp;
        }
        for (int i = d * len, j = len; i < n; i++, j++) {
            ans[d - 1][j] = arr[i];
        }
        return ans;
    }

    /*
    *
    * 不进行复制粘贴，直接使用原数组进行排序
    * */
    int[] tmp;
    public void sortByAuto2(int[] arr, int l, int r, int len) {
        if (r - l + 1 <= len) {
            Arrays.sort(arr, l, r + 1);
            return;
        }
        int mid = (l + r) >> 1;
        CountDownLatch latch = new CountDownLatch(2);
        new Thread(()->{
            sortByAuto2(arr, l, mid, len);
            latch.countDown();
        }).start();
        new Thread(()->{
            sortByAuto2(arr, mid + 1, r, len);
            latch.countDown();
        }).start();

        try{
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = l, j = l, k = mid + 1;
        while(j <= mid && k <= r) {
            tmp[i++] = arr[j] <= arr[k] ? arr[j++] : arr[k++];
        }
        while(j <= mid) {
            tmp[i++] = arr[j++];
        }
        while(k <= r) {
            tmp[i++] = arr[k++];
        }
        for (i = l; i <= r; i++) arr[i] = tmp[i];
    }

    public int[] sortByAuto(int[] arr, int l, int r, int len) throws Exception{
        if (r - l + 1 <= len) {
            int []tmp = Arrays.copyOfRange(arr, l, r + 1);
            Arrays.sort(tmp);
            return tmp;
        }
        int mid = (l + r) >> 1;

        FutureTask<int[]> task1 = new FutureTask<int[]>(()->{
            int[] ans = sortByAuto(arr, l, mid, len);
            return ans;
        });
        FutureTask<int[]>task2 = new FutureTask<int[]>(()->{
            int[] ans = sortByAuto(arr, mid + 1, r, len);
            return ans;
        });
        new Thread(task1).start();
        new Thread(task2).start();

        // 由于是阻塞式的，所以再返回之前一定是已经完成了这两组的排序了
        int[] tmpl = task1.get();
        int[] tmpr = task2.get();
        int[] ans = new int[r - l + 1];
        int i = 0, j = 0, k = 0;
        while(i < tmpl.length && j < tmpr.length) {
            ans[k++] = tmpl[i] <= tmpr[j] ? tmpl[i++] : tmpr[j++];
        }
        while(i < tmpl.length) {
            ans[k++] = tmpl[i++];
        }
        while(j < tmpr.length) {
            ans[k++] = tmpr[j++];
        }
        return ans;
    }

        /*
    *
    *   直接采用分治的方式进行
    * 是一个错误的方法不知道怎么实现同步
    * */
    @Deprecated                 // 错误方法，别用了
    public int[] sortByAuto3(int[] arr, int l, int r, int len) {


        // 1. 递归基
        int[] tmp = new int[r - l + 1];
        for (int i = l, j = 0; i <= r; i++, j++) {
            tmp[j] = arr[i];
        }
        if (r - l + 1 <= len) {
            new Thread(()->{
                Arrays.sort(tmp);
            }).start();
            return tmp;
        }
        // 2.进行左右划分
        int mid = (l + r) >> 1;
        int[] tmpl = sortByAuto3(arr, l, mid, len);
        int[] tmpr = sortByAuto3(arr, mid + 1, r, len);

        // 3. 进行合并, 必须等待所有的子线程排序完成。
        int[] merge = new int[r - l + 1];
        int i = l, j = 0, k = 0;
        while(j < tmpl.length && k < tmpr.length) {
            merge[i - l] = tmpl[j] <= tmpr[k] ? tmpl[j++] : tmpr[k++];
            i++;
        }
        while(j < tmpl.length) {
            merge[i - l] = tmpl[j++];
            i++;
        }
        while(k < tmpr.length) {
            merge[i - l] = tmpr[k++];
            i++;
        }
        return merge;
    }

    public int[] collect(int[][]sortedArr, int size) {

        class Node implements Comparable<Node>{
            int num;
            int ground;             // 第几组
            int id;                 // 第几个

            public Node(int num, int group, int id) {
                this.num = num;
                this.ground = group;
                this.id = id;
            }

            @Override
            public int compareTo(Node o) {
                return Integer.compare(this.num, o.num);
            }
        }

        int[] ans = new int[size];
        Queue<Node> heap = new PriorityQueue<>();
        int d = sortedArr.length;
        // 1.建立初始堆
        for (int i = 0; i < d; i++) {
            Node p = new Node(sortedArr[i][0], i, 0);
            heap.add(p);
        }
        // 2.取顶，放入答案，重新建堆。
        int idx = 0;
        while(!heap.isEmpty()) {
            Node top = heap.poll();
            ans[idx ++] = top.num;
            int g = top.ground, id = top.id;
            if (id  + 1 < sortedArr[g].length) {     // 看是否为空
                heap.add(new Node(sortedArr[g][id + 1], g, id + 1));
            }
        }
        return ans;
    }

    /*
    *
    *   使用线程池进行数组的排序
    * */
    public void threadSortByPool(int[] arr, int l, int r) {

    }
    /*
    *
    *   使用手动线程进行数组的排序
    * */

    public int[] threadSortByFutT(int[] arr) {
        int n = 8;      // 线程数量, 小于等于数组的长度。
        int[][] divArr = div(arr, n);


        AtomicInteger id = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            new Thread(()->{
                Arrays.sort(divArr[id.getAndIncrement()]);  // 给数组进行排序
                latch.countDown();
            }).start();
        }
        // 这里必须等所有数组排好序才能继续执行
        try {
            latch.await();
        }catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("拍好序之后的数组");
//        SortUtils.printArr(divArr);

        int[] ans = collect(divArr, arr.length);            // 得到一个新的数组
        return ans;
    }

    public int[] sortBySingle(int[] arr) {
        Arrays.sort(arr);
        return arr;
    }

    // 进行辅助函数的测试
    public static void test() {
        int n = 16, m = 100;
        int[] arr = SortUtils.getArr(16, 100);
        SortThread s = new SortThread();
        SortUtils.printArr(arr);
        int[][] divArr = s.div(arr, 3);
        SortUtils.printArr(divArr);
        int tn = 4;                      // 开启线程的个数
        int len = n / tn ;               // 每个线程处理的数组的大小
        try{
            int[] ans = s.sortByAuto(arr, 0, n - 1, len);
            SortUtils.printArr(ans);
        } catch (Exception e) {
            e.printStackTrace();
        }

        s.tmp = new int[n];
        s.sortByAuto3(arr, 0, n - 1, len);
        SortUtils.printArr(arr);
    }
    public static void main(String[] args) {
//        test();

        int n = (int)1e8, m = (int)100;
        int[] arr1 = SortUtils.getArr(n, m);       // 生成数组
        int[] arr2 = SortUtils.getArr(n, m);

        SortThread s = new SortThread();
        SortThread st = (SortThread) new TimeProxy().getProxy(SortThread.class);
        int[] ans1 = st.threadSortByFutT(arr1);
        int [] ans2 = st.sortBySingle(arr2);
        int tn = 8;                      // 开启线程的个数
        int len = n / tn ;               // 每个线程处理的数组的大小
        try{
            long startTime = System.currentTimeMillis();
            int[] ans = s.sortByAuto(arr1, 0, n - 1, len);
            long endTime = System.currentTimeMillis();
            System.out.println("sortByAuto运行时间为: " + (endTime - startTime) + "ms");
//            SortUtils.printArr(ans);
        } catch (Exception e) {
            e.printStackTrace();
        }
        s.tmp = new int[n];
        long startTime = System.currentTimeMillis();
        s.sortByAuto2(arr1, 0, n - 1, len);
        long endTime = System.currentTimeMillis();
        System.out.println("sortByAuto2运行时间:" + (endTime - startTime) + "ms");
//        SortUtils.printArr(arr1);

    }
}
