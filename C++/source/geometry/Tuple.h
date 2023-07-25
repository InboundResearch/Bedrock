#pragma once

#include "TupleHelper.h"
#include "Log.h"

enum Coordinate {
    X = 0, Y = 1, Z = 2, W = 3,
    U = 0, V = 1
};

#define DOT |
#define CROSS ^

#define ENABLE_DIMENSION(ddd) template <uint dim = dimension, typename enable_if<dim == ddd, void>::type* = nullptr>

template<typename Scalar, uint dimension>
class Tuple {
    protected:
        Scalar  value[dimension];
        static Scalar epsilon;

    public:
        static const Tuple ZERO;

        Tuple () {
            TupleHelper<Scalar, dimension>::fill(value, 0);
        }

        Tuple (Scalar fillValue) {
            TupleHelper<Scalar, dimension>::fill(value, fillValue);
        }

        ENABLE_DIMENSION(2)
        Tuple (Scalar x, Scalar y) {
            // the optimizer doesn't pass the parameters on the stack, so the too-clever by half
            // reverse-copy approach doesn't work in release build
            //TupleHelper<Scalar, dimension>::rcopy (value, &x);
            value[0] = x; value[1] = y;
        }

        ENABLE_DIMENSION(3)
        Tuple (Scalar x, Scalar y, Scalar z) {
            // the optimizer doesn't pass the parameters on the stack, so the too-clever by half
            // reverse-copy approach doesn't work in release build
            //TupleHelper<Scalar, dimension>::rcopy (value, &x);
            value[0] = x; value[1] = y; value[2] = z;
        }

        ENABLE_DIMENSION(4)
        Tuple (Scalar x, Scalar y, Scalar z, Scalar w) {
            // the optimizer doesn't pass the parameters on the stack, so the too-clever by half
            // reverse-copy approach doesn't work in release build
            //TupleHelper<Scalar, dimension>::rcopy (value, &x);
            value[0] = x; value[1] = y; value[2] = z; value[3] = w;
        }

        Tuple (Scalar* source) {
            TupleHelper<Scalar, dimension>::copy (value, source);
        }

        Tuple (const Tuple& source) {
            TupleHelper<Scalar, dimension>::copy (value, source.value);
        }

        Tuple (const Tuple<Scalar, dimension - 1>& source, Scalar fill = 0) {
            TupleHelper<Scalar, dimension - 1>::copy (value, *source);
            value[dimension - 1] = fill;
        }

        Tuple (const Tuple<Scalar, dimension + 1>& source) {
            // just drop the last value
            TupleHelper<Scalar, dimension>::copy (value, *source);
        }

        // accessors
        template<typename IndexType>
        Scalar& operator [] (IndexType index) {
            return value[index];
        }

        template<typename IndexType>
        const Scalar& operator [] (IndexType index) const {
            return value[index];
        }

        Scalar* operator * () {
            return value;
        }

        const Scalar* operator * () const {
            return value;
        }

        // 2 dimensional tuples have a perpendicular component...
        ENABLE_DIMENSION(2)
        Tuple<Scalar, dimension> perpendicular () {
            return Tuple<Scalar, dimension> { value[Y], -(value[X]) };
        }

        // add/subtract, multiply/divide by scalar, negate
        static Tuple& add (const Tuple& left, const Tuple& right, Tuple& result) {
            TupleHelper<Scalar, dimension>::add (left.value, right.value, result.value);
            return result;
        }

        static Tuple& subtract (const Tuple& left, const Tuple& right, Tuple& result) {
            TupleHelper<Scalar, dimension>::subtract (left.value, right.value, result.value);
            return result;
        }

        static Tuple& multiply (const Tuple& left, Scalar right, Tuple& result) {
            TupleHelper<Scalar, dimension>::multiply (left.value, right, result.value);
            return result;
        }

        static Tuple& divide (const Tuple& left, Scalar right, Tuple& result) {
            return multiply (left, 1 / right, result);;
        }

        static Tuple& negate (const Tuple& left, Tuple& result) {
            TupleHelper<Scalar, dimension>::multiply (left.value, -1, result.value);
            return result;
        }

        Tuple& operator += (const Tuple& right) {
            return static_cast<Tuple&> (add (*this, right, *this));
        }

        Tuple& operator -= (const Tuple& right) {
            return static_cast<Tuple&> (subtract (*this, right, *this));
        }

        Tuple& operator *= (Scalar right) {
            return static_cast<Tuple&> (multiply (*this, right, *this));
        }

        Tuple& operator /= (Scalar right) {
            return static_cast<Tuple&> (multiply (*this, 1 / right, *this));
        }

        Tuple operator + (const Tuple& right) const {
            Tuple result;
            return static_cast<Tuple&> (add (*this, right, result));
        }

        Tuple operator - (const Tuple& right) const {
            Tuple result;
            return static_cast<Tuple&> (subtract (*this, right, result));
        }

        Tuple operator * (Scalar right) const {
            Tuple result;
            return static_cast<Tuple&> (multiply (*this, right, result));
        }

        Tuple operator / (Scalar right) const {
            Tuple result;
            return static_cast<Tuple&> (multiply (*this, 1 / right, result));
        }

        Tuple operator - () const {
            Tuple result;
            return static_cast<Tuple&> (multiply (*this, -1, result));
        }

        Scalar operator DOT (const Tuple& right) const {
            return TupleHelper<Scalar, dimension>::sumMult (value, right.value);
        }

        // norms, measures, lengths
        // norm L-1, a.k.a. Manhattan distance
        Scalar normL1 () const {
            return TupleHelper<Scalar, dimension>::sumAbs (value);
        }

        // norm L-2, a.k.a. Euclidean distance, or length
        // NOTE: computing the L2 norm involves a square root, which can be computationally
        // expensive if you want to do a lot of them, so it's useful to return a squared version of
        // the value that can be used to reason about the result
        Scalar normL2Sq () const {
            return TupleHelper<Scalar, dimension>::sumSquare (value);
        }
        Scalar normL2 () const {
            return sqrt (normL2Sq ());
        }

        // the L2 norm is what most people think of when they use a norm as a unit of measure, so
        // we define the expected names to return the L2 norm
        Scalar normSq () const {
            return normL2Sq ();
        }

        Scalar norm () const {
            return normL2 ();
        }

        Scalar lengthSq () const {
            return normL2Sq ();
        }

        Scalar length () const {
            return normL2 ();
        }

        // the infinity norm, a.k.a. max norm
        Scalar normInf () const {
            return TupleHelper<Scalar, dimension>::max (value);
        }

        // the power norm general function, a.k.a. p-norm
        // see: https://en.wikipedia.org/wiki/Norm_(mathematics)
        Scalar normPower (double power) const {
            return pow (TupleHelper<Scalar, dimension>::sumPower (value, power), 1.0 / power);
        }

        // normalization
        static Tuple& normalize (const Tuple& source, Tuple& result) {
            return divide (source, source.norm (), result);
        }

        Tuple& normalize () {
            return static_cast<Tuple&> (normalize (*this, *this));
        }

        Tuple normalized () const {
            Tuple result;
            return static_cast<Tuple&> (normalize (*this, result));
        }

        bool isUnit () const {
            return (abs (norm () - 1.0) <= epsilon);
        }

        // there are multiple ways to do value comparison with tuples:
        // 1. exact value comparisons per component
        //
        // 2. range value comparisons per component, this is effectively a discretized
        //    value comparison, which checks whether the two tuples occupy a square region
        //    whose dimension is the epsilon (option 1 is a simplified version of this option where
        //    epsilon = 0)
        //
        // 3. subtract the vectors and take the L2 norm of the delta, which checks whether the two
        //    vectors occupy a circular region whose radius is the epsilon.
        //
        // epsilon options are good if we are operating in a fixed scale, and want to control
        // precision in a uniform way across the vector space, but aren't the best when we
        // are using vectors across multiple scales (i.e. the classic teapot in a football stadium
        // example). in order to use this approach to best effect, we would want to specify the
        // range of space we intend to represent, and then use precision = epsilon * range for
        // comparisons. we approximate this by exposing the epsilon value, but a fixed-point Scalar
        // type would actually be a more consistent solution.
        //
        // 4. subtract the vectors and compare the magnitude of the delta to 0 in ULPs - this
        //    allows for comparison over a wide range of scales, but the relative error is sort of
        //    a function of how many operations have been performed, so might become less useful in
        //    very complex operations. this is the most computationally intensive of the methods.
        //
        // so all of this leads back to which method should be used... we choose #3 because it's
        // probably going to behave the way users think it should, and if you use the ulps method
        // you will probably find that is is not a good general purpose solution because of
        // catastrophic cancellation errors when comparing numbers that are "near" 0. see the
        // discussion of ULP-based floating point comparisons in Math.h.

#ifndef TUPLE_COMPARISON_TYPE
#define TUPLE_COMPARISON_TYPE 3
#endif

        bool operator == (const Tuple& right) const {
#if TUPLE_COMPARISON_TYPE == 1
            return TupleHelper<Scalar, dimension>::compare (value, right.value);
#else
            Tuple delta;
            TupleHelper<Scalar, dimension>::subtract (value, right.value, delta.value);
#if TUPLE_COMPARISON_TYPE == 2
            return (TupleHelper<Scalar, dimension>::max (delta.value) <= epsilon);
#elif TUPLE_COMPARISON_TYPE == 3
            // use the L2 norm, but skip the square root
            return (TupleHelper<Scalar, dimension>::sumSquare (delta.value) <= (epsilon * epsilon));
#elif TUPLE_COMPARISON_TYPE == 4
            return ulpEquals (delta.norm (), 0);
#endif
#endif
        }

        bool operator != (const Tuple& right) const {
            return not operator == (right);
        }

        // epsilon
        static Scalar getEpsilon () {
            return epsilon;
        }

        static void setEpsilon (Scalar eps) {
            epsilon = eps;
            Log::debug () << "Tuple(" << dimension << "): Set epsilon: " << epsilon << endl;
        }

        // a scope-based helper for setting epsilon
        class Scope {
            private:
                Scalar oldEpsilon;

            public:
                Scope () : oldEpsilon (Tuple::epsilon) { }

                Scope (Scalar newEpsilon) : oldEpsilon (Tuple::epsilon) {
                   Tuple::setEpsilon (newEpsilon);
                }

                ~Scope () {
                    Tuple::setEpsilon (oldEpsilon);
                 }
        };

        Coordinate maxCoordinate () const {
            uint maxCoordinate = 0;
            Scalar maxValue = abs (value[maxCoordinate]);
            for (uint coordinate = 1; coordinate < dimension; ++coordinate) {
                Scalar newValue = abs (value[coordinate]);
                if (newValue > maxValue) {
                    maxCoordinate = coordinate;
                    maxValue = newValue;
                }
            }
            return static_cast<Coordinate> (maxCoordinate);
        }
};

template<typename Scalar, uint dimension>
const Tuple<Scalar, dimension> Tuple<Scalar, dimension>::ZERO (0.0);

// tuple multiplication is commutative, so we implement (left * right) by calling (right * left)
template<typename LeftType, typename Scalar, uint dimension>
Tuple<Scalar, dimension> operator * (LeftType left, const Tuple<Scalar, dimension>& right) {
    return right * Scalar (left);
}

// ostream writer...
template<typename Scalar, uint dimension>
ostream& operator << (ostream& stream, const Tuple<Scalar, dimension>& tuple) {
    const char* spacer = "(";
    for (uint i = 0; i < dimension; ++i) {
        stream << spacer << tuple[static_cast<Coordinate> (i)];
        spacer = ", ";
    }
    return stream << ")";
}

// some tuples add a cross product, in 2D it's the magnitude of the vertical vector that would
// result from a 3D operation if the Z components were 0. In 3D, it's a vector that is
// perpendicular to the two provided vectors, whose magnitude is the area of the parallelogram
// defined by the 3 points.
// see: https://en.wikipedia.org/wiki/Cross_product
template<typename Scalar>
static inline
Scalar operator CROSS (const Tuple<Scalar, 2>& left, const Tuple<Scalar, 2>& right)  {
    return (left[X] * right[Y]) - (left[Y] * right[X]);
}

template<typename Scalar>
static inline
Tuple<Scalar, 3> operator CROSS (const Tuple<Scalar, 3>& left, const Tuple<Scalar, 3>& right) {
    return Tuple<Scalar, 3> { (left[Y] * right[Z]) - (left[Z] * right[Y]),
                              (left[Z] * right[X]) - (left[X] * right[Z]),
                              (left[X] * right[Y]) - (left[Y] * right[X]) };
}

typedef Tuple<f8, 2> Point2;
typedef Tuple<f8, 3> Point3;
typedef Tuple<f8, 4> Point4;

typedef Tuple<f8, 2> Vector2;
typedef Tuple<f8, 3> Vector3;
typedef Tuple<f8, 4> Vector4;
