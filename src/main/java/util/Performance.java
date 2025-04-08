package util;

import org.jfree.data.Range;
import pcd.ass01.*;
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
        //var MtSim = new MultithreadedBoidsSimulator(model, 1);
        System.out.println("Calculate base time...");
        //var base = getAvgTime(MtSim,500,1000);
        //new ChartGenerator().exampleUsage();
        System.out.println("done");
        //var effDataseries = new ArrayList<DataSeries>();
        //var dataseries = new ArrayList<DataSeries>();
        var noCores = new ArrayList<Double>();
        var speedup = new ArrayList<Double>();
        var efficiency = new ArrayList<Double>();
        MultithreadedBoidsSimulator MtSim;
        double base = 0;
        for (int i = 1; i <= cores ; i++) {
            noCores.add((double) i);
            double wct = 0;
            for (int j = 0; j < 5; j++) {
                model = resetModel();
                MtSim = new MultithreadedBoidsSimulator(model,i);
                System.out.println("it. "+j+" for "+i+" core.");
            wct += getAvgTime(MtSim, 1000, 1000);
            }
            wct = wct / 5;
            if (i == 1) {
                base = wct;
            }
            var y = base/wct;
            var z = (y/i)*100;
            System.out.println("base:\t"+base);
            System.out.println("wct:\t"+wct);
            System.out.println("sce:\t"+z);
            speedup.add(y);
            efficiency.add(z);

        }
        var mtsp = new DataSeries(noCores,speedup,"Multithread");
        var mtse = new DataSeries(noCores,efficiency, "Multithread");
        var noCores2 = new ArrayList<Double>();
        var speedup2 = new ArrayList<Double>();
        var efficiency2 = new ArrayList<Double>();
        TaskBoidsSimulator TsSim;
        base = 0;
        for (int i = 1; i <= cores ; i++) {
            noCores2.add((double) i);
            double wct = 0;
            for (int j = 0; j < 5; j++) {
                model = resetModel();
                TsSim = new TaskBoidsSimulator(model,i);
                System.out.println("it. "+j+" for "+i+" core.");
                wct += getAvgTime(TsSim, 1000, 1000);
            }
            wct = wct / 5;
            if (i == 1) {
                base = wct;
            }
            var y = base/wct;
            var z = (y/i)*100;
            System.out.println("base:\t"+base);
            System.out.println("wct:\t"+wct);
            System.out.println("sce:\t"+z);
            speedup2.add(y);
            efficiency2.add(z);

        }


        var tbsp =new DataSeries(noCores2,speedup2,"Task-based");
        var tbse = new DataSeries(noCores2,efficiency2, "Task-based");

        new ChartGenerator().createLineChart("SpeedUp.png",
                "Speedup",
                "Number of core",
                "Speedup",
                false,
                List.of(new DataSeries(noCores,speedup,"Multithread")
                        ,new DataSeries(noCores2,speedup2,"Task-based")));
        new ChartGenerator().createLineChart("StrongScalingEfficiency.png",
                "StrongScalingEfficiency",
                "Number of core",
                "Efficiency",
                true,
                List.of(new DataSeries(noCores,efficiency, "Multithread"),
                        new DataSeries(noCores2,efficiency2, "Task-based")));
        System.out.println("Done.");




    }

    private static double getAvgTime(BoidsSimulator sim, int nBoids, int iteration) {
        sim.initBoids(nBoids);
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
