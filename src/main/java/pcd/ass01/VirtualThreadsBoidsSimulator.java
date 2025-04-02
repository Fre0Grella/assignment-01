package pcd.ass01;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualThreadsBoidsSimulator implements BoidsSimulator {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25;
    private int framerate;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private CyclicBarrier barrier;
    private CyclicBarrier updateViewBarrier;
    private final Semaphore viewCoordinator = new Semaphore(1);

    public VirtualThreadsBoidsSimulator(BoidsModel model) {
        this.model = model;
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

        this.barrier = new CyclicBarrier(model.getBoids().size());
        this.updateViewBarrier = new CyclicBarrier(model.getBoids().size() + 1);


        for (final var boid: model.getBoids()) {
            pool.add(new Thread(() -> {
                while (true) {

                    try {
                        updateViewBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    if (false) {
                        break;
                    }

                    boid.updateVelocity(model);
                    //System.out.println(Thread.currentThread().getName()+" arrived");
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    boid.updatePos(model);
                }

            }));
        }
        pool.forEach(Thread::startVirtualThread);
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

            if (false) {
                break;
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
            }
        }
    }
}
