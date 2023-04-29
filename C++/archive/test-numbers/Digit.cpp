#include "Test.h"
#include "Digit.h"

TEST_CASE(TestDigitStrToDigit) {
    //Log::Scope scope (Log::DEBUG);

    // static int strToDigit (const char* string, uDigit radix, uDigit& digit, uDigit& multiplier) {
    int consumed;
    uDigit result;
    uDigit multiplier;

    // zero
    Log::debug () << "TestDigitStrToDigit: " << "ZERO" << endl;
    consumed = Digit::strToDigit("0", 2, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 0); TEST_XY(multiplier, 2);

    consumed = Digit::strToDigit("0", 8, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 0); TEST_XY(multiplier, 8);

    consumed = Digit::strToDigit("0", 10, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 0); TEST_XY(multiplier, 10);

    consumed = Digit::strToDigit("0", 16, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 0); TEST_XY(multiplier, 16);

    // one
    Log::debug () << "TestDigitStrToDigit: " << "ONE" << endl;
    consumed = Digit::strToDigit("1", 2, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 1); TEST_XY(multiplier, 2);

    consumed = Digit::strToDigit("1", 8, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 1); TEST_XY(multiplier, 8);

    consumed = Digit::strToDigit("1", 10, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 1); TEST_XY(multiplier, 10);

    consumed = Digit::strToDigit("1", 16, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 1); TEST_XY(multiplier, 16);

    // two
    Log::debug () << "TestDigitStrToDigit: " << "TWO" << endl;
    consumed = Digit::strToDigit("10", 2, result, multiplier);
    TEST_XY(consumed, 2); TEST_XY(result, 2); TEST_XY(multiplier, 4);

    consumed = Digit::strToDigit("2", 8, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 2); TEST_XY(multiplier, 8);

    consumed = Digit::strToDigit("2", 10, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 2); TEST_XY(multiplier, 10);

    consumed = Digit::strToDigit("2", 16, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 2); TEST_XY(multiplier, 16);

    // eight
    Log::debug () << "TestDigitStrToDigit: " << "EIGHT" << endl;
    consumed = Digit::strToDigit("1000", 2, result, multiplier);
    TEST_XY(consumed, 4); TEST_XY(result, 8); TEST_XY(multiplier, 16);

    consumed = Digit::strToDigit("10", 8, result, multiplier);
    TEST_XY(consumed, 2); TEST_XY(result, 8); TEST_XY(multiplier, 64);

    consumed = Digit::strToDigit("8", 10, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 8); TEST_XY(multiplier, 10);

    consumed = Digit::strToDigit("8", 16, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 8); TEST_XY(multiplier, 16);

    // 10
    Log::debug () << "TestDigitStrToDigit: " << "10" << endl;
    consumed = Digit::strToDigit("1010", 2, result, multiplier);
    TEST_XY(consumed, 4); TEST_XY(result, 10); TEST_XY(multiplier, 16);

    consumed = Digit::strToDigit("12", 8, result, multiplier);
    TEST_XY(consumed, 2); TEST_XY(result, 10); TEST_XY(multiplier, 64);

    consumed = Digit::strToDigit("10", 10, result, multiplier);
    TEST_XY(consumed, 2); TEST_XY(result, 10); TEST_XY(multiplier, 100);

    consumed = Digit::strToDigit("a", 16, result, multiplier);
    TEST_XY(consumed, 1); TEST_XY(result, 10); TEST_XY(multiplier, 16);

    // 1000
    Log::debug () << "TestDigitStrToDigit: " << "1000" << endl;
    consumed = Digit::strToDigit("1111101000", 2, result, multiplier);
    TEST_XY(consumed, 10); TEST_XY(result, 1000); TEST_XY(multiplier, 1024);

    consumed = Digit::strToDigit("1750", 8, result, multiplier);
    TEST_XY(consumed, 4); TEST_XY(result, 1000); TEST_XY(multiplier, 4096);

    consumed = Digit::strToDigit("1000", 10, result, multiplier);
    TEST_XY(consumed, 4); TEST_XY(result, 1000); TEST_XY(multiplier, 10000);

    consumed = Digit::strToDigit("3E8", 16, result, multiplier);
    TEST_XY(consumed, 3); TEST_XY(result, 1000); TEST_XY(multiplier, 4096);
}

TEST_CASE(TestDigitDigitToStr) {
    //Log::Scope scope (Log::DEBUG);
    //static int digitToStr (uDigit digit, char* string, uDigit radix) {
    char buffer[256];
    char* string = buffer + 200;
    *string = 0;
    TEST_XY(strcmp("0", string + Digit::digitToStr (0, string, 2, false)), 0);
    TEST_XY(strcmp("0", string + Digit::digitToStr (0, string, 8, false)), 0);
    TEST_XY(strcmp("0", string + Digit::digitToStr (0, string, 10, false)), 0);
    TEST_XY(strcmp("0", string + Digit::digitToStr (0, string, 16, false)), 0);

    TEST_XY(strcmp("00000000000000000000000000000000", string + Digit::digitToStr (0, string, 2)), 0);
    TEST_XY(strcmp("00000000000", string + Digit::digitToStr (0, string, 8)), 0);
    TEST_XY(strcmp("0000000000", string + Digit::digitToStr (0, string, 10)), 0);
    TEST_XY(strcmp("00000000", string + Digit::digitToStr (0, string, 16)), 0);

    TEST_XY(strcmp("1111101000", string + Digit::digitToStr (1000, string, 2, false)), 0);
    TEST_XY(strcmp ("1750", string + Digit::digitToStr (1000, string, 8, false)), 0);
    TEST_XY(strcmp ("1000", string + Digit::digitToStr (1000, string, 10, false)), 0);
    TEST_XY(strcmp ("3e8", string + Digit::digitToStr (1000, string, 16, false)), 0);

    TEST_XY(strcmp("11111111111111111111111111111111", string + Digit::digitToStr (0xffffffff, string, 2)), 0);
    TEST_XY(strcmp("37777777777", string + Digit::digitToStr (0xffffffff, string, 8)), 0);
    TEST_XY(strcmp("4294967295", string + Digit::digitToStr (0xffffffff, string, 10)), 0);
    TEST_XY(strcmp("ffffffff", string + Digit::digitToStr (0xffffffff, string, 16)), 0);
    TEST_XY(strcmp("1z141z3", string + Digit::digitToStr (0xffffffff, string, 36)), 0);

    /*
    for (int i = 2; i < 37; ++i) {
        int offset = Digit::digitToStr (0xffffffff, string, i, false);
        Log::debug () << i << ": (" << (string + offset) << ") length=" << -offset << endl;
    }
    */
}

// this is a "test case" that computes the needed radix maximum values
#if 0
TEST_CASE(TestDigitComputeDigitDivisors) {
    // XXX TODO: still not sure about this
    Log::Scope scope (Log::DEBUG);
    Log& log = Log::debug () << "TestDigitComputeDigitDivisors: 0, 0";
    for (uint i = 2; i < 37; ++i) {
        uTwoDigit divisor = 1;
        sTwoDigit testValue = 0xffffffff;
        while (testValue > i) {
            testValue /= i;
            divisor *= i;
        }
        log << ", " << divisor;
    }
    log << endl;
    TEST_X(true);
}
#endif

TEST_CASE(TestDigitDigitBits) {
    //Log::Scope scope (Log::DEBUG);
    TEST_XY(Digit::bits (0), 32);
    TEST_XY(Digit::bits (1), 31);
    TEST_XY(Digit::bits (2), 30);
    TEST_XY(Digit::bits (3), 30);
    TEST_XY(Digit::bits (0xffffffff), 0);
    TEST_XY(Digit::bits (0x0000ffff), 16);
}

TEST_CASE(TestDigitCompare) {
    // static sTwoDigit compare (int size, const uDigit* a, const uDigit* b) {
    //Log::Scope scope (Log::DEBUG);
    uDigit a[] = {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff};
    uDigit b[] = {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff};
    TEST_XY(Digit::compare (5, a, b), 0);

    uDigit c[] = {0x00000000, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff};
    uDigit d[] = {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff};
    TEST_XY(Digit::compare (5, c, d), -0x00ffffffffLL);

    uDigit e[] = {0xffffffff, 0x00000000, 0xffffffff, 0xffffffff, 0xffffffff};
    uDigit f[] = {0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff};
    TEST_XY(Digit::compare (5, e, f), -0x00ffffffffLL);
}

TEST_CASE(TestDigitSubtract) {
    TEST_X(true);
}

TEST_CASE(TestDigitSubtractMultiply) {
    TEST_X(true);
}
