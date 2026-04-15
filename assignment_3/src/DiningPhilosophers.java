import java.util.concurrent.locks.ReentrantLock;

/**
 * No circular wait because of the global ordering
 * Locks are eventually acquired and thus no starving
 */
public class DiningPhilosophers {

    static class Philosopher extends Thread {
        private final ReentrantLock leftFork;
        private final ReentrantLock rightFork;

        public Philosopher(ReentrantLock leftFork, ReentrantLock rightFork) {
            this.leftFork = leftFork;
            this.rightFork = rightFork;
        }

        @Override
        public void run() {
            while (true) {
                think();
                ReentrantLock first = leftFork;
                ReentrantLock second = rightFork;

                // Enforcing order
                if (System.identityHashCode(leftFork) > System.identityHashCode(rightFork)) {
                    first = rightFork;
                    second = leftFork;
                }

                first.lock();
                second.lock();
                try {
                    eat();
                } finally {
                    first.unlock();
                    second.unlock();
                }
            }
        }

        private void think() {}
        private void eat() {}
    }

    public static void main(String[] args) {
        int N = 5;
        ReentrantLock[] forks = new ReentrantLock[N];

        for (int i = 0; i < N; i++) {
            forks[i] = new ReentrantLock();
        }

        for (int i = 0; i < N; i++) {
            new Philosopher(forks[i], forks[(i + 1) % N]).start();
        }
    }
}
