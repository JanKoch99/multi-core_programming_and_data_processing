public class Main {
    public static void main(String[] args) throws InterruptedException {
        // 1.1
        exerciseOnePointOne(1, 10000000);
        exerciseOnePointOne(1, 100000000);
        exerciseOnePointOne(2, 10000000);
        exerciseOnePointOne(2, 100000000);
        exerciseOnePointOne(4, 10000000);
        exerciseOnePointOne(4, 100000000);
        exerciseOnePointOne(8, 10000000);
        exerciseOnePointOne(8, 100000000);
        exerciseOnePointOne(16, 10000000);
        exerciseOnePointOne(16, 100000000);

        /*
        Results:
            Time taken: 767 ms with 1 threads
            Time taken: 19606 ms with 1 threads
            Time taken: 627 ms with 2 threads
            Time taken: 16533 ms with 2 threads
            Time taken: 420 ms with 4 threads
            Time taken: 9150 ms with 4 threads
            Time taken: 207 ms with 8 threads
            Time taken: 5736 ms with 8 threads
            Time taken: 106 ms with 16 threads
            Time taken: 3160 ms with 16 threads
         */

        // 1.2
        exerciseOnePointTwo(1, 10000000);
        exerciseOnePointTwo(1, 100000000);
        exerciseOnePointTwo(2, 10000000);
        exerciseOnePointTwo(2, 100000000);
        exerciseOnePointTwo(4, 10000000);
        exerciseOnePointTwo(4, 100000000);
        exerciseOnePointTwo(8, 10000000);
        exerciseOnePointTwo(8, 100000000);
        exerciseOnePointTwo(16, 10000000);
        exerciseOnePointTwo(16, 100000000);

         /*
        Results:
            Time taken: 794 ms with 1 threads
            Time taken: 21011 ms with 1 threads
            Time taken: 1305 ms with 2 threads
            Time taken: 31340 ms with 2 threads
            Time taken: 849 ms with 4 threads
            Time taken: 25741 ms with 4 threads
            Time taken: 1406 ms with 8 threads
            Time taken: 36733 ms with 8 threads
            Time taken: 1564 ms with 16 threads
            Time taken: 41423 ms with 16 threads
         */

        // 1.3
        exerciseOnePointThree(1, 100000, 100);
        exerciseOnePointThree(10, 100000, 100);
        exerciseOnePointThree(100, 100000, 100);
        exerciseOnePointThree(1000, 100000, 100);
        exerciseOnePointThree(1, 100000, 1000000);
        exerciseOnePointThree(10, 100000, 1000000);
        exerciseOnePointThree(1, 100000, 2);
        exerciseOnePointThree(10, 100000, 2);

         /*
        Results:
            Time taken: 23 ms with 1 threads
            Time taken: 894 ms with 10 threads
            Time taken: 6612 ms with 100 threads
            Time taken: 88138 ms with 1000 threads
            Time taken: 19 ms with 1 threads
            Time taken: 82 ms with 10 threads
            Time taken: 522 ms with 1 threads
            Time taken: 12828 ms with 10 threads
         */

        // 1.4
        /*
        Amdahl's Law: Speedup = 1 / (1 - f + f / n)
        The uniprocessor is 5 times faster compared to a single thread of the multiprocessor. With Amdahl's Law, we can see that the speedup of the total multiprocessor is: 1 / (1 - p + p / 10)
        We need to find the breakpoint on which the speedup is equal to the uniprocessor speedup (5) --> 1 / (1 - p + p / 10) >= 5
        ==> (1 - p + p / 10) <= 1 / 5
        ==> 10 - 2 <= 10p - p
        ==> 8 <= 9p
        ==> p >= 8/9
        This tells us that if 8/9 or more of the program can be parallelized we should prefer the multiprocessor over the uniprocessor.
         */
    }

    private static void exerciseOnePointOne(int threadCount, int n) throws InterruptedException {
        Thread[] threads = new Thread[threadCount];
        int range = n / threadCount;
        int start = 1;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new BasicPrimeCalculator(start, start + range - 1));
            threads[i].start();
            start += range;
        }

        // wait for all threads
        for (Thread t : threads) {
            t.join();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms with " + threadCount + " threads");
    }

    private static void exerciseOnePointTwo(int threadCount, int n) throws InterruptedException {
        Thread[] threads = new Thread[threadCount];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new PrimeCalculatorWithSharedCounter(n));
            threads[i].start();
        }

        // wait for all threads
        for (Thread t : threads) {
            t.join();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms with " + threadCount + " threads");
    }

    private static void exerciseOnePointThree(int threadCount, int n, int bufferSize) throws InterruptedException {
        Buffer buffer = new Buffer(bufferSize);
        Thread[] threadsProducer = new Thread[threadCount];
        Thread[] threadsConsumer = new Thread[threadCount];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            threadsProducer[i] = new Thread(new Producer(buffer, n));
            threadsConsumer[i] = new Thread(new Consumer(buffer));
            threadsProducer[i].start();
            threadsConsumer[i].start();
        }

        // wait for all threads
        for (Thread t : threadsProducer) {
            t.join();
        }

        // set to terminate
        for (int i = 0; i < threadCount; i++) {
            buffer.put(-1);
        }

        // wait for all threads
        for (Thread t : threadsConsumer) {
            t.join();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms with " + threadCount + " threads");
    }
}