package pcd.ass01;

public interface BoidsSimulator {
    void config(BoidsView view);

    void config(int numBoids, int iteration);

    void startSimulation();

    void runSimulation();

    void stopSimulation();

    default void resetSimulation() {};
}
