package Counter;

public class CounterWorker implements Runnable{

    private Counter counter;
    private int N;
    private boolean isEven;

    public CounterWorker(Counter counter, int N, boolean isEven) {
        this.counter = counter;
        this.N = N;
        this.isEven = isEven;
    }

    @Override
    public void run() {
        if (isEven) {
            for (int i = 0; i < N; i++) {
                counter.increment();
            }
        } else {
            for (int i = 0; i < N; i++) {
                counter.decrement();
            }
        }
    }
}
