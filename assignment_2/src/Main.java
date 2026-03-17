import Counter.Counter;
import Counter.CounterAtomic;
import Counter.CounterReentrant;
import Counter.CounterWorker;
import Counter.SynchronizedCounter;

public class Main {
    public static void main(String[] args) {
        // 2.1
        exerciseTwoPointOne(10000000, 2);
        exerciseTwoPointOne(10000000, 4);
        exerciseTwoPointOne(10000000, 8);
        exerciseTwoPointOne(10000000, 16);

        /* Results:
        Threads: 2 | Final value: -64559 | Time: 7.4633 ms | Method: No Synchronization
        Threads: 2 | Final value: 0 | Time: 1048.5013 ms | Method: Synchronized
        Threads: 2 | Final value: 0 | Time: 1648.0746 ms | Method: Reentrant
        Threads: 2 | Final value: 0 | Time: 250.9772 ms | Method: Atomic
        Threads: 4 | Final value: -8125893 | Time: 307.4445 ms | Method: No Synchronization
        Threads: 4 | Final value: 0 | Time: 2100.0746 ms | Method: Synchronized
        Threads: 4 | Final value: 0 | Time: 1189.594 ms | Method: Reentrant
        Threads: 4 | Final value: 0 | Time: 1412.3332 ms | Method: Atomic
        Threads: 8 | Final value: -918848 | Time: 663.1155 ms | Method: No Synchronization
        Threads: 8 | Final value: 0 | Time: 4575.507 ms | Method: Synchronized
        Threads: 8 | Final value: 0 | Time: 2063.4706 ms | Method: Reentrant
        Threads: 8 | Final value: 0 | Time: 1064.4653 ms | Method: Atomic
        Threads: 16 | Final value: 630657 | Time: 1395.2765 ms | Method: No Synchronization
        Threads: 16 | Final value: 0 | Time: 8233.6677 ms | Method: Synchronized
        Threads: 16 | Final value: 0 | Time: 3818.8262 ms | Method: Reentrant
        Threads: 16 | Final value: 0 | Time: 4311.6977 ms | Method: Atomic
         */

        // 2.2
        /*
        Have a look at Lock/ReadWriteLock.java and Lock/ReadWriteLockStarvationFree.java
         */

        //2.3
        /*
        1.
        - a: We can ignore the voids from A as it could have done r.read() in the past. Then the Order would be A.write(1) -> B.read() -> A.write(2) -> C.read() -> A.write(1) -> C.read()
        Therefore it is sequentially consistent
        - b:A.write(1) -> B.read() -> A.write(2) -> C.read() -> A.write(1)
        Therefore it is sequentially consistent but not Linearizable because B reads 1 and later C reads 2

        2.
        - a: The stack must be empty at the start and B pop() on an empty stack and gets 10 --> Not linearizable and sequentially consistent
        - b: Because the stack should be empty when B: s.empty() it cannot be linearizable or sequentially consistent

        3.
        - a: Queue cannot return the same element twice --> it cannot be linearizable or sequentially consistent
         */
    }

    private static void exerciseTwoPointOne(int N, int T) {
        exerciseTwoPointOneNoSynchro(N, T);
        exerciseTwoPointOneSynchro(N, T);
        exerciseTwoPointOneReentrant(N, T);
        exerciseTwoPointOneAtomic(N, T);
    }

    private static void exerciseTwoPointOneNoSynchro(int N, int T) {
        Counter counter = new Counter(N);
        counterSynchro(N, T, counter, "No Synchronization");
    }

    private static void exerciseTwoPointOneSynchro(int N, int T) {
        Counter counter = new SynchronizedCounter(N);
        counterSynchro(N, T, counter, "Synchronized");
    }

    private static void exerciseTwoPointOneReentrant(int N, int T) {
        Counter counter = new CounterReentrant(N);
        counterSynchro(N, T, counter, "Reentrant");
    }

    private static void exerciseTwoPointOneAtomic(int N, int T) {
        Counter counter = new CounterAtomic(N);
        counterSynchro(N, T, counter, "Atomic");
    }

    private static void counterSynchro(int N, int T, Counter counter, String method) {
        Thread[] threads = new Thread[T];
        long start = System.nanoTime();

        for (int i = 0; i < T; i++) {
            threads[i] = new Thread(new CounterWorker(counter, N, i % 2 == 0));
            threads[i].start();
        }

        for (int i = 0; i < T; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long end = System.nanoTime();

        double timeMs = (end - start) / 1_000_000.0;

        System.out.println("Threads: " + T +
                " | Final value: " + counter.getCount() +
                " | Time: " + timeMs + " ms" +
                " | Method: " + method);
    }
}