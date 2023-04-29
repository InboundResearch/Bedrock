#pragma once

#include        "Common.h"

// the basic memory types to use for half-digits, whole-digits, and double-digits
typedef u2 uHalfDigit;
typedef s2 sHalfDigit;
typedef u4 uDigit;
typedef s4 sDigit;
typedef	u8 uTwoDigit;
typedef	s8 sTwoDigit;

const int DIGIT_BITS = (sizeof (uDigit) * 8);
const int DIGIT_SHIFT = 4;
const uTwoDigit DIGIT_MASK = ((uTwoDigit(0x01) << DIGIT_BITS) - 1);

struct Digit {
        static uDigit       digitDivisorByRadix[];
        static int          maxLengthByRadix[];
        static const char*  digit2char;
        static uDigit       char2digit[];

    // convert a part of a string to a digit, assuming a given radix
    // return the number of chars consumed in the process
    // outputs the final multiplier
    static int strToDigit (const char* string, uDigit radix, uDigit& digit, uDigit& multiplier) {
        digit = 0;
        multiplier = 1;
        int i;
        int maxLength = maxLengthByRadix[radix];
        for (i = 0; (i < maxLength) and *string; ++i) {
            digit = (digit * radix) + char2digit[uint (*string++)];
            multiplier *= radix;
        }
        return i;
    }

    // place a digit into a string, assuming a given radix - works backwards from the end of the
    // buffer
    static int digitToStr (uDigit digit, char* string, uDigit radix, bool pad = true) {
        int count = 0;
        for (int i = 0, maxLength = maxLengthByRadix[radix]; (i < maxLength) and (pad || digit || (count == 0)); ++i) {
            uDigit  remainder = digit % radix;
            digit /= radix;
            string[--count] = digit2char[remainder];
        }
        return count;
    }

    // return the digit by digit comparison of two strings of digits, where the max delta between
    // any two digits is max (uDigit) + a sign bit... so a sTwoDigit is needed to hold it
    static sTwoDigit compare (int size, const uDigit* a, const uDigit* b) {
        for (int i = size - 1; i >= 0; --i) {
            sTwoDigit comparison = sTwoDigit (a[i]) - sTwoDigit (b[i]);
            if (comparison) {
                return comparison;
            }
        }
        return 0;
    }

    // subtract one array of digits from another, return the borrow
    static uDigit subtract (sDigit size, const uDigit* a, const uDigit* b, uDigit* result) {
        uTwoDigit borrow = 0;
        for (sDigit i = 0; i < size; i++) {
            result[i] = uDigit (borrow = a[i] - b[i] - ((borrow bit_and (uTwoDigit (0x01) << DIGIT_BITS)) >> DIGIT_BITS));
        }
        return uDigit (borrow);
    }

    static uDigit subtractMultiply (sDigit size, const uDigit* a, const uDigit* b, uDigit c, uDigit* result) {
        uTwoDigit carry = 0;
        uTwoDigit borrow = 0;
        for (sDigit i = 0; i < size; i++) {
            carry = (b[i] * c) + (carry >> DIGIT_BITS);
            borrow = a[i] - uDigit (carry) - ((borrow bit_and (uTwoDigit(0x01) << DIGIT_BITS)) >> DIGIT_BITS);
            result[i] = uDigit (borrow);
        }
        return borrow;
    }

    static int bits (uDigit digit) {
        return (digit == 0) ? DIGIT_BITS : __builtin_clz (digit);
    }
};
