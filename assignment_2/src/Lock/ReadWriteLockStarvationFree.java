package Lock;

public class ReadWriteLockStarvationFree {
    private int readers;
    private int writers;
    private int waitingWriters;

    public ReadWriteLockStarvationFree() {
        readers = 0;
        writers = 0;
        waitingWriters = 0;
    }

    public synchronized void lockRead() throws InterruptedException {
        while (writers > 0 || waitingWriters > 0) {
            wait();
        }
        readers++;
    }

    public synchronized void unlockRead() {
        readers--;
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        waitingWriters++;
        while (readers > 0 || writers > 0) {
            wait();
        }
        waitingWriters--;
        writers++;
    }

    public synchronized void unlockWrite() {
        writers--;
        notifyAll();
    }
}
