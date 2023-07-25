#include "Test.h"
#include "BoundingBall.h"

TEST_MODULE_DEPENDENCIES (BoundingBall, "Tuple")

/*
TEST_CASE(BoundingBall) {
    //Log::Scope scope (Log::TRACE);
    Vector2 points[] = {Vector2 (0, 0), Vector2 (1, 0), Vector2 (0, 1)};
    auto ball = BoundingBall2::fromPoints (points, 3);

    TEST_EQUALS(ball.getRadius(), sqrt (2) / 2);
    TEST_FALSE(ball.isEmpty());
    TEST_TRUE(ball.contains(Vector2(0, 0)));
    TEST_TRUE(ball.contains(Vector2(1, 0)));
    TEST_TRUE(ball.contains(Vector2(0, 1)));
    TEST_TRUE(ball.contains(Vector2(0.25, 0.25)));
    TEST_TRUE(ball.contains(Vector2(0.75, 0.75)));
    TEST_FALSE(ball.contains(Vector2(0, 2)));
    TEST_FALSE(ball.contains(Vector2(2, 0)));
    TEST_FALSE(ball.contains(Vector2(0, -2)));
    TEST_FALSE(ball.contains(Vector2(-2, 0)));
    TEST_EQUALS(ball.getCenter(), Vector2(0.5, 0.5));
}
*/

TEST_CASE(From2BoundaryPoints) {
    //Log::Scope scope (Log::TRACE);
    auto ball = BoundingBall2::fromTwoPoints(Vector2 (1, 0), Vector2 (-1, 0));
    Log::debug () << "center: " << ball.getCenter () << endl;
    TEST_FALSE(ball.isEmpty());
    TEST_EQUALS(ball.getRadius(), 1);
    TEST_EQUALS(ball.getCenter(), Point2 ());
}

TEST_CASE(From3BoundaryPoints) {
    Log::Scope scope (Log::TRACE);
    Vector2 points[] = {Vector2 (-3, 4), Vector2 (4, 5), Vector2(1, -4)};
    auto ball = BoundingBall2::fromBoundaryPoints (points, 3);
    Log::debug () << "center: " << ball.getCenter () << endl;
    TEST_FALSE(ball.isEmpty());
    TEST_EQUALS(ball.getRadius(), 5);
    TEST_EQUALS(ball.getCenter(), Vector2 (1, 1));
}

TEST_CASE(From3BoundaryPointsRandom) {
    //Log::Scope scope (Log::TRACE);

    random_device randomDevice;
    mt19937 twister (randomDevice ());
    std::uniform_real_distribution<f8> dist(1.0, nextafter(10.0, numeric_limits<f8>::max ()));

    for (int i = 0; i < 100; ++i) {
        // generate a random circle
        f8 radius = dist (twister);
        Point2 center (dist (twister), dist (twister));

        // generate three points on the circle at random
        f8 alpha;
        alpha = ((dist (twister) - 1.0) / 9.0) * M_PI * 2;
        Point2 a = (Point2 (cos (alpha), sin (alpha)) * radius) + center;
        alpha = ((dist (twister) - 1.0) / 9.0) * M_PI * 2;
        Point2 b = (Point2 (cos (alpha), sin (alpha)) * radius) + center;
        alpha = ((dist (twister) - 1.0) / 9.0) * M_PI * 2;
        Point2 c = (Point2 (cos (alpha), sin (alpha)) * radius) + center;

        // now compute the bounding ball
        vector<Point2> points = { a, b, c };
        auto ball = BoundingBall2::fromBoundaryPoints(points.data (), 3);

        // and confirm our result matches the setup
        TEST_FALSE(ball.isEmpty());
        TEST_EQUALS(ball.getCenter(), center);
        TEST_EQUALS_EPS(ball.getRadius(), radius, Point2::getEpsilon ());
    }
}

