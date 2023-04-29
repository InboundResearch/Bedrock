#pragma once

#include "Common.h"

// TwoDigitOp is like DigitOp, but it needs to know about a TwoDigit type so it can contain values
// that might overflow a single digit type

#define DIGIT_BITS (sizeof (DigitType) * 8)
#define Q ((N - M) - 1)

template<typename DigitType, typename TwoDigitType, int N, int M = 0>
struct  TwoDigitOp {
    // basic operations on two arrays of digits

    // add two arrays of digits, returning the carry
    static DigitType add (const DigitType* a, const DigitType* b, DigitType* result, DigitType carry) {
        TwoDigitType	r = a[M] + b[M] + carry;
        *result = DigitType (r);
        return TwoDigitOp<DigitType, TwoDigitType, N - 1>::add (a, b, result + 1, r >> DIGIT_BITS);
    }

    // subtract two arrays of digits, returning the borrow
    static DigitType subtract (const DigitType* a, const DigitType* b, DigitType* result, DigitType borrow) {
        TwoDigitType	r = *a - *b - borrow;
        *result = DigitType (r);
        return TwoDigitOp<DigitType, TwoDigitType, N - 1>::subtract (a + 1, b + 1, result + 1, (r bit_and (TwoDigitType (0x01) << DIGIT_BITS)) >> DIGIT_BITS);
    }

    // operations with an array of digits and a single digit

    // add a single digit into a digit array, return the carry
    static DigitType addDigit (const DigitType* a, DigitType b, DigitType* result, DigitType carry) {
        TwoDigitType	r = *a + b + carry;
        *result = DigitType (r);
        return TwoDigitOp<DigitType, TwoDigitType, N - 1>::addDigit (a + 1, 0, result + 1, r >> DIGIT_BITS);
    }

    // subtract a single digit from a digit array, return the borrow
    static DigitType subtractDigit (const DigitType* a, DigitType b, DigitType* result, DigitType borrow) {
        TwoDigitType	r = *a - b - borrow;
        *result = DigitType (r);
        return TwoDigitOp<DigitType, TwoDigitType, N - 1>::subtractDigit (a + 1, 0, result + 1, (r bit_and (TwoDigitType (0x01) << DIGIT_BITS)) >> DIGIT_BITS);
    }

    // multiply a digit array by a single digit, return the carry
    static DigitType multiplyDigit (const DigitType* a, DigitType b, DigitType* result, DigitType carry) {
        TwoDigitType	r = (*a * b) + carry;
        *result = DigitType (r);
        return TwoDigitOp<DigitType, TwoDigitType, N - 1>::multiplyDigit (a + 1, b, result + 1, r >> DIGIT_BITS);
    }

    // divide a digit array by a single digit, return the remainder
    static DigitType divideDigit (const DigitType* a, DigitType b, DigitType* result, DigitType remainder) {
        TwoDigitType	r = (TwoDigitType (remainder) << DIGIT_BITS) + a[N - 1];
        result[N - 1] = DigitType (r / b);
        return TwoDigitOp<DigitType, TwoDigitType, N - 1>::divideDigit (a, b, result, DigitType (r % b));
    }

    // multiply a digit array by a single digit, and add the product to another digit array, return
    // the carry
    static DigitType multiplyDigitAdd (const DigitType* a, const DigitType* b, DigitType c, DigitType* result, DigitType carry) {
        TwoDigitType	r = *a + (*b * c) + carry;
        *result = DigitType (r);
        return TwoDigitOp<DigitType, TwoDigitType, N - 1>::multiplyDigitAdd (a + 1, b + 1, c, result + 1, r >> DIGIT_BITS);
    }

    // bit operations on a single array of digits

    static DigitType bitShiftLeft (const DigitType* a, DigitType bits, DigitType* result){
        TwoDigitType r = a[N - 1] << bits;
        result[N - 1] = DigitType (r) bit_or TwoDigitOp<DigitType, TwoDigitType, N - 1>::bitShiftLeft (a, bits, result);
        return DigitType (r >> DIGIT_BITS);
    }

    static DigitType bitShiftRight (const DigitType* a, DigitType bits, DigitType* result) {
        TwoDigitType r = *a;
        *result = DigitType (r >> bits) bit_or TwoDigitOp<DigitType, TwoDigitType, N - 1>::bitShiftRight (a + 1, bits, result + 1);
        return DigitType ((r << DIGIT_BITS) >> bits);
    }
};

// specialized for N = 0
template<typename DigitType, typename TwoDigitType, int N>
struct  TwoDigitOp<DigitType, TwoDigitType, N, N> {
    // basic operations on two arrays of digits
    static DigitType add (const DigitType* a, const DigitType* b, DigitType* result, DigitType carry) { return carry; }
    static DigitType subtract (const DigitType* a, const DigitType* b, DigitType* result, DigitType borrow) { return borrow; }

    // operations with an array of digits and a single digit
    static DigitType addDigit (const DigitType* a, DigitType b, DigitType* result, DigitType carry) { return carry; }
    static DigitType subtractDigit (const DigitType* a, DigitType b, DigitType* result, DigitType borrow) { return borrow; }
    static DigitType multiplyDigit (const DigitType* a, DigitType b, DigitType* result, DigitType carry) { return carry; }
    static DigitType divideDigit (const DigitType* a, DigitType b, DigitType* result, DigitType remainder) { return remainder; }
    static DigitType multiplyDigitAdd (const DigitType* a, const DigitType* b, DigitType c, DigitType* result, DigitType carry) { return carry; }

    // bit operations on a single array of digits
    static DigitType bitShiftLeft (const DigitType* a, DigitType bits, DigitType* result) { return 0; }
    static DigitType bitShiftRight (const DigitType* a, DigitType bits, DigitType* result) { return 0; }
};

#undef Q
#undef DIGIT_BITS

