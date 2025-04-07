package util;

import org.jfree.data.Range;
import pcd.ass01.BoidsModel;
import pcd.ass01.BoidsSimulator;
import pcd.ass01.MultithreadedBoidsSimulator;
import pcd.ass01.SerialBoidsSimulator;
import util.ChartGenerator.DataSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static util.Costants.*;

public class Performance {

    public static void main(String[] args) {

        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Number of available core used: "+cores);
        var model = resetModel();
        var MtSim = new MultithreadedBoidsSimulator(model, 1);
        System.out.println("Calculate base time...");
        var base = getAvgTime(MtSim,500,1000);

        var dataseries = new ArrayList<DataSeries>();
        var noCores = new ArrayList<Double>();
        var speedup = new ArrayList<Double>();
        for (int i = 1; i <= cores ; i++) {
            model = resetModel();
            var SrSim = new MultithreadedBoidsSimulator(model,i);
            noCores.add((double) i);
            double wct = 0;
            for (int j = 0; j < 5; j++) {
                System.out.println("it. "+j+" for "+i+" core.");
            wct += getAvgTime(SrSim, 500, 1000);

            }
            wct = wct / 5;
            speedup.add(wct/base);

        }
        dataseries.add(new DataSeries(noCores,speedup,"Multithread"));

        new ChartGenerator().createLineChart("Speedup.png",
                "Speedup",
                "Number of core",
                "Elapsed Time (ms)",
                false,
                dataseries);
        System.out.println("Done.");




    }

    private static double getAvgTime(BoidsSimulator sim, int nBoids, int iteration) {
        var t0 = System.currentTimeMillis();
        sim.config(nBoids,iteration);
        var t1 = System.currentTimeMillis();
        var elapsedTime = t1 - t0;

        return  (double) elapsedTime / iteration;
    }

    private static BoidsModel resetModel() {return new BoidsModel(
            0,
            SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
            ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
            MAX_SPEED,
            PERCEPTION_RADIUS,
            AVOID_RADIUS);}


}
