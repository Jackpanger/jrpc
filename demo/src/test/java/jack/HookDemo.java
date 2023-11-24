package jack;

/**
 * description: HookDemo
 * date: 11/24/23 4:57 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class HookDemo {
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Hook is running");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Hook is finished");
        }));
        while (true) {
            System.out.println("running");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
