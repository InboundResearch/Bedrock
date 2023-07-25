#include "Test.h"
#include "Hyperplane.h"

TEST_MODULE_DEPENDENCIES (Hyperplane2, "Tuple")

TEST_CASE(Hyperplane2) {
    //Log::Scope scope (Log::TRACE);

    Vector2 a (0, 2);
    Vector2 b (1, 1);

    auto hyperplane = Hyperplane2::fromPoints (a, b);
    Vector2 normal = hyperplane.getNormal ();
    Vector2 pt = hyperplane.getPoint ();

    TEST_EQUALS(normal, (Vector2 (-1, -1)).normalized ());

    TEST_TRUE(hyperplane.isOn (a));
    TEST_TRUE(hyperplane.isOn (b));
    TEST_TRUE(hyperplane.isOn (pt));

    TEST_EQUALS_EPS(hyperplane.distance (Vector2 ()), sqrt (2), 1e-6);
}

TEST_CASE(Hyperplane3) {
    //Log::Scope scope (Log::TRACE);

    auto plane = Hyperplane3::fromPoints (Vector3 (0, 1, 0), Vector3 (1, 1, 0), Vector3 (0, 1, -1));
    TEST_EQUALS (plane.getNormal (), Vector3 (0, 1, 0));
    TEST_EQUALS_EPS(plane.getC (), -1, 1e-9);
    TEST_EQUALS(plane.getHyperplane (), Vector4 (0, 1, 0, -1));
    TEST_EQUALS_EPS (plane.distance (Vector3 ()), -1, 1e-9);
    TEST_EQUALS_EPS (plane.intersect (Line3::fromTwoPoints(Point3 (), Point3 (1, 1, 0))), sqrt (2), 1e-9);
}
