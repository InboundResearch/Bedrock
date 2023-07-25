#pragma once

#include "Edge.h"

// line tests return the area of the parallelogram subtended by the three points, which has a lot
// of applications in computational geometry. a positive value indicates a left turning sequence,
// and a negative value indicates a right turning sequence.

enum class LineClass {
    TURNS_LEFT =    1,
    TURNS_RIGHT =   -1,
    SPANNING =      TURNS_LEFT * TURNS_RIGHT,
    COLINEAR =      0
};

#define STREAM_OUT_ENUM(x) case LineClass::x: stream << #x; break;
static inline
ostream& operator << (ostream& stream, LineClass lineClass) {
    switch (lineClass) {
        STREAM_OUT_ENUM(TURNS_LEFT);
        STREAM_OUT_ENUM(TURNS_RIGHT);
        STREAM_OUT_ENUM(COLINEAR);
    }
    return stream;
}
#undef STREAM_OUT_ENUM

template<typename Scalar>
class LineTest {
    public:
        typedef Tuple<Scalar, 2> Point;

        static LineClass classify (Scalar lineTest) {
            Scalar epsilon = Point::getEpsilon();
            return  (lineTest > epsilon) ? LineClass::TURNS_LEFT :
                    (lineTest < -epsilon) ? LineClass::TURNS_RIGHT :
                    LineClass::COLINEAR;
        }

        static Scalar raw (const Point& a, const Point& b, const Point& c) {
            return (b - a) CROSS (c - a);
        }

        static Scalar normalizedRaw (const Point& a, const Point& b, const Point& c) {
            return ((b - a).normalized ()) CROSS ((c - a).normalized ());
        }

        static LineClass classified (const Point& a, const Point& b, const Point& c) {
            return classify (raw (a, b, c));
        }

        static LineClass classified (const Edge<Scalar, 2>& edge, Point& c) {
            return classified (edge.a, edge.b, c);
        }
};

typedef LineTest<f8> LineTest2;
