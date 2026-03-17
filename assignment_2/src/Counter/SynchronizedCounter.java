package Counter;

public class SynchronizedCounter extends Counter {

    public SynchronizedCounter(int N) {
        super(N);
    }

    @Override
    public synchronized void increment() {
        count++;
    }

    @Override
    public synchronized void decrement() {
        count--;
    }
}
