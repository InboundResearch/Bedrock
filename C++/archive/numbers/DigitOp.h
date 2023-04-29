#pragma once

#include "Common.h"

// DigitOp is a helper class for working with arrays of "digits", as part of an implementation of a
// big-integer class. arrays of digits are organized in Least-Significant-Digit first fashion, so
// digit 0 is the LSD - this matches the mathematical operations of all the processors we target,
// but can be a bit confusing as numbers are written right to left instead of left to right.

template<typename DigitType, int N>
struct  DigitOp {
    // basic operations on a single array of digits

    // numbers in digit arrays may have leading zeroes, this method returns the number of
    // significant (non-zero-leading) digits in the number, where 0 means no digits are
    // non-zero. this is used extensively in framing the calculations between two numbers
    static uint top (const DigitType* a) {
        return a[N - 1] ? N : DigitOp<DigitType, N - 1>::top (a);
    }

    // set all the digits of an array to a source value
    static void set (DigitType* dst, DigitType src) {
        *dst++ = src;
        DigitOp<DigitType, N - 1>::set (dst, src);
    }

    // return true if any digit in the array is non-zero
    static bool test (const DigitType* a) {
        return a[N - 1] ? true : DigitOp<DigitType, N - 1>::test (a);
    }

    // basic operations on two arrays of digits

    // copy one digit array to another
    static void copy (DigitType* dst, const DigitType* src) {
        *dst++ = *src++;
        DigitOp<DigitType, N - 1>::copy (dst, src);
    }

    // compare two digit arrays, returning a signum (-1, 0, 1) if a <, == , or > b, respectively
    static int compare (const DigitType* a, const DigitType* b) {
        return (a[N - 1] > b[N - 1]) ? 1 : ((a[N - 1] < b[N - 1]) ? -1 : DigitOp<DigitType, N - 1>::compare (a, b));
    }

    // operations with an array of digits and a single digit

    // shift the entire array one digit to the left
    static DigitType digitShiftLeft (const DigitType* a, DigitType* result, DigitType fill = 0) {
        DigitType r = a[N - 1];
        result[N - 1] = DigitOp<DigitType, N - 1>::digitShiftLeft (a, result, fill);
        return r;
    }

    // shift the entire array one digit to the right
    static DigitType digitShiftRight (const DigitType* a, DigitType* result, DigitType fill = 0) {
        DigitType r = *a;
        *result = DigitOp<DigitType, N - 1>::digitShiftRight (a + 1, result + 1, fill);
        return r;
    }

    // compare a digit array against a single digit, returning a signum (-1, 0, 1) if a <, == , or > b, respectively
    static int compareDigit (const DigitType* a, DigitType b) {
        return a[N - 1] ? 1 : DigitOp<DigitType, N - 1>::compareDigit (a, b);
    }

    static void bitAnd (const DigitType* a, const DigitType* b, DigitType* result) {
        result[N - 1] = a[N - 1] bit_and b[N - 1];
        DigitOp<DigitType, N - 1>::bitAnd (a, b, result);
    }

    static void bitOr (const DigitType* a, const DigitType* b, DigitType* result) {
        result[N - 1] = a[N - 1] bit_or b[N - 1];
        DigitOp<DigitType, N - 1>::bitOr (a, b, result);
    }

    static void bitXor (const DigitType* a, const DigitType* b, DigitType* result) {
        result[N - 1] = a[N - 1] ^ b[N - 1];
        DigitOp<DigitType, N - 1>::bitXor (a, b, result);
    }

    static void bitComplement (const DigitType* a, DigitType* result) {
        result[N - 1] = complement a[N - 1];
        DigitOp<DigitType, N - 1>::bitComplement (a, result);
    }
};

// specialized for N = 0
template<typename DigitType>
struct  DigitOp<DigitType, 0> {
    // basic operations on a single array of digits
    static uint top (const DigitType* a) { return 0; }
    static void set (DigitType* dst, DigitType src) {}
    static bool test (const DigitType* a) { return false; }

    // basic operations on two arrays of digits
    static void copy (DigitType* dst, const DigitType* src) {}
    static int compare (const DigitType* a, const DigitType* b) { return 0; }

    // operations with an array of digits and a single digit
    static DigitType digitShiftLeft (const DigitType* a, DigitType* result, DigitType fill = 0) { return fill; }
    static DigitType digitShiftRight (const DigitType* a, DigitType* result, DigitType fill = 0) { return fill; }
    static int compareDigit (const DigitType* a, DigitType b) { return (*a > b) ? 1 : ((*a < b) ? -1 : 0); }

    // bit operations on a single array of digits
    static void bitAnd (const DigitType* a, const DigitType* b, DigitType* result) {}
    static void bitOr (const DigitType* a, const DigitType* b, DigitType* result) {}
    static void bitXor (const DigitType* a, const DigitType* b, DigitType* result) {}
    static void bitComplement (const DigitType* a, DigitType* result) {}
};
