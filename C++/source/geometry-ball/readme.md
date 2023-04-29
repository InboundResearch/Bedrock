# Geometry- Ball

computing the "ideal" bounding ball depends on the definition of "ideal". optimizing
time vs. accuracy is a classic tradeoff. if speed matters most, there are several O(n)
algorithms that produce approximate balls that may be 10%-25% larger than necessary.
most are two-pass, and involve some sort of geometric operations:

- compute the mean point, then compute the radius

- compute the median point (center of a bounding box), then compute the radius

- Jack Ritter's 1990 paper computes the median point in the axis of greatest separation,
  and uses that to define the initial ball. A second pass through the points identifies
  points that are outside of the ball, and uses the point to skewer the existing ball.
  the ball is grown by choosing a new center along the skewer line that divides the
  farthest point on the existing ball and the new point. the algorithm is described in a
  "graphics gem", which is available at:
    https://www.researchgate.net/publication/242453691_An_Efficient_Bounding_Sphere

- Welzl presented a linear programming technique to compute an exact ball in O(n)
  expected runtime, using a randomized sampler. the effect is to sort the input points
  according to their distance to the "center", and use the outermost samples to define
  the ball's boundary. his paper is available at:
    http://www.stsci.edu/~RAB/Backup%20Oct%2022%202011/f_3_CalculationForWFIRSTML/Bob1.pdf

- Gaertner extended Welzl's method with a pivot step whose aim is to present points that
  are likely to be boundary points to the algorithm as fast as possible. his work can be
  fetched from:
    https://people.inf.ethz.ch/gaertner/subdir/texts/own_work/esa99_final.pdf

- Larsson presented the EPOS algorithm, which uses a first pass to project the points
  onto a set of vectors chosen to indicate the span of space, and use the extremal
  points so identified to generate a ball. the algorithm proceeds to test the generated
  ball against the rest of the points, expanding as necessary (a la Ritter). the paper,
  which includes an interesting comparison of algorithms, is available at:
    http://www.idt.mdh.se/~tla/publ/FastBoundingSpheres_Sigrad2008.pdf

- Kumar refined some earlier work by Badoiu that produces approximate balls, primarily
  intended for clustering. this works by testing subsets in an iterative fashion, adding
  the most extremal points into the tested subset at each iteration. these can be found,
  respectively, at:
    https://www2.cs.duke.edu/courses/spring07/cps296.2/papers/badoiu02approximate.pdf
    http://www.ams.sunysb.edu/~yildirim/meb.pdf

- Fischer documented a combinatorial algorithm that starts with a too large bound, and
  shrinks it. it maintains a QR decomposition, and handles many error conditions more
  gracefully than the alternatives (I need to prove that). it can be retrieved from:
    http://hbf.github.io/miniball/seb.pdf

we observe that the Welzl algorithm is generally efficient for 2 and 3 dimensions, but
the function that computes a ball from boundary points is an expensive step. the
algorithm attempts to minimize the impact of that expense by growing the ball as fast as
possible so that each subsequent point is more likely to already be contained and not
require a new ball to be generated. however, any approach that can prioritize extremal
points before invoking the algorithm will probably see a gain in performance.

