package pcd.ass01;

import static util.Costants.*;

public class MultithreadedBoidsSimulation {


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
        var sim = new MultithreadedBoidsSimulator(model, cores);
        var view = new BoidsView(model, sim, SCREEN_WIDTH, SCREEN_HEIGHT);
    	sim.config(view);
    }

}
