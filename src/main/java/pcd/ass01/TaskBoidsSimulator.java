package pcd.ass01;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskBoidsSimulator implements BoidsSimulator {
    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25;
    private int framerate;
    private int iteration = 1;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int cores;
    private final Semaphore viewCoordinator = new Semaphore(1);
    private final ExecutorService pool;

    public TaskBoidsSimulator(BoidsModel model, int cores) {
        this.model = model;
        this.cores = cores;
        view = Optional.empty();
        pool = Executors.newFixedThreadPool(this.cores);
    }

    public void config(BoidsView view) {
        this.view = Optional.of(view);
    }

    @Override
    public void initBoids(int numBoids) {
        model.initializeBoids(numBoids);
    }

    public void config(int numBoids, int iteration) {
        //model.initializeBoids(numBoids);
        this.iteration = iteration;
        runSimulation();
    }

    public void startSimulation() {
        running.set(true);
        viewCoordinator.release();
    }

    @Override
    public void stopSimulation() {
        this.running.set(false);
    }

    public void runSimulation() {
        System.out.println("start");
        int it = 0;
        while (it < iteration) {
            while (!running.get()) {
                try {
                    viewCoordinator.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            var t0 = System.currentTimeMillis();
            final var futures = new ArrayList<Future<?>>();
            for (final var boid: model.getBoids()) {
                final var future = pool.submit(() -> boid.updateVelocity(model));
                futures.add(future);
            }
            for (final var future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            for (final var boid: model.getBoids()) {
                final var future = pool.submit(() -> boid.updatePos(model));
                futures.add(future);
            }
            for (final var future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                //System.out.println("Elapsed time:\t" + dtElapsed);
                var framratePeriod = 1000 / FRAMERATE;
                if (dtElapsed < framratePeriod) {
                    try {
                        Thread.sleep(framratePeriod - dtElapsed);
                    } catch (Exception ignored) {
                    }
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000 / dtElapsed);
                }
            } else {
                it++;
                //System.out.println("Iteration no.\t"+it);
            }
        }
    }
}
