package chainOfResponsibility;


import java.util.ArrayList;
import java.util.List;

/*
*
*   经典servlet的实现版本
* */
public class FilterChainIV {

    public static void main(String[] args) {
        MyFilterChain fc = new MyFilterChain();
        fc.add(new MyHtmlFilter()).add(new MySensitiveFilter());
        MyResponse res = new MyResponse();
        MyRequest req = new MyRequest();
        fc.doFilter(req, res, fc);
        System.out.println("req = " + req.msg);
        System.out.println("resp = " + res.msg);
    }
}

class MyRequest {
    String msg = "";
}

class MyResponse {
    String msg = "";
}

interface MyFilter {
    public void doFilter(MyRequest req, MyResponse resp, MyFilterChain fc);
}

class MyFilterChain implements MyFilter{
    List<MyFilter>filters = new ArrayList<>();
    int index = 0;

    public MyFilterChain add(MyFilter filter) {
        filters.add(filter);
        return this;
    }

    @Override
    public void doFilter(MyRequest req, MyResponse resp, MyFilterChain fc) {
        if (index >= filters.size()) return ;
        MyFilter filter = filters.get(index);
        index++;
        filter.doFilter(req, resp, fc);
    }
}

class MyHtmlFilter implements MyFilter{

    @Override
    public void doFilter(MyRequest req, MyResponse resp, MyFilterChain fc) {
        req.msg += "Req.htmlFilter-->";
        fc.doFilter(req, resp, fc);           // 如果从这里不进行递归调用，就不进行下一步处理了
        resp.msg += "Resp.htmlFilter-->";
    }
}
class MySensitiveFilter implements MyFilter{
    @Override
    public void doFilter(MyRequest req, MyResponse resp, MyFilterChain fc) {
        req.msg += "Req.sensitiveFilter-->";
        fc.doFilter(req, resp, fc);             // 有用户自己决定是否进行下一步的调用
        resp.msg += "Resp.sensitiveFilter-->";
    }
}
