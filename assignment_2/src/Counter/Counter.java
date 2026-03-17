package Counter;

public class Counter {
    protected int count;
    protected int N;

    public Counter(int N) {
        this.N = N;
        count = 0;
    }

    public void increment() {
        count++;
    }

    public void decrement() {
        count--;
    }

    public int getCount() {
        return count;
    }
}
