package pcd.ass01;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultithreadedBoidsSimulator {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25;
    private int framerate;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int cores;
    private final CyclicBarrier barrier;
    private final CyclicBarrier updateViewBarrier;
    private final Semaphore viewCoordinator = new Semaphore(1);

    public MultithreadedBoidsSimulator(BoidsModel model, int cores) {
        this.model = model;
        this.cores = cores;
        this.barrier = new CyclicBarrier(cores);
        this.updateViewBarrier = new CyclicBarrier(cores + 1);
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void toggleSimulation() {
        if (this.running.get()) {
            this.running.set(false);
        } else {
            running.set(true);
            viewCoordinator.release();
        }
    }

    public void runSimulation() {
        System.out.println("start");
        var boids = model.getBoids();
        var nboids = boids.size();
        final var pool = new ArrayList<Thread>();

        for (int i = 0; i < cores; i++) {
            var indexStart = i * nboids / cores;
            var indexEnd = (i + 1) * nboids / cores;
            pool.add(new Thread(() -> {
                while (true) {
                    try {
                        updateViewBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    for (int k1 = indexStart; k1 < indexEnd; k1++) {
                        boids.get(k1).updateVelocity(model);
                    }
                    //System.out.println(Thread.currentThread().getName()+" arrived");
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    //System.out.println(Thread.currentThread().getName()+" Resumed");
                    for (int k = indexStart; k < indexEnd; k++) {
                        boids.get(k).updatePos(model);
                    }
                }
            }));
        }
        pool.forEach(Thread::start);
        while (true) {

            while (!running.get()) {
                try {
                    viewCoordinator.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            var t0 = System.currentTimeMillis();
            try {
                //System.out.println("View Wait");
                updateViewBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                System.out.println("Elapsed time:\t" + dtElapsed);
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
            }
        }
    }
}
