package Lock;

public class ReadWriteLock {

    private int readers;
    private int writers;

    public ReadWriteLock() {
        readers = 0;
        writers = 0;
    }

    public synchronized void lockRead() throws InterruptedException {
        while (writers > 0) {
            wait();
        }
        readers++;
    }

    public synchronized void unlockRead() {
        readers--;
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        while (readers > 0 || writers > 0) {
            wait();
        }
        writers++;
    }

    public synchronized void unlockWrite() {
        writers--;
        notifyAll();
    }
}
