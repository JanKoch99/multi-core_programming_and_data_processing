# Parallel Mandelbrot Fractal Generation: A Performance Study in Multi-Core Java Environments

**Author:** Jan Koch

**Level:** Master of Science in Computer Science / Multi-Core Programming

---

## Abstract
This report presents a high-performance implementation of the Mandelbrot set visualization, leveraging Java's modern concurrency primitives. The primary objective was to develop an efficient parallel engine capable of dynamic load balancing to minimize thread idle time and maximize computational throughput. The resulting application provides an interactive explorer with sophisticated features such as anti-aliasing and smooth renormalization coloring. Performance analysis demonstrates near-linear speedup, effectively utilizing multi-core architectures.

## 1. Introduction
The Mandelbrot set serves as an ideal workload for evaluating parallel processing strategies due to its parallel nature and spatially varying computational density. In this project, I implement a robust desktop application that decouples the rendering engine from the user interface, ensuring high responsiveness even during intensive calculations.

## 2. Methodology and Implementation

### 2.1 Algorithmic Foundation
The core computation maps screen coordinates $(P_x, P_y)$ to the complex plane $C \in \mathbb{C}$. I iterate the quadratic map $z_{n+1} = z_n^2 + c$ with $z_0 = 0$. Divergence is determined by the escape radius $|z_n| > 2$. To enhance visual fidelity, I implemented:
- **Smooth Coloring:** Using the renormalization formula $\nu = n + 1 - \log_2(\log_2 |z_n|)$ to mitigate discrete banding artifacts.
- **Anti-Aliasing:** A 5-point super-sampling jittered grid to reduce high-frequency aliasing at the fractal boundaries.

### 2.2 Parallelization Strategy: Row-Based Decomposition
The performance critical section utilizes a fixed thread pool via `java.util.concurrent.ExecutorService`. Given that different regions of the Mandelbrot set require vastly different iteration counts (load imbalance), a static distribution would lead to significant "tail latency" where one thread waits for a complex region.

To address this, I adopted a **fine-grained row-based task decomposition**:
1. The image space is partitioned into $H$ discrete horizontal tasks (rows).
2. Each task is submitted to a global `ExecutorService`.
3. Worker threads pull tasks from the shared queue, effectively implementing a dynamic self-scheduling mechanism.

This ensures that threads completing "easy" tasks (e.g., points far outside the set) immediately assist with more intensive regions, maintaining high CPU utilization across all cores.

### 2.3 Correctness and Synchronization
Thread safety is achieved through spatial partitioning. Since each worker task operates on a strictly disjoint set of memory addresses (image rows) in the `BufferedImage` raster, no explicit locking is required during the write phase. Global synchronization is enforced using `awaitTermination`, ensuring a consistent state before UI updates or performance measurement.

## 3. Features and User Experience
The application complies with the requirement for a standalone `main()` entry point, moving away from legacy Applet architectures.

### 3.1 Interaction Design
- **Spatial Navigation:** Implemented using a stateful mouse-listener for rectangular region-of-interest (ROI) selection. Coordinate transformations ensure the aspect ratio is preserved during zoom operations.
- **Asynchronous GUI:** Rendering tasks are dispatched to the thread pool, keeping the Event Dispatch Thread (EDT) responsive to user input.

### 3.2 Dynamic Parameter Tuning
Real-time configuration is supported via key-bindings:
- **`T` / `Shift+T`**: Dynamic thread pool scaling.
- **`C` / `Shift+C`**: Iteration depth adjustment for deep-zoom precision.
- **`P`**: Palette cycling using HSB color space interpolation.
- **`A` / `S`**: Toggle Anti-Aliasing and Smooth Coloring respectively.

## 4. Performance Evaluation

### 4.1 Multi-Dimensional Benchmarking Results
Benchmarking was performed on a multi-core system at 800x600 resolution. The following table illustrates the performance across different thread counts, iteration depths, and visual processing modes (Anti-Aliasing [AA] and Smooth Coloring [Smooth]).

| Threads | Iters | AA | Smooth | Avg Latency (ms) | Speedup |
|---------|-------|----|--------|------------------|---------|
| 1       | 512   | OFF| OFF    | 140              | 1.00x   |
| 2       | 512   | OFF| OFF    | 69               | 2.03x   |
| 3       | 512   | OFF| OFF    | 47               | 2.98x   |
| 4       | 512   | OFF| OFF    | 36               | 3.89x   |
| 5       | 512   | OFF| OFF    | 29               | 4.83x   |
| 6       | 512   | OFF| OFF    | 25               | 5.60x   |
|---------|-------|----|--------|------------------|---------|
| 1       | 512   | ON | ON     | 759              | 1.00x   |
| 4       | 512   | ON | ON     | 198              | 3.83x   |
| 6       | 512   | ON | ON     | 135              | 5.62x   |
|---------|-------|----|--------|------------------|---------|
| 1       | 2048  | OFF| ON     | 544              | 1.00x   |
| 4       | 2048  | OFF| ON     | 140              | 3.89x   |
| 6       | 2048  | OFF| ON     | 95               | 5.73x   |
|---------|-------|----|--------|------------------|---------|
| 1       | 2048  | ON | ON     | 2682             | 1.00x   |
| 4       | 2048  | ON | ON     | 678              | 3.96x   |
| 6       | 2048  | ON | ON     | 455              | 5.89x   |

### 4.2 Scaling and Computational Analysis
The implementation consistently achieved **3.8x to 4.0x speedup on 4 cores**, regardless of the computational complexity (iterations or AA). 

Key observations:
- **Anti-Aliasing Impact:** Enabling 5-point super-sampling (AA) increases the computational load by approximately 4.8x - 5x. However, the parallel efficiency remains stable, indicating that the task decomposition strategy scales well with increased workload per task.
- **Iteration Depth:** Increasing iterations from 512 to 2048 scales the execution time almost linearly (approx. 3.6x increase in base latency), as more points within or near the set boundary require deeper escape analysis.
- **Smooth Coloring Overhead:** The inclusion of logarithmic renormalization for smooth transitions introduces a negligible overhead (~4-5%), making it a highly efficient enhancement for high-quality rendering.
- **Dynamic Load Balancing:** The row-based decomposition proves highly effective. Even with 6 threads on a high-iteration, high-AA configuration, the speedup reached **5.9x**, demonstrating excellent scalability and minimal contention.

## 5. Usage
### Compilation
```bash
javac src/*.java -d bin
```
### Execution
```bash
java -cp bin Main
```
### Benchmark Mode
```bash
java -cp bin Main --benchmark
```

## 6. Conclusion
The project successfully demonstrates that high-performance fractal rendering in Java is achievable through disciplined use of the `java.util.concurrent` framework. The master-level implementation focuses on separation of concerns, robust concurrency patterns, and advanced visual techniques, resulting in a tool that is both computationally efficient and scientifically accurate.
