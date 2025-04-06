package pcd.ass01;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoidsTest {
    final static int DEFAULT_BOIDS = 2;

    final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1000;
    final static int ENVIRONMENT_HEIGHT = 1000;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

    public static void main(String[] args) throws InterruptedException {
        BoidsModel model = new BoidsModel(
                DEFAULT_BOIDS,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED,
                PERCEPTION_RADIUS,
                AVOID_RADIUS,
                new Supplier<Double>() {
                    final List<Double> values = List.of(0.0, 0.3, 0.5, 0.7, 0.9);
                    int i = -1;
                    @Override
                    public Double get() {
                        i = (i + 1) % values.size();
                        return values.get(i);
                    }
                }
        );
        MultithreadedBoidsSimulator sim = new MultithreadedBoidsSimulator(model, 2);
        new Thread(sim::runSimulation).start();
        sim.stopSimulation();
        sim.startSimulation();
    }
}
