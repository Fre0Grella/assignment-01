package pcd.ass01;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class BoidsSimulation {

	//final static int DEFAULT_BOIDS = 5000;

	final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1000; 
	final static int ENVIRONMENT_HEIGHT = 1000;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

	final static int SCREEN_WIDTH = 800; 
	final static int SCREEN_HEIGHT = 800;

    public static void main(String[] args) {      
    	var model = new BoidsModel(
				0,
    					SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT, 
    					ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
    					MAX_SPEED,
    					PERCEPTION_RADIUS,
    					AVOID_RADIUS);
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of available core used: "+cores);
    	var sim = new BoidsSimulator(model,cores);
    	var view = new BoidsView(model, sim, SCREEN_WIDTH, SCREEN_HEIGHT);
    	sim.attachView(view);

    }
}
