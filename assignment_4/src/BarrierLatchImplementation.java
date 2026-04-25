import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Barrier/Latch time: 2521.9298 ms
 */
public class BarrierLatchImplementation {

    static ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();

    static class Producer extends Thread {
        int N;
        CyclicBarrier barrier;
        CountDownLatch latch;

        Producer(int N, CyclicBarrier b, CountDownLatch l) {
            this.N = N;
            this.barrier = b;
            this.latch = l;
        }

        public void run() {
            try {
                barrier.await(); // wait for all producers
            } catch (Exception e) {}

            for (int i = 0; i < N; i++) {
                queue.add(new Object());
            }

            latch.countDown(); // signal done
        }
    }

    static class Consumer extends Thread {
        int N;
        CountDownLatch latch;

        Consumer(int N, CountDownLatch l) {
            this.N = N;
            this.latch = l;
        }

        public void run() {
            try {
                latch.await(); // wait until producers finished
            } catch (Exception e) {}

            int count = 0;
            while (count < N) {
                if (queue.poll() != null) {
                    count++;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int T = Integer.parseInt(args[0]);
        int N = Integer.parseInt(args[1]);

        CyclicBarrier barrier = new CyclicBarrier(T);
        CountDownLatch latch = new CountDownLatch(T);

        Thread[] producers = new Thread[T];
        Thread[] consumers = new Thread[T];

        long start = System.nanoTime();

        for (int i = 0; i < T; i++) {
            producers[i] = new Producer(N, barrier, latch);
            consumers[i] = new Consumer(N, latch);
            producers[i].start();
            consumers[i].start();
        }

        for (int i = 0; i < T; i++) {
            producers[i].join();
            consumers[i].join();
        }

        long end = System.nanoTime();
        System.out.println("Barrier/Latch time: " + (end - start)/1e6 + " ms");
    }
}
