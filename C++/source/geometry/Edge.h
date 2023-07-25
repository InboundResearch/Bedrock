#pragma once

#include "Line.h"

template<typename Scalar, uint dimension>
class Edge {
    public:
        typedef Tuple<Scalar, dimension> Point;

        Point a;
        Point b;

        Edge (void) {}
        Edge (const Point& _a, const Point& _b) : a (_a), b (_b) {}

        Line<Scalar, dimension> getLine () const {
            return Line<Scalar, dimension>::fromTwoPoints(a, b);
        }
};

typedef Edge<f8, 2> Edge2;
typedef Edge<f8, 3> Edge3;

