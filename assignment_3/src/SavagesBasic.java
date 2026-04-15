/**
 * The mutual exclusion is ensured via synchronized methods.
 * Only one refill request due to the refillRequested flag.
 * Cook refills only when requested.
 * Savages cannot consume from empty pot
 */
public class SavagesBasic {

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

        public Savage(Pot pot) {
            this.pot = pot;
        }

        @Override
        public void run() {
            // each savage eats once
            while (true) {
                if (pot.takePortion()) {
                    // savage ate successfully
                    break;
                } else {
                    pot.requestRefill();

                    while (pot.isEmpty()) {
                        // wait until the pot is refilled
                        Thread.yield();
                    }
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
        int savages = 10;

        Pot pot = new Pot(N);
        Cook cook = new Cook(pot);
        cook.start();

        for (int i = 0; i < savages; i++) {
            new Savage(pot).start();
        }
    }
}
