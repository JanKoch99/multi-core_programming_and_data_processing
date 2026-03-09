public class Producer implements Runnable{

    private final Buffer buffer;
    private final int n;
    private int value = 0;

    public Producer(Buffer buffer, int n) {
        this.buffer = buffer;
        this.n = n;
    }

    @Override
    public void run() {
        try {
            while (true) {
                buffer.put(value++);
                if (value > n) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
