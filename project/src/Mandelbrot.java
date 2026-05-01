import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Mandelbrot: A high-performance parallel fractal rendering engine.
 * 
 * This class implements a multi-threaded Mandelbrot set generator with
 * support for dynamic load balancing via row-based task decomposition.
 * Features include smooth renormalization coloring, super-sampled anti-aliasing,
 * and an interactive Swing-based GUI.
 *
 */
public class Mandelbrot {
    /** Concurrent Rendering Configuration */
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int MAX_ITER_DEFAULT = 512;
    private static final double LOG2 = Math.log(2.0);

    /** Viewport boundaries in the Complex Plane */
    private double xMin = -2.0;
    private double xMax = 0.5;
    private double yMin = -1.25;
    private double yMax = 1.25;

    /** Internal State */
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private int maxIter = MAX_ITER_DEFAULT;
    private int numThreads = Runtime.getRuntime().availableProcessors();
    
    private BufferedImage image;
    private JPanel canvas;
    private JLabel statusBar;
    private JFrame frame;

    /** Interaction State */
    private Point dragStart;
    private Point dragEnd;
    private boolean isShiftDown = false;
    private boolean isHeadless = false;

    /** Visual Enhancement Flags */
    private int paletteIdx = 0;
    private boolean antiAliasing = false;
    private boolean smoothColoring = true; // Enabled by default for "Master's" quality

    /**
     * Default constructor initializes the engine in interactive mode.
     */
    public Mandelbrot() {
        this(false);
    }

    /**
     * Parameterized constructor for headless or interactive execution.
     * @param headless If true, suppresses GUI initialization.
     */
    public Mandelbrot(boolean headless) {
        this.isHeadless = headless;
        if (!isHeadless) {
            initGui();
        }

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        render();
    }

    private void initGui() {
        try {
            frame = new JFrame("Parallel Mandelbrot Fractal Generator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            canvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(image, 0, 0, null);
                    if (dragStart != null && dragEnd != null) {
                        g.setColor(Color.WHITE);
                        int x = Math.min(dragStart.x, dragEnd.x);
                        int y = Math.min(dragStart.y, dragEnd.y);
                        int w = Math.abs(dragStart.x - dragEnd.x);
                        int h = Math.abs(dragStart.y - dragEnd.y);
                        g.drawRect(x, y, w, h);
                    }
                }
            };
            canvas.setPreferredSize(new Dimension(width, height));
            frame.add(canvas, BorderLayout.CENTER);

            statusBar = new JLabel(" Ready");
            frame.add(statusBar, BorderLayout.SOUTH);

            setupListeners();
            frame.pack();
            frame.setLocationRelativeTo(null);
        } catch (HeadlessException e) {
            System.err.println("Running in headless mode, GUI disabled.");
            isHeadless = true;
        }
    }

    public void setVisible(boolean visible) {
        if (frame != null) frame.setVisible(visible);
    }

    private void setupListeners() {
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                isShiftDown = e.isShiftDown();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragEnd = e.getPoint();
                if (dragStart != null && !dragStart.equals(dragEnd)) {
                    zoomToRect(dragStart, dragEnd, isShiftDown);
                }
                dragStart = null;
                dragEnd = null;
                canvas.repaint();
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragEnd = e.getPoint();
                canvas.repaint();
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean shift = e.isShiftDown();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        reset();
                        break;
                    case KeyEvent.VK_I:
                        zoom(0.5);
                        break;
                    case KeyEvent.VK_O:
                        zoom(2.0);
                        break;
                    case KeyEvent.VK_T:
                        numThreads = Math.max(1, numThreads + (shift ? -1 : 1));
                        render();
                        break;
                    case KeyEvent.VK_C:
                        maxIter = Math.max(1, maxIter + (shift ? -128 : 128));
                        render();
                        break;
                    case KeyEvent.VK_P:
                        paletteIdx = (paletteIdx + (shift ? -1 : 1) + 3) % 3;
                        render();
                        break;
                    case KeyEvent.VK_A:
                        antiAliasing = !antiAliasing;
                        render();
                        break;
                    case KeyEvent.VK_S:
                        smoothColoring = !smoothColoring;
                        render();
                        break;
                }
            }
        });
    }

    private void reset() {
        xMin = -2.0; xMax = 0.5;
        yMin = -1.25; yMax = 1.25;
        maxIter = MAX_ITER_DEFAULT;
        render();
    }

    private void zoom(double factor) {
        double xCenter = (xMin + xMax) / 2.0;
        double yCenter = (yMin + yMax) / 2.0;
        double xRange = (xMax - xMin) * factor;
        double yRange = (yMax - yMin) * factor;
        xMin = xCenter - xRange / 2.0;
        xMax = xCenter + xRange / 2.0;
        yMin = yCenter - yRange / 2.0;
        yMax = yCenter + yRange / 2.0;
        render();
    }

    private void zoomToRect(Point p1, Point p2, boolean moveOnly) {
        double nx1 = xMin + (double)p1.x / width * (xMax - xMin);
        double ny1 = yMin + (double)p1.y / height * (yMax - yMin);
        double nx2 = xMin + (double)p2.x / width * (xMax - xMin);
        double ny2 = yMin + (double)p2.y / height * (yMax - yMin);

        if (moveOnly) {
            double dx = nx2 - nx1;
            double dy = ny2 - ny1;
            xMin -= dx; xMax -= dx;
            yMin -= dy; yMax -= dy;
        } else {
            xMin = Math.min(nx1, nx2);
            xMax = Math.max(nx1, nx2);
            yMin = Math.min(ny1, ny2);
            yMax = Math.max(ny1, ny2);
            
            // Maintain aspect ratio
            double newWidth = xMax - xMin;
            double newHeight = yMax - yMin;
            double ratio = (double)width / height;
            if (newWidth / newHeight > ratio) {
                double targetHeight = newWidth / ratio;
                double diff = targetHeight - newHeight;
                yMin -= diff / 2.0;
                yMax += diff / 2.0;
            } else {
                double targetWidth = newHeight * ratio;
                double diff = targetWidth - newWidth;
                xMin -= diff / 2.0;
                xMax += diff / 2.0;
            }
        }
        render();
    }

    /**
     * Dispatches the rendering task to the worker thread pool.
     * Implements a fine-grained row-based decomposition to achieve
     * optimal dynamic load balancing across physical and logical cores.
     */
    private void render() {
        if (image == null) return;
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int y = 0; y < height; y++) {
            final int row = y;
            executor.execute(() -> {
                for (int x = 0; x < width; x++) {
                    int color = antiAliasing ? computeAntiAliasedColor(x, row) : computeColor(x, row);
                    image.setRGB(x, row, color);
                }
            });
        }

        executor.shutdown();
        try {
            // Wait for completion (blocking call for simplicity in state management)
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Rendering pipeline interrupted: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        if (!isHeadless) {
            SwingUtilities.invokeLater(() -> {
                if (statusBar != null) {
                    statusBar.setText(String.format(" Rendered in %d ms | Threads: %d | Iterations: %d | AA: %s | Smooth: %s",
                            duration, numThreads, maxIter, antiAliasing ? "ON" : "OFF", smoothColoring ? "ON" : "OFF"));
                }
                if (canvas != null) {
                    canvas.repaint();
                }
            });
        }
    }

    /**
     * Map screen coordinates to the complex plane and compute the escape iteration.
     * @param px X-coordinate in pixel space
     * @param py Y-coordinate in pixel space
     * @return Packed RGB integer
     */
    private int computeColor(int px, int py) {
        double cRe = xMin + (px * (xMax - xMin) / width);
        double cIm = yMax - (py * (yMax - yMin) / height);
        return mandelbrot(cRe, cIm);
    }

    /**
     * Performs super-sampled anti-aliasing using a 5-point grid.
     * @param px X-coordinate in pixel space
     * @param py Y-coordinate in pixel space
     * @return Averaged RGB integer
     */
    private int computeAntiAliasedColor(int px, int py) {
        // 5-point super-sampling pattern (Jittered Grid)
        double[][] offsets = {
                {0.5, 0.5}, {0.25, 0.25}, {0.75, 0.25}, {0.25, 0.75}, {0.75, 0.75}
        };

        int r = 0, g = 0, b = 0;
        for (double[] offset : offsets) {
            double cRe = xMin + (px + offset[0]) * (xMax - xMin) / width;
            double cIm = yMax - (py + offset[1]) * (yMax - yMin) / height;
            int color = mandelbrot(cRe, cIm);
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }

        return ((r / 5) << 16) | ((g / 5) << 8) | (b / 5);
    }

    /**
     * Core Mandelbrot iteration loop.
     * Implements smooth renormalization coloring if enabled.
     * @param cRe Real part of the complex constant c
     * @param cIm Imaginary part of the complex constant c
     * @return Packed RGB color
     */
    private int mandelbrot(double cRe, double cIm) {
        double zRe = 0, zIm = 0;
        int iter = 0;
        while (zRe * zRe + zIm * zIm <= 4.0 && iter < maxIter) {
            double nextZRe = zRe * zRe - zIm * zIm + cRe;
            double nextZIm = 2.0 * zRe * zIm + cIm;
            zRe = nextZRe;
            zIm = nextZIm;
            iter++;
        }

        if (iter == maxIter) return 0x000000; // Point is inside the set

        double mu = iter;
        if (smoothColoring) {
            double modulus = Math.sqrt(zRe * zRe + zIm * zIm);
            mu = iter + 1 - Math.log(Math.log(modulus)) / LOG2;
        }

        return getColorFromPalette(mu);
    }

    /**
     * Internal color mapping based on escape time (mu).
     * @param mu Escaped iteration count (supports fractional values for smoothing)
     * @return Packed RGB integer
     */
    private int getColorFromPalette(double mu) {
        float h;
        switch (paletteIdx) {
            case 0: // Rainbow Gradient
                h = (float) (mu / 256.0);
                return Color.HSBtoRGB(h, 0.8f, 1.0f);
            case 1: // Monochromatic Blue/White
                h = (float) (0.6 + mu / 500.0);
                return Color.HSBtoRGB(h, 0.5f, (float)Math.min(1.0, mu/100.0));
            case 2: // High-Contrast Fire
                h = (float) (mu / 1000.0);
                return Color.HSBtoRGB(h, 1.0f, (float)Math.min(1.0, mu/50.0));
            default:
                return Color.WHITE.getRGB();
        }
    }

    /**
     * Entry point for standalone execution.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--benchmark")) {
            runPerformanceAnalysis();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            new Mandelbrot().setVisible(true);
        });
    }

    /**
     * Executes an expanded series of performance benchmarks to evaluate multi-core scaling
     * across various rendering configurations (iterations, AA, smoothing).
     */
    private static void runPerformanceAnalysis() {
        System.out.println("Starting comprehensive multi-core performance analysis...");
        Mandelbrot engine = new Mandelbrot(true); // Headless mode
        
        int[] threadConfigs = {1, 2, 3, 4, 5, 6};
        int[] iterConfigs = {512, 2048};
        boolean[] aaConfigs = {false, true};
        boolean[] smoothConfigs = {false, true};

        System.out.printf("%-8s | %-6s | %-4s | %-7s | %-12s | %-8s\n", 
                "Threads", "Iters", "AA", "Smooth", "Avg Latency", "Speedup");
        System.out.println("-------------------------------------------------------------------------");

        for (int iter : iterConfigs) {
            for (boolean aa : aaConfigs) {
                for (boolean smooth : smoothConfigs) {
                    engine.maxIter = iter;
                    engine.antiAliasing = aa;
                    engine.smoothColoring = smooth;
                    
                    long t1 = 0;
                    for (int t : threadConfigs) {
                        engine.numThreads = t;
                        long accum = 0;
                        final int benchmarkRuns = 3;
                        
                        // Warm-up & Benchmark
                        for (int i = 0; i < benchmarkRuns; i++) {
                            long start = System.currentTimeMillis();
                            engine.render();
                            accum += (System.currentTimeMillis() - start);
                        }
                        
                        long avg = accum / benchmarkRuns;
                        if (t == 1) t1 = avg;
                        double s = (double) t1 / (avg == 0 ? 1 : avg);
                        
                        System.out.printf("%-8d | %-6d | %-4s | %-7s | %-8d ms | %-6.2fx\n", 
                                t, iter, aa ? "ON" : "OFF", smooth ? "ON" : "OFF", avg, s);
                    }
                    System.out.println("-------------------------------------------------------------------------");
                }
            }
        }
        System.exit(0);
    }
}
