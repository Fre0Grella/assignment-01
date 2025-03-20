package pcd.ass01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 25;
    private int framerate;
    private boolean running = true;
    private final int cores;
    private CyclicBarrier barrier;
    private CountDownLatch cd;

    public BoidsSimulator(BoidsModel model, int cores) {
        this.model = model;
        this.cores = cores; //for an optimal use of thread
        this.barrier = new CyclicBarrier(cores);
        this.cd = new CountDownLatch(1);
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public boolean isRunning() {
        return running;
    }

    public void toogleSimulation() {
            this.running = !this.running;
    }

    public void stopSimulation() {
        this.running = false;
    }

    public void runSimulation() {
    	while (true) {
            var boids = model.getBoids();
            var nboids = boids.size();
            final var pool = new ArrayList<Thread>();
            for (int i = 0; i < cores; i++) {
                var indexStart = i*nboids/cores;
                var indexEnd = (i+1)*nboids/cores;

                pool.add(new Thread(() -> {
                    while(running) {
                        for (int k = indexStart; k < indexEnd; k++) {
                            boids.get(k).updateVelocity(model);
                        }

                        try {
                            barrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                        /*
                         * ..then update positions
                         */
                        for (int k = indexStart; k < indexEnd; k++) {
                            boids.get(k).updatePos(model);
                        }

                        barrier.reset();
                        try {
                            cd.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
            }
            pool.forEach(Thread::start);
                /*pool.forEach((t)->{
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });*/
            while (running) {
                var t0 = System.currentTimeMillis();

    		    if (view.isPresent()) {
                	view.get().update(framerate);
                	var t1 = System.currentTimeMillis();
                    var dtElapsed = t1 - t0;
                    var framratePeriod = 1000/FRAMERATE;

                    if (dtElapsed < framratePeriod) {
                    	try {
                    		Thread.sleep(framratePeriod - dtElapsed);
                    	} catch (Exception ex) {}
                    	framerate = FRAMERATE;
                    } else {
                    	framerate = (int) (1000/dtElapsed);
                    }
    		    }
                cd.countDown();
            }
            try {
                Thread.sleep(50);
            } catch (Exception ex) {}
    	}
    }
}
