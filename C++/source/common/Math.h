#pragma once

// signum
template <typename NumberType> inline constexpr
int signum (NumberType value, false_type is_signed) {
    return (NumberType(0) < value);
}

template <typename NumberType> inline constexpr
int signum (NumberType value, true_type is_signed) {
    return (NumberType (0) < value) - (value < NumberType (0));
}

template <typename NumberType> inline constexpr
int signum (NumberType value) {
    return signum(value, is_signed<NumberType>());
}

// saturate
template <typename NumberType>
NumberType saturate (NumberType value) {
    return (abs (value) > 0.5) ? NumberType (signum (value)) : 0;
}

template <typename NumberType>
NumberType square (NumberType value) {
    return value * value;
}

// floating point comparisons
// references:
//   https://en.wikipedia.org/wiki/Machine_epsilon
//   https://en.wikipedia.org/wiki/Unit_in_the_last_place
//   https://people.eecs.berkeley.edu/~wkahan/LOG10HAF.TXT
//   https://floating-point-gui.de/errors/comparison/
//   https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
//   https://en.wikipedia.org/wiki/Loss_of_significance
// we default to 4 bits of precision in comparison
template<typename FType, typename SType>
union FloatUlp {
        FType real;
        SType integer;
        FloatUlp (FType _real) : real (_real) {}
        bool isNegative () const { return integer < 0; }
};

template<typename FType, typename SType>
SType ulpDifference (FType left, FType right) {
    FloatUlp<FType, SType> floatUlpLeft (left);
    FloatUlp<FType, SType> floatUlpRight (right);
    return abs (floatUlpLeft.integer - floatUlpRight.integer);
}

static inline
bool ulpEquals (f8 left, f8 right, s4 bitsOfprecision = 4) {
    return ulpDifference<f8, s8> (left, right) <= ((s8 (1) << bitsOfprecision) - 1);
}

static inline
bool ulpEquals (f4 left, f4 right, s4 bitsOfprecision = 4) {
    return ulpDifference<f4, s4> (left, right) <= ((s4 (1) << bitsOfprecision) - 1);
}

