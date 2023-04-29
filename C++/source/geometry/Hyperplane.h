#pragma once

#include "Line.h"

template<typename Scalar, uint dimension>
class Hyperplane {
    public:
        typedef Tuple<Scalar, dimension> Point;
        typedef Tuple<Scalar, dimension> Vector;

    private:
        Vector normal;
        Scalar C;

    public:
        Hyperplane (const Vector& _normal, Scalar _C) : normal (_normal), C (_C) {}

        static Hyperplane<Scalar, dimension> fromPointNormal (const Point& point, const Vector& normal) {
            Scalar C = -(point DOT normal);
            return Hyperplane<Scalar, dimension> (normal, C);
        }

        ENABLE_DIMENSION(2)
        static Hyperplane<Scalar, dimension> fromPoints (const Point& a, const Point& b) {
            Vector normal =  (b - a).normalized ().perpendicular ();
            return fromPointNormal (a, normal);
        }

        ENABLE_DIMENSION(3)
        static Hyperplane<Scalar, dimension> fromPoints (const Point& a, const Point& b, const Point& c) {
            Vector normal = ((b - a) CROSS (c - a)).normalized ();
            return fromPointNormal (a, normal);
        }

        // return the scalar perpendicular distance from a point to the hyperplane (along the normal vector)
        Scalar distance (const Point& point) const {
            return (point DOT normal) + C;
        }

        // check if a point is on the hyperplane
        bool isOn (const Point& point) const {
            return distance (point) <= Point::getEpsilon ();
        }

        // return the normal vector
        const Vector& getNormal () const {
            return normal;
        }

        // return the C component of the hyperplane equation
        Scalar getC () const {
            return C;
        }

        // return a point that is on the hyperplane
        Point getPoint () const {
            return normal * -C;
        }

        // return a representation of the hyperplane as a d+1 vector
        Tuple<Scalar, dimension + 1> getHyperplane () const {
            return Tuple<Scalar, dimension + 1> (normal, C);
        }

        // return the time, t, along the line, when it will intersect the hyperplane
        Scalar intersect (const Line<Scalar, dimension>& line) const {
            // compute the cosine of the angle between the line and the hyperplane, then check to
            // see if that angle is ever going to reach the hyperplane (i.e. it's not effectively
            // parallel to the hyperplane). a parallel line get's an infinite time. otherwise,
            // compute the time along the line where the intersection will occur.
            Scalar cosTheta = -(line.getDirection () DOT normal);
            return (abs (cosTheta) < Vector::getEpsilon ()) ? numeric_limits<Scalar>::infinity () : distance (line.getOrigin ()) / cosTheta;
        }
};

typedef Hyperplane<f8, 2> Hyperplane2;
typedef Hyperplane<f8, 3> Hyperplane3;
