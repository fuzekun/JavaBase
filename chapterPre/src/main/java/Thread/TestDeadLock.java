package Thread;

public class TestDeadLock implements Runnable{
	public int flag = 1;
	private Object o1 =new Object(),o2 = new Object();
	public void  run() {
		System.out.println(flag);
		if(flag == 1) {
			synchronized (o1) {
				try{
					Thread.sleep(100);
				}
				catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				synchronized (o2) {
					System.out.println("执行完成1");
				}
			}
		}
		if(flag == 0) {
			synchronized (o2) {
				try {
					Thread.sleep(100);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}	
				synchronized (o1) {
				System.out.println("执行完成2");
				}
			}
		
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestDeadLock test = new TestDeadLock();
		TestDeadLock test2 = new TestDeadLock();
		test.flag=1;
		test2.flag=0;
		Thread thread1 = new Thread(test);
		Thread thread2 = new Thread(test2);
		thread1.start();
		thread2.start();
	}

}
