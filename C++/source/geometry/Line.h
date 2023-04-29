#pragma once

#include "Tuple.h"

template<typename Scalar, unsigned int dimension>
class Line {
    public:
        typedef Tuple<Scalar, dimension> Point;
        typedef Tuple<Scalar, dimension> Vector;

    private:
        Point origin;
        Tuple<Scalar, dimension> direction;

    public:
        Line (const Point& _origin, const Tuple<Scalar, dimension>& _direction) : origin (_origin), direction (_direction) {}

        static Line fromTwoPoints (const Point& a, const Point& b) {
            return Line<Scalar, dimension> (a, (b - a).normalized ());
        }

        // return the shortest scalar distance from a point to the line (along a perpendicular vector)
        ENABLE_DIMENSION(2)
        Scalar distance (const Point& point) const {
            return direction CROSS (origin - point);
        }

        // return the shortest scalar distance from a point to the line (along a perpendicular vector)
        ENABLE_DIMENSION(3)
        Scalar distance (const Point& point) const {
            return (direction CROSS (origin - point)).norm ();
        }

        bool isOnLine (const Point& point) const {
            return distance (point) <= Point::getEpsilon ();
        }

        const Point& getOrigin () const {
            return origin;
        }

        const Vector& getDirection () const {
            return direction;
        }

        Point pointAt (Scalar t) {
            return origin + (direction * t);
        }
};

typedef Line<f8, 2> Line2;
typedef Line<f8, 3> Line3;

