#pragma once

#include "Tuple.h"

template<typename Scalar, uint dimension>
class BoundingBox {
    public:
        typedef Tuple<Scalar, dimension> Point;
        typedef Tuple<Scalar, dimension> Vector;

    private:
        Point low;
        Point high;

    public:
        BoundingBox () : low (numeric_limits<Scalar>::max ()), high (numeric_limits<Scalar>::lowest ()) {}

        BoundingBox (const Point* points, uint pointCount) : low (numeric_limits<Scalar>::max ()), high (numeric_limits<Scalar>::lowest ()) {
            addPoints (points, pointCount);
        }

        BoundingBox& addPoint (const Point& point) {
            TupleHelper<Scalar, dimension>::updateLowHigh (*point, *low, *high);
            return *this;
        }

        BoundingBox& addPoints (const Point* points, uint pointCount) {
            for (uint i = 0; i < pointCount; ++i) {
                addPoint (points[i]);
            }
            return *this;
        }

        static BoundingBox fromPoints (const Point* points, uint pointCount) {
            return BoundingBox ().addPoints(points, pointCount);
        }

        Vector getSpan () const {
            return high- low;
        }

        bool contains (const Point&point) const {
            return TupleHelper<Scalar, dimension>::inBound(*point, *low, *high);
        }

        bool isEmpty () const {
            Vector span = getSpan ();
            return (span[X] < 0) or (span[Y] < 0);
        }

        const Point& getLow () const {
            return low;
        }

        const Point& getHigh () const {
            return high;
        }

        Point getCenter () const {
            return (low + high) / 2;
        }
};

// ostream writer...
template<typename Scalar, uint dimension>
ostream& operator << (ostream& stream, const BoundingBox<Scalar, dimension>& box) {
    return stream << "low: " << box.getLow() << ", high: " <<box.getHigh();
}

typedef BoundingBox<f8, 2> BoundingBox2;
typedef BoundingBox<f8, 3> BoundingBox3;
