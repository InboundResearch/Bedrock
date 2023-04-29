#pragma once

#include "Line.h"

template<typename Scalar>
class Line2 {
    public:
        typedef Tuple<Scalar, 2> Point;
        typedef Tuple<Scalar, 2> Vector;

    public:
        static Line<Scalar, 2> fromSlopeIntercept (Scalar slope, Scalar yIntercept) {
            // compute two points on this line (y = mx + b)
            Point a (-10, (slope * -10) + yIntercept);
            Point b (10, (slope * 10) + yIntercept);
            return Line<Scalar, 2>::fromPoints (a, b);
        }

        static Line<Scalar, 2> fitPoints (const vector<Point>& points) {
            Scalar intercept = 0;
            // compute the mean point
            Point mean;
            for (typename vector<Point >::const_iterator iter = points.begin (); iter != points.end; ++iter) {
                mean += *iter;
            }
            mean /= points.size();

            // make a second pass to compute the sums needed to compute the slope
            Scalar numerator = 0;
            Scalar denominator = 0;
            for (typename vector<Point>::const_iterator iter = points.begin (); iter != points.end; ++iter) {
                Vector delta = *iter - mean;
                numerator += delta[X] * delta[Y];
                denominator += delta[X] * delta[X];
            }

            // compute the slope, and then the intercept
            if (denominator != 0) {
                Scalar slope = numerator / denominator;
                Scalar intercept = mean[Y] - (slope * mean[X]);
                return fromSlopeIntercept (slope, intercept);
            }
            return Line<Scalar> (Tuple<Scalar, 2> (1, 0), -mean[X]);
        }
};
