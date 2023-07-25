#pragma once

#include "Common.h"

template<typename Scalar, unsigned int size>
class TupleHelper {
    public:
        static void fill (Scalar* destination, Scalar fillValue) {
            *destination = fillValue;
            TupleHelper<Scalar, size - 1>::fill (destination + 1, fillValue);
        }

        static void copy (Scalar* destination, const Scalar* source) {
            *destination = *source;
            TupleHelper<Scalar, size - 1>::copy (destination + 1, source + 1);
        }

        static void rcopy (Scalar* destination, const Scalar* source) {
            *destination = *source;
            TupleHelper<Scalar, size - 1>::rcopy (destination + 1, source - 1);
        }

        static void add (const Scalar* left, const Scalar* right, Scalar* result) {
            *result = *left + *right;
            TupleHelper<Scalar, size - 1>::add (left + 1, right + 1, result + 1);
        }

        static void subtract (const Scalar* left, const Scalar* right, Scalar* result) {
            *result = *left - *right;
            TupleHelper<Scalar, size - 1>::subtract (left + 1, right + 1, result + 1);
        }

        static void multiply (const Scalar* left, Scalar right, Scalar* result) {
            *result = *left * right;
            TupleHelper<Scalar, size - 1>::multiply (left + 1, right, result + 1);
        }

        static void divide (const Scalar* left, Scalar right, Scalar* result) {
            *result = *left / right;
            TupleHelper<Scalar, size - 1>::divide (left + 1, right, result + 1);
        }

        static bool compare (const Scalar* left, const Scalar* right) {
            return (*left == *right) ? TupleHelper<Scalar, size - 1>::compare (left + 1, right + 1) : false;
        }

        static Scalar sumAbs (const Scalar* source) {
            return abs (*source) + TupleHelper<Scalar, size - 1>::sumAbs (source + 1);
        }

        static Scalar sumSquare (const Scalar* source) {
            return (*source * *source) + TupleHelper<Scalar, size - 1>::sumSquare (source + 1);
        }

        static Scalar sumMult (const Scalar* left, const Scalar* right) {
            return (*left * *right) + TupleHelper<Scalar, size - 1>::sumMult (left + 1, right + 1);
        }

        static Scalar sumPower (const Scalar* source, Scalar power) {
            return pow (*source, power) + TupleHelper<Scalar, size - 1>::sumPower (source + 1, power);
        }

        static Scalar max (const Scalar* source) {
            return std::max (abs (*source), TupleHelper<Scalar, size - 1>::max (source + 1));
        }

        static bool inBound (const Scalar* source, const Scalar* low, const Scalar* high) {
            return (*source >= *low) and (*source <= *high) and TupleHelper<Scalar, size - 1>::inBound (source + 1, low + 1, high + 1);
        }

        static void updateLowHigh (const Scalar* source, Scalar* low, Scalar* high) {
            *low = std::min (*source, *low);
            *high = std::max (*source, *high);
            TupleHelper<Scalar, size - 1>::updateLowHigh (source + 1, low + 1, high + 1);
        }
};

template<typename Scalar>
class TupleHelper<Scalar, 0> {
    public:
        static void fill (Scalar* destination, Scalar fillValue) {}
        static void copy (Scalar* destination, const Scalar* source) {}
        static void rcopy (Scalar* destination, const Scalar* source) {}
        static void add (const Scalar* left, const Scalar* right, Scalar* result) {}
        static void subtract (const Scalar* left, const Scalar* right, Scalar* result) {}
        static void multiply (const Scalar* left, Scalar right, Scalar* result) {}
        static void divide (const Scalar* left, Scalar right, Scalar* result) {}
        static bool compare (const Scalar* left, const Scalar* right) { return true; }
        static Scalar sumAbs (const Scalar* source) { return 0; }
        static Scalar sumSquare (const Scalar* source) { return 0; }
        static Scalar sumMult (const Scalar* left, const Scalar* right) { return 0; }
        static double sumPower (const Scalar* source, Scalar power) { return 0; }
        static Scalar max (const Scalar* source) { return numeric_limits<Scalar>::lowest (); }
        static bool inBound (const Scalar* source, const Scalar* low, const Scalar* high) { return true; }
        static void updateLowHigh (const Scalar* source, Scalar* low, Scalar* high) {}
};

