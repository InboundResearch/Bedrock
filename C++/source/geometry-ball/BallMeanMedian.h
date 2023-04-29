#pragma once

#include "BoundingBall.h"
#include "BoundingBox.h"

template<typename Scalar, uint dimension>
static BoundingBall<Scalar, dimension> ballFromMean (const Tuple<Scalar, dimension>* points, uint pointCount) {
    Tuple<Scalar, dimension> mean;
    const Tuple<Scalar, dimension>* iter = points;
    const Tuple<Scalar, dimension>* end = points + pointCount;
    for (; iter != end; ++iter) {
        mean += *iter;
    }
    return BoundingBall<Scalar, dimension>::fromCenterAndPoints(mean / pointCount, points, pointCount);
}

template<typename Scalar, uint dimension>
static BoundingBall<Scalar, dimension> ballFromMedian (const Tuple<Scalar, dimension>* points, uint pointCount) {
    auto box = BoundingBox<Scalar, dimension>::fromPoints(points, pointCount);
    return BoundingBall<Scalar, dimension>::fromCenterAndPoints(box.getCenter (), points, pointCount);
}

