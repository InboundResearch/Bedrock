#pragma once

#include "BoundingBall.h"
#include "Hyperplane.h"

template<typename Scalar>
static BoundingBall<Scalar, 2> BallFromThreePoints (const Tuple<Scalar, 2>& a, const Tuple<Scalar, 2>& b, const Tuple<Scalar, 2>& c) {
    // define a hyperplane using the midpoint and direction of a-b, and a line using
    // the midpoint of b-c and it's perpendicular direction
    auto hyperplane = Hyperplane<Scalar, 2>::fromPointNormal ((a + b) / 2, b - a);
    Line<Scalar, 2> line ((b + c) / 2, (c - b).perpendicular ());

    // compute the intersection of the line with the plane, and Bob's your uncle
    // see - https://en.wikipedia.org/wiki/Bob%27s_your_uncle
    f8 t = hyperplane.intersect (line);
    if (not std::isinf (t)) {
        Tuple<Scalar, 2> center = line.pointAt (t);
        return BoundingBall<Scalar, 2>::fromCenterSquaredRadius(center, (a - center).lengthSq ());
    } else {
        // the points are collinear, or two of the points are equivalent
        // XXX do some work to figure out which point is collinear and exlude it
    }
    return BoundingBall<Scalar, 2> ();
}

