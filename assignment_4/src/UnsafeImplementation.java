import java.util.LinkedList;

/**
 * Unsafe time: 1720.7093 ms
 */
public class UnsafeImplementation {

    static LinkedList<Object> queue = new LinkedList<>();

    static class Producer extends Thread {
        int N;

        Producer(int N) { this.N = N; }

        public void run() {
            for (int i = 0; i < N; i++) {
                queue.add(new Object());
            }
        }
    }

    static class Consumer extends Thread {
        int N;

        Consumer(int N) { this.N = N; }

        public void run() {
            int count = 0;
            while (count < N) {
                if (!queue.isEmpty()) {
                    queue.removeFirst();
                    count++;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int T = Integer.parseInt(args[0]);
        int N = Integer.parseInt(args[1]);

        Thread[] producers = new Thread[T];
        Thread[] consumers = new Thread[T];

        long start = System.nanoTime();

        for (int i = 0; i < T; i++) {
            producers[i] = new Producer(N);
            consumers[i] = new Consumer(N);
            producers[i].start();
            consumers[i].start();
        }

        for (int i = 0; i < T; i++) {
            producers[i].join();
            consumers[i].join();
        }

        long end = System.nanoTime();
        System.out.println("Unsafe time: " + (end - start)/1e6 + " ms");
    }
}
