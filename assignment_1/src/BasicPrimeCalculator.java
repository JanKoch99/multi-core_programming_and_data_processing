public class BasicPrimeCalculator implements Runnable {

    private final int t;

    private final int n;

    public BasicPrimeCalculator(int t, int n) {
        this.t = t;
        this.n = n;
    }

    @Override
    public void run() {
        for (int i = t; i <= n; i++) {
            if (isPrime(i)) {
                // DO NOTHING
            }
        }
    }

    // Copied from chatGPT
    private boolean isPrime(int n) {
        if (n <= 1) return false;      // 0 and 1 are not prime
        if (n <= 3) return true;       // 2 and 3 are prime
        if (n % 2 == 0 || n % 3 == 0) return false; // eliminate multiples of 2 and 3

        int i = 5;
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
            i += 6;
        }
        return true;
    }
}
