/**
 * Application Entry Point.
 * Dispatches execution to the Mandelbrot engine.
 */
public class Main {
    public static void main(String[] args) {
        // Master's level entry point: delegate to the core engine
        Mandelbrot.main(args);
    }
}
