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
        //var MtSim = new MultithreadedBoidsSimulator(model, cores);


        //var x = getAvgTime(MtSim,2000,1000);

        var dataseries = new ArrayList<DataSeries>();
        var noCores = new ArrayList<Double>();
        var avgTimes = new ArrayList<Double>();
        for (int i = 1; i <= cores ; i++) {
            var model = resetModel();
            var SrSim = new SerialBoidsSimulator(model);

            noCores.add((double) i);
            avgTimes.add(getAvgTime(SrSim, 5000, 100));

        }
        dataseries.add(new DataSeries(noCores,avgTimes,"Multithread"));

        new ChartGenerator().createLineChart("Speedup.png",
                "Speedup",
                "Number of core",
                "Elapsed Time (ms)",
                false,
                dataseries);
        //System.out.println(x+" ms");




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
