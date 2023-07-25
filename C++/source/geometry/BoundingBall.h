#pragma once

#include "Tuple.h"

template<typename Scalar, uint dimension>
class BoundingBall {
    public:
        typedef Tuple<Scalar, dimension> Point;
        typedef Tuple<Scalar, dimension> Vector;

    private:
        Point center;
        Scalar squaredRadius;

        BoundingBall (const Point& _center, Scalar _squaredRadius) : center (_center), squaredRadius (_squaredRadius + Point::getEpsilon()) {}

        BoundingBall& addPoints (const Point* points, uint pointCount) {
            squaredRadius = -1;
            for (uint i = 0; i < pointCount; ++i) {
                Scalar squaredDelta = (points[i] - center).lengthSq ();
                if (squaredDelta > squaredRadius) {
                    squaredRadius = squaredDelta;
                }
            }
            return *this;
        }

    public:
        BoundingBall () : squaredRadius (-1) {}
        BoundingBall (const Point& _center) : center (_center), squaredRadius (0) {}

        // these are here to differentiate the constructors from a point and scalar
        static BoundingBall fromCenterRadius (const Point& center, Scalar radius) {
            return BoundingBall (center, radius * radius);
        }

        static BoundingBall fromCenterSquaredRadius (const Point& center, Scalar squaredRadius) {
            return BoundingBall (center, squaredRadius);
        }

        // in any n-dimension space, 2 points always define a ball with the center at the middle of
        // the line segment between them.
        static BoundingBall fromTwoPoints (const Point& a, const Point& b) {
            Point center = (a + b) / 2;
            return BoundingBall (center, (a - center).lengthSq ());
        }

        // build an n-dimension bounding ball from k points known to be on the boundary, where k in
        // the range [0..n+1].
        static BoundingBall fromBoundaryPoints (const Point* points, uint pointCount) {
            BoundingBall ball;
            fromBoundaryPoints (points, pointCount, ball);
            return ball;
        }

        // this is an implementation of the incremental solution to the k-dimensional minimum ball
        // with points on the boundary described in the Welzl and Gaertner papers, but we drive it
        // non-iteratively. the best discussion is Section 4 "The implementation" in Gaertner's
        // "Fast and Robust Smallest Enclosing Balls", which is available at:
        //   https://people.inf.ethz.ch/gaertner/subdir/texts/own_work/esa99_final.pdf
        static bool fromBoundaryPoints (const Point* boundaryPoints, uint boundaryPointCount, BoundingBall& ball) {
            Scalar epsilon = square<Scalar>(Point::getEpsilon());
            if (boundaryPointCount > 0) {
                Vector v[dimension + 1];
                Scalar z[dimension + 1];

                Scalar squaredRadius = 0;
                Point  center = boundaryPoints[0];

                for (uint current = 1; current < boundaryPointCount; ++current) {
                    // v_current is the vector delta between the current point and the first center
                    v[current] = boundaryPoints[current] - boundaryPoints[0];

                    // setup for the second half of equation 11
                    Scalar a[dimension + 1];
                    for (uint i = 1; i < current; ++i) {
                        a[i] = (v[i] DOT v[current]) * (2 / z[i]);
                    }

                    // update v_current to Q_current - Qbar_current, see discussion of equation 9
                    for (uint i = 1; i < current; ++i) {
                        v[current] -= a[i] * v[i];
                    }

                    // compute z_current, lemma 1 (iii), twice the distance from Qm to its
                    // projection, and check if it is not too small
                    z[current] = v[current].lengthSq () * 2;
                    if (z[current] < (epsilon * squaredRadius)) {
                        // see discussion around equation 12. skip if z is too small, because in
                        // the next step, we divide by z. if z is a very small value, that will
                        // result in numeric instability. this could happen if two points on the
                        // boundary are very close to each other (or the same point repeated). we
                        // effectively treat the new point as if it is inside the ball we have,
                        // and accept the possible error.
                        Log::warn () << "z < epsilon" << endl;
                        return false;
                    } else {
                        // equation 9/10
                        Scalar e = (boundaryPoints[current] - center).lengthSq () - squaredRadius;
                        center += (e / z[current]) * v[current];
                        squaredRadius += (e * e) / (2 * z[current]);
                    }
                }
                ball = BoundingBall (center, squaredRadius);
            }
            return true;
        }

        // sometimes, you want to fix the center and then just add all the points - for instance
        // when bounding a physical object whose center of gravity is known, but which would not be
        // the same center as a geometric center
        static BoundingBall fromCenterAndPoints (const Point& center, const Point* points, uint pointCount) {
            return BoundingBall (center).addPoints(points, pointCount);
        }

        // see geometry-ball/readme.md for a discussion of the various algorithms available to
        // compute a bounding ball for an arbitrary set of points.

        bool contains (const Point&point) const {
            return (point - center).lengthSq () <= squaredRadius;
        }

        bool isEmpty () const {
            return squaredRadius < 0;
        }

        const Point& getCenter () const {
            return center;
        }

        Scalar getSquaredRadius () const {
            return  squaredRadius;
        }

        Scalar getRadius () const {
            // XXX
            return  (squaredRadius >= 0) ? sqrt (squaredRadius - Point::getEpsilon()) : -1;
        }
};

// ostream writer...
template<typename Scalar, uint dimension>
ostream& operator << (ostream& stream, const BoundingBall<Scalar, dimension>& ball) {
    return stream << "center: " << ball.getCenter() << ", radius: " << ball.getRadius();
}

typedef BoundingBall<f8, 2> BoundingBall2;
typedef BoundingBall<f8, 3> BoundingBall3;
