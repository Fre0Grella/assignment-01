package pcd.ass01;


import static util.Costants.*;

public class SerialBoidsSimulation {

    public static void main(String[] args) {
        var model = new BoidsModel(
                0,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED,
                PERCEPTION_RADIUS,
                AVOID_RADIUS);
        var sim = new SerialBoidsSimulator(model);
        var view = new BoidsView(model, sim, SCREEN_WIDTH, SCREEN_HEIGHT);
    	sim.config(view);

    }

}
