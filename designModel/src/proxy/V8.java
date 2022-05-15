package proxy;

import javafx.scene.control.Tab;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class V8 {

    // 测试能否生成没有接口的对象, 不行，因为返回的代理对象，没法进行接收
    private class Tank {
        public void move() {
            System.out.println("tank is moving...");
        }
    }

    public static void main(String[] args) {
        V8 v8 = new V8();
        Tank tank = v8.new Tank();
        Tank tank1 = (Tank) Proxy.newProxyInstance(tank.getClass().getClassLoader(), new Class[]{}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Object o = method.invoke(tank, args);          // 注意这里面的tank
                return o;
            }
        });
    }

}
