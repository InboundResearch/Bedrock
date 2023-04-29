#include "Test.h"
#include "Copy.h"

TEST_MODULE_DEPENDENCIES (Copy, "Types")

TEST_CASE(Copy) {
    int a[] = { 0, 1, 2, 3, 4, 5, 6, 7 };
    int b[8];
    Copy<int, 8>::copy (b, a);
    for (int i = 0; i < 8; ++i) {
        TEST_EQUALS(b[i], a[i]);
    }
}
