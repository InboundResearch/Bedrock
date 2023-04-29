#include "Test.h"
#include "LineTest.h"

TEST_MODULE_DEPENDENCIES (LineTest, "Tuple")

TEST_CASE(LineTest) {
    //Log::Scope scope (Log::TRACE);

    Point2 a (0, 2);
    Point2 b (2, 0);
    Point2 c (3, 0);
    Point2 d (2, -1);
    Point2 e (4, -2);

    TEST_EQUALS (LineTest2::classified (a, b, c), LineClass::TURNS_LEFT);
    TEST_EQUALS (LineTest2::classified (a, b, d), LineClass::TURNS_RIGHT);
    TEST_EQUALS (LineTest2::classified (a, b, e), LineClass::COLINEAR);

    Edge<f8, 2> edge (a, b);
    TEST_EQUALS (LineTest2::classified (edge, c), LineClass::TURNS_LEFT);
    TEST_EQUALS (LineTest2::classified (edge, d), LineClass::TURNS_RIGHT);
    TEST_EQUALS (LineTest2::classified (edge, e), LineClass::COLINEAR);
}

