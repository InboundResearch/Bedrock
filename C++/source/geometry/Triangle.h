#pragma once

#include "LineTest.h"

template<typename Scalar, uint dimension>
class Triangle {
    public:
        typedef Tuple<Scalar, dimension> Point;
        typedef Tuple<Scalar, dimension> Vector;

    private:
        Point a;
        Point b;
        Point c;

    public:
        Triangle (const Point* points) : a (points[0]), b (points[1]), c (points[2]) {}
        Triangle (const Point* points, uint _a, uint _b, uint _c) : a (points[_a]), b (points[_b]), c (points[_c]) {}
        Triangle (const Point* points, uint* index) : a (points[index[0]]), b (points[index[1]]), c (points[index[2]]) {}

        ENABLE_DIMENSION(2)
        Scalar area () const {
            return ((b - a) CROSS (c - a)) / 2;
        }

        ENABLE_DIMENSION(3)
        Scalar area () const {
            return ((b - a) CROSS (c - a)).length () / 2;
        }

        ENABLE_DIMENSION(2)
        bool contains (const Point& point, LineClass lineTest) const {
            return  (LineTest<Scalar>::classified (a, b, point) == lineTest) and
                    (LineTest<Scalar>::classified (b, c, point) == lineTest) and
                    (LineTest<Scalar>::classified (c, a, point) == lineTest);
        }
};
