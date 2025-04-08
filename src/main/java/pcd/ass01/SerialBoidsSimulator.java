package pcd.ass01;

import java.util.Optional;

public class SerialBoidsSimulator implements BoidsSimulator {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private int iteration = 1;
    private static final int FRAMERATE = 25;
    private int framerate;
    private boolean running = true;

    public SerialBoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
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

    @Override
    public void startSimulation() {
        running = true;
    }

    @Override
    public void stopSimulation() {
        this.running = false;

    }

    public void runSimulation() {
        int it = 0;
    	while (it < iteration) {
            while (running && it < iteration) {
                var t0 = System.currentTimeMillis();
    		    var boids = model.getBoids();
    		    /*
    		    for (Boid boid : boids) {
                    boid.update(model);
                }
                */

    		    /*
    		     * Improved correctness: first update velocities...
    		     */
    		    for (Boid boid : boids) {
                    boid.updateVelocity(model);
                }

    		    /*
    		     * ..then update positions
    		     */
    		    for (Boid boid : boids) {
                    boid.updatePos(model);
                }


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
    		    } else {
                    it++;
                    //System.out.println("Iteration no.\t"+it);
                }
            }
    	}
    }
}
