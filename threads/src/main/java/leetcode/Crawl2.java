package leetcode;

import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class Crawl2 {   // 已知URL集合，存储当前可见的所有URL。

    private class HtmlParser {
        public List<String> getUrls(String s) {
            List<String>ans = new ArrayList<>();
            return ans;
        }
    }

    private ConcurrentHashMap<String, Boolean> totalUrls = new ConcurrentHashMap<>();

    // 结果URL链表及对应锁。
    private ReentrantLock resultLock = new ReentrantLock();
    private LinkedList<String> resultUrls = new LinkedList<>();

    // 待抓取URL链表及对应锁。
    private ReentrantLock crawlLock = new ReentrantLock();
    private LinkedList<String> urlsToCrawl = new LinkedList<>();

    // 当前正在执行的工作线程个数。
    private AtomicInteger choreCount = new AtomicInteger(0);

    public List<String> crawl(String startUrl, HtmlParser htmlParser) {
        String hostName = extractHostName(startUrl); // 1. 获取主机名

        this.totalUrls.put(startUrl, true);         //

        addUrlToResult(startUrl);
        addUrlToCrawl(startUrl);

        while (true) {
            String urlToCrawl = fetchUrlToCrawl();
            if (urlToCrawl != null) {
                incrChore();
                Chore chore = new Chore(this, hostName, htmlParser, urlToCrawl);
                (new Thread(chore)).start();
            } else {
                if (this.choreCount.get() == 0) {
                    break;
                }
                LockSupport.parkNanos(1L);
            }
        }

        return fetchResultUrls();
    }

    private String extractHostName(String url) {
        // HTTP protocol only.
        String processedUrl = url.substring(7);

        int index = processedUrl.indexOf("/");
        if (index == -1) {
            return processedUrl;
        } else {
            return processedUrl.substring(0, index);
        }
    }

    private class Chore implements Runnable {
        private Crawl2 solution;
        private String hostName;
        private HtmlParser htmlParser;
        private String urlToCrawl;

        public Chore(Crawl2 solution, String hostName, HtmlParser htmlParser, String urlToCrawl) {
            this.solution = solution;
            this.hostName = hostName;
            this.htmlParser = htmlParser;
            this.urlToCrawl = urlToCrawl;
        }

        @Override
        public void run() {
            try {
                filterUrls(this.htmlParser.getUrls(urlToCrawl));
            } finally {
                this.solution.decrChore();      // 跑完了，线程减少。
            }
        }

        private void filterUrls(List<String> crawledUrls) {
            if (crawledUrls == null || crawledUrls.isEmpty()) {
                return;
            }

            for (String url : crawledUrls) {
                // 如果该URL在已知的URL集合中已存在，那么不需要再重复抓取。
                if (this.solution.totalUrls.containsKey(url)) {
                    continue;
                }

                this.solution.totalUrls.put(url, true);

                String crawlHostName = this.solution.extractHostName(url);
                if (!crawlHostName.equals(this.hostName)) {
                    // 如果抓取的URL对应的HostName同Start URL对应的HostName不同，那么直接丢弃该URL。
                    continue;
                }

                // 将该URL添加至结果链表。
                this.solution.addUrlToResult(url);
                // 将该URL添加至待抓取链表，以便进行下一跳抓取。
                this.solution.addUrlToCrawl(url);
            }
        }
    }

    private void addUrlToResult(String url) {
        this.resultLock.lock();
        try {
            this.resultUrls.add(url);
        } finally {
            this.resultLock.unlock();
        }
    }

    private List<String> fetchResultUrls() {
        this.resultLock.lock();
        try {
            return this.resultUrls;
        } finally {
            this.resultLock.unlock();
        }
    }

    private void addUrlToCrawl(String url) {
        this.crawlLock.lock();
        try {
            this.urlsToCrawl.add(url);
        } finally {
            this.crawlLock.unlock();
        }
    }

    private String fetchUrlToCrawl() {
        this.crawlLock.lock();
        try {
            return this.urlsToCrawl.poll();
        } finally {
            this.crawlLock.unlock();
        }
    }

    private void incrChore() {
        this.choreCount.incrementAndGet();
    }

    private void decrChore() {
        this.choreCount.decrementAndGet();
    }

}
