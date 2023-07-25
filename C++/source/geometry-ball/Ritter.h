#pragma once

#include "BoundingBall.h"

template<typename Scalar, uint dimension>
class Ritter {
    public:
        typedef Tuple<Scalar, dimension> Point;
        typedef Tuple<Scalar, dimension> Vector;
        typedef BoundingBall<Scalar, dimension> Ball;

    private:


    public:
        static Ball fromPoints (const Point* points, uint pointCount) {
            // XXX TODO
            return Ball ();
        }
};
