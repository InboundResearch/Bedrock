#include "Test.h"
#include "DigitOp.h"

/*
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
    static int compareDigit (const DigitType* a, DigitType b) { return signum (a[0] - b); }

    // bit operations on a single array of digits
    static void bitAnd (const DigitType* a, const DigitType* b, DigitType* result) {}
    static void bitOr (const DigitType* a, const DigitType* b, DigitType* result) {}
    static void bitXor (const DigitType* a, const DigitType* b, DigitType* result) {}
    static void bitComplement (const DigitType* a, DigitType* result) {}
*/

// we expect that the processors we are working on are little-endian, meaning the lowest point in
// memory is the least significant bit in a number. this can result in some confusing arrangements,
// as it essentially is like writing right to left
TEST_CASE(TestLittleEndian) {
    Log::Scope scope (Log::DEBUG);
    byte x[] = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

    u2*  u2x = reinterpret_cast<u2*> (x);
    Log::debug () << hex (u2x[0]) << ", " << hex (u2x[1]) << ", " << hex (u2x[2]) << ", " << hex (u2x[3]) << endl;
    TEST_XY(u2x[0], 0x0201);
    TEST_XY(u2x[1], 0x0403)
    TEST_XY(u2x[2], 0x0605);
    TEST_XY(u2x[3], 0x0807);

    u4*  u4x = reinterpret_cast<u4*> (x);
    Log::debug () << hex (u4x[0]) << ", " << hex (u4x[1])<< endl;
    TEST_XY(u4x[0], 0x04030201);
    TEST_XY(u4x[1], 0x08070605);
}

TEST_CASE(TestDigitOpTop) {
    Log::Scope scope (Log::DEBUG);
    u4 testValue;
    uint top;

    testValue = 0x01000000;
    top = DigitOp<u1, 4>::top (reinterpret_cast<u1*>(&testValue));
    TEST_XY(top, 4);

    testValue = 0x00010000;
    top = DigitOp<u1, 4>::top (reinterpret_cast<u1*>(&testValue));
    TEST_XY(top, 3);

    testValue = 0x00000100;
    top = DigitOp<u1, 4>::top (reinterpret_cast<u1*>(&testValue));
    TEST_XY(top, 2);

    testValue = 0x00000001;
    top = DigitOp<u1, 4>::top (reinterpret_cast<u1*>(&testValue));
    TEST_XY(top, 1);

    testValue = 0;
    top = DigitOp<u1, 4>::top (reinterpret_cast<u1*>(&testValue));
    TEST_XY(top, 0);
}

TEST_CASE(TestDigitOpSet) {
    Log::Scope scope (Log::DEBUG);
    //u4 testValue;
    //top = DigitOp<u1, 4>::top (&testValue);
    TEST_X(true);
}

