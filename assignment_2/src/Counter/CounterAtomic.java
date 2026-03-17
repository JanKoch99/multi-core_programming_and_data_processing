package Counter;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterAtomic extends Counter{

    private final AtomicInteger atomicCount = new AtomicInteger();

    public CounterAtomic(int N) {
        super(N);
        atomicCount.set(0);
    }

    @Override
    public void increment() {
        atomicCount.incrementAndGet();
    }

    @Override
    public void decrement() {
        atomicCount.decrementAndGet();
    }

    @Override
    public int getCount() {
        return atomicCount.get();
    }

}
