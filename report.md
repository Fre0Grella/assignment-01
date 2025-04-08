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
On the other hand, alignment is inherently problematic due to the fact that velocities depends on alignment and alignment depends on velocities. This, if not appropriately addressed, can lead to race conditions.

