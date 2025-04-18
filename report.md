# Assignment #01 - Concurrent Boids
_by Marco Galeri marco.galeri@studio.unibo.it & Luca Patrignani luca.patrignani3@studio.unibo.it_

## Problem analysis
From a computation point of view the problem can be divided into three main sections:
1. Update of the boids' velocities
2. Update of the boids' positions
3. Update the view.

The bulk of the computation is found in the first two sections, so we concentrate our effort of performance improvement in those points. In order to keep the computational results correct all boids' velocities have to be calculated before updating the positions.
Analyzing the data dependencies we notice that the update of a boid's position depends only on its velocity. 
The velocity, on the other hand, depends on three factors:
- Separation, which depends on the surrounding previous boids' positions
- Alignment, which depends on  the surrounding boids' **velocities**
- Cohesion, which depends on the surrounding previous boids' positions.

The computations of separation and cohesion factors are not problematic because when updating boids' velocities the previous positions' computation has been already completed, avoiding any type of race conditions.
On the other hand, alignment is inherently problematic due to the fact that velocities depend on alignment and alignment depends on velocities. This, if not appropriately addressed, can lead to race conditions.

## Design
### Overall design
The proposed design is explicated by the following Petri Net: ![Petri Net](./PetriNet.png)
The upper part of the graph represents the computational part of the program using 42 threads as an example. The lower part models the controller, which is responsible for updating the view, stopping and restarting the simulation.
The `UpdateBarrier` is a barrier responsible for coordinating the controller thread with the computing threads. It guarantees two things:
- stopping and starting the threads avoiding any type of busy waiting
- synchronizing the computational threads, guaranteeing that the calculation of the new positions is terminated before the update of the view and the starting of the new iteration. This is done in order to avoid race conditions is accessing the boids' velocities.

The other `Barrier` is responsible to wait for the completion of velocities' update before updating the positions. This synchronization point guarantees the absence of race conditions in accessing the boids' positions.

### Race conditions in velocities' update
In order to address the race condition previously identified read-write locks have been introduced for each velocity vector. The `CalculateAligment` method will read-lock the other boid's velocity and `UpdateVel` method will write-lock its own velocity when updating it.

### View-Controller synchronization
In order to make the view responsive, the controller is executed on a different thread. The view can stop and resume the simulation by calling respectively the method `StopSimulation` and `StartSimulation` of the controller which changes a condition variable. This condition variable is implemented with an `AtomicBoolean` field and an event `Semaphore`.

### Platform thread version
The platform thread version is a direct implementation of the presented design. Since this is a case of compute-intensive tasks, the number of spawned threads dedicated to computation is `Ncpu + 1` and each of them will simulate a sublist of boids in order to keep the workload balanced among all threads.

### Virtual thread version
This implementation leverages the ability to spawn a great number of virtual threads with little overhead by creating a virtual thread for each boid.

### Task-based version
In this version for each iteration each boid delegates the execution of `UpdateVel` and `UpdatePos` to a `FixedThreadPool`. The synchronization barrier effect is obtained by waiting for the result of `Future<?>` returned. Just like in the platform version the number of underlying threads is `Ncpu + 1`.

## Performance evaluation
In high performance computing, speedup can be defined as:

S(p) = T_parallel(1) / T_parallel(p)

with:
- p = number of processors
- T_parallel(p) = Execution time of the parallel program with p processors
Where S(p) = p is the ideal case.


We choose to don't use the serial program to compute the speedup because it could cause spurious super linear speedup that don't reflect the real performance.
Using this definition we can also define the strong Scaling Efficiency and Weak Scaling Efficiency.

The Strong scaling efficiency can be defined as:

E_s(p) = S(p)/p

This formula is important as it's a visual way to represent the Amdahl law, that is, the amount of code that wasn't parallelized. instead the Weak Scaling Efficiency is different and can be defined as

W(p) = T_1 \ T_p

where:
- t_1 = time required to complete 1 unit of work with 1 processor
- t_p = time required to complete p unit of work with p processors

Unfortunately this formula required an analysis of the computational cost and since the boids simulation don't have a computational cost we exclude this measure to our analysis.


The result below were generated with a script located at src\main\java\util\Performance.java in the performance branch.
We measured the performance of the 3 version on an AMD Ryzen 7 5700U, with 8 core and 16 logic processor, and this is what we got:

![SpeedUp.png](SpeedUp.png)
![efficiency](StrongScalingEfficiency.png)

We can see that the Task based version is the best clearly outperforming the multithreaded version.
we could also note the peak and elbow present in the multithreaded version, this strange behaviour is due the architecture used to perform the benchmark that has only 8 physical core. In this case after filling each physical core the CPU use the HTT (Hyper-threading Technologies) to perform multiple concurrent computation on every physical core, slowing down the performance. one last thing to pay attention is that the task based version perform a super linear speedup on a 2 core settings. This phenomenon could be due internal optimization made by the jvm at runtime.
For the virtual thread version we couldn't able to record the data presented above because this design is not able to restrain the number of core used to a constant number.
Note: for sake of performance the race condition identified in accessing boids' velocities has not been fixed since it does not alter significantly the correctness of the model. A fixed version of the project can be found and test in the `jpf` branch.

## Model checking with Java Pathfinder
A simplified version of the program has been produced for the sake of checking the proposed model against JPF. This version0's controller only does two iteration and then terminate. Meanwhile, the main thread will call `stopSimulation` and subsequently `startSimulation` to emulate the calling of these methods by the event dispatcher thread of Java Swing. JPF will try every operations order, thus checking that in every moment the call of those methods won't produce any faults or race conditions. 

The model is initialized with a random generator which returns always 0, this is done in order to place the boids close and forcing the model to check if the read write locks previously introduced work correctly. For speeding up the execution the model will contain only two boids with only two threads.

The version that uses JPF is on the Git branch called `jpf`. This is the output produced:
```
Starting a Gradle Daemon (subsequent builds will be faster)

> Task :compileJava
Note: /home/luca/ass01-jpf/src/main/java/pcd/ass01/BoidsView.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.

> Task :runAssignment01Verify
[WARNING] unknown classpath element: /home/luca/ass01-jpf/jpf-runner/build/examples
JavaPathfinder core system v8.0 (rev 81bca21abc14f6f560610b2aed65832fbc543994) - (C) 2005-2014 United States Government. All rights reserved.


====================================================== system under test
pcd.ass01.BoidsTest.main()

====================================================== search started: 4/8/25, 3:25 PM
[WARNING] orphan NativePeer method: jdk.internal.reflect.Reflection.getCallerClass(I)Ljava/lang/Class;
start
start
start

====================================================== results
no errors detected

====================================================== statistics
elapsed time:       00:02:10
states:             new=217915,visited=533370,backtracked=751285,end=61
search:             maxDepth=302,constraints=0
choice generators:  thread=217915 (signal=6296,lock=49027,sharedRef=137432,threadApi=7,reschedule=25153), data=0
heap:               new=115263,released=1051573,maxLive=791,gcCycles=693671
instructions:       16969303
max memory:         246MB
loaded code:        classes=140,methods=3311

====================================================== search finished: 4/8/25, 3:27 PM

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.4/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 2m 23s
2 actionable tasks: 2 executed
```