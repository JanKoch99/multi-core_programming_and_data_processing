/**
 * The mutual exclusion is ensured via synchronized methods.
 * Only one refill request due to the refillRequested flag.
 * Cook refills only when requested.
 * Savages cannot consume from empty pot
 * No starvation possible as we circle around the savages and only the next savage in line can eat
 */
public class SavagesFair {

    static class Pot {
        private int portions = 0;
        private final int capacity;
        private volatile boolean refillRequested = false;

        public Pot(int capacity) {
            this.capacity = capacity;
        }

        public synchronized boolean takePortion() {
            if (portions > 0) {
                portions--;
                return true;
            }
            return false;
        }

        public synchronized void requestRefill() {
            if (!refillRequested) {
                refillRequested = true;
            }
        }

        public synchronized boolean isRefillRequested() {
            return refillRequested;
        }

        public synchronized void refill() {
            portions = capacity;
            refillRequested = false;
        }

        public synchronized boolean isEmpty() {
            return portions == 0;
        }
    }

    static class Savage extends Thread {
        private final Pot pot;
        private final int id;
        private final int total;
        private static volatile int turn = 0;
        private static final Object turnLock = new Object();

        public Savage(Pot pot, int id, int total) {
            this.pot = pot;
            this.id = id;
            this.total = total;
        }

        @Override
        public void run() {
            // each savage eats once
            while (true) {
                // wait for ones turn
                while (turn != id) {
                    Thread.yield();
                }

                if (!pot.takePortion()) {
                    pot.requestRefill();
                    while (pot.isEmpty()) {
                        Thread.yield();
                    }
                    // Ensures savage can take one portion and does not have to wait for his next turn
                    pot.takePortion();
                }
                synchronized (turnLock) {
                    turn = (turn + 1) % total;
                }
            }
        }
    }

    static class Cook extends Thread {
        private final Pot pot;

        public Cook(Pot pot) {
            this.pot = pot;
        }

        @Override
        public void run() {
            while (true) {
                while (pot.isRefillRequested()) {
                    pot.refill();
                }
                Thread.yield();
            }
        }
    }

    public static void main(String[] args) {
        int N = 5;
        int savages = 5;

        Pot pot = new Pot(N);
        Cook cook = new Cook(pot);
        cook.start();

        for (int i = 0; i < savages; i++) {
            new Savage(pot, i, savages).start();
        }
    }
}
