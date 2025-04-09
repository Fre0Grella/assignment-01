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
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int cores;
    private final Barrier barrier;
    private final Barrier updateViewBarrier;
    private final Semaphore viewCoordinator = new Semaphore(1);

    public MultithreadedBoidsSimulator(BoidsModel model, int cores) {
        this.model = model;
        this.cores = cores;
        this.barrier = new Barrier(cores);
        this.updateViewBarrier = new Barrier(cores + 1);
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
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
                for (int j = 0; j < 2; j++) {
                    var boids = model.getBoids();
                    var nboids = boids.size();
                    var indexStart = id * nboids / cores;
                    var indexEnd = (id + 1) * nboids / cores;
                    try {
                        updateViewBarrier.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    for (int k1 = indexStart; k1 < indexEnd; k1++) {
                        boids.get(k1).updateVelocity(model);
                    }
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    for (int k = indexStart; k < indexEnd; k++) {
                        boids.get(k).updatePos(model);
                    }
                }
            }));
        }
        pool.forEach(Thread::start);
        for (int i = 0; i < 2; i++) {
            while (!running.get()) {
                try {
                    viewCoordinator.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                updateViewBarrier.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
