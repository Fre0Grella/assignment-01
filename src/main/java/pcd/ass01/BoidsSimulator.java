package pcd.ass01;

public interface BoidsSimulator {
    void attachView(BoidsView view);

    void startSimulation();

    void runSimulation();

    void stopSimulation();

    default void resetSimulation() {};
}
