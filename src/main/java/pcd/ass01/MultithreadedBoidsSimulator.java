package pcd.ass01;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultithreadedBoidsSimulator implements BoidsSimulator {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25;
    private int framerate;
    private int iteration = 1;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean quit = new AtomicBoolean(false);
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



    public void config(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void config(int numBoids, int iteration) {
        model.initializeBoids(numBoids);
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
        final var pool = new ArrayList<Thread>();

        for (int i = 0; i < cores; i++) {
            var id = i;
            pool.add(new Thread(() -> {
                while (true) {
                    var boids = model.getBoids();
                    var nboids = boids.size();
                    var indexStart = id * nboids / cores;
                    var indexEnd = (id + 1) * nboids / cores;
                    try {
                        updateViewBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    if (quit.get()) {
                        break;
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
        quit.set(true);
        for (Thread thread : pool) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
