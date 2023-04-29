#include "Test.h"

// some checks to see what the memory/architecture model is
static_assert((sizeof(u2) == 2) and (sizeof(u4) == 4) and (sizeof(u8) == 8), "unsupported data model");

TEST_CASE (TypeSizes) {
    // these tests should pass if compilation succeeded
    TEST_EQUALS(sizeof (u1), 1);
    TEST_EQUALS(sizeof (u2), 2);
    TEST_EQUALS(sizeof (u4), 4);
    TEST_EQUALS(sizeof (u8), 8);

    TEST_EQUALS(sizeof (f4), 4);
    TEST_EQUALS(sizeof (f8), 8);
}

