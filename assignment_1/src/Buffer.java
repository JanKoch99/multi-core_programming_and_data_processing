public class Buffer {

    private final int[] buffer;
    private int count = 0;
    private int in = 0;
    private int out = 0;

    public Buffer(int size) {
        buffer = new int[size];
    }

    public synchronized void put(int value) throws InterruptedException {
        while (count == buffer.length) {
            wait(); // currently buffer is full
        }

        buffer[in] = value;
        in = (in + 1) % buffer.length;
        count++;
        this.notifyAll();
    }

    public synchronized int get() throws InterruptedException {
        while (count == 0) {
            wait(); // currently buffer is empty
        }

        int value = buffer[out];
        out = (out + 1) % buffer.length;
        count--;
        this.notifyAll();
        return value;
    }
}
