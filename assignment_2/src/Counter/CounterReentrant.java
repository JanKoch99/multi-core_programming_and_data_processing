package Counter;

import java.util.concurrent.locks.ReentrantLock;

public class CounterReentrant extends Counter {
    ReentrantLock lock = new ReentrantLock();

    public CounterReentrant(int N) {
        super(N);
    }

    @Override
    public synchronized void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public synchronized void decrement() {
        lock.lock();
        try {
            count--;
        } finally {
            lock.unlock();
        }
    }
}
