#include "Test.h"
#include "BoundingBox.h"

TEST_MODULE_DEPENDENCIES (BoundingBox, "Tuple")

TEST_CASE(BoundingBox) {
    //Log::Scope scope (Log::TRACE);
    BoundingBox<f8, 2> box;

    TEST_TRUE(box.isEmpty());

    f8 span = box.addPoint(Vector2 (0, 0)).getSpan().length();
    Log::debug () << "Low: " << box.getLow () << endl;
    Log::debug () << "High: " << box.getHigh () << endl;
    Log::debug () << "Span: " << box.getSpan () << endl;
    TEST_EQUALS(span, 0);
    TEST_FALSE(box.isEmpty());

    span = box.addPoint(Vector2 (1, 0)).getSpan().length();
    TEST_EQUALS(span, 1);
    TEST_FALSE(box.isEmpty());

    span = box.addPoint(Vector2 (0, 1)).getSpan().length();
    TEST_EQUALS(span, sqrt (2));
    TEST_FALSE(box.isEmpty());
    TEST_TRUE(box.contains(Vector2(0, 0)));
    TEST_TRUE(box.contains(Vector2(1, 0)));
    TEST_TRUE(box.contains(Vector2(0, 1)));
    TEST_TRUE(box.contains(Vector2(0.25, 0.25)));
    TEST_TRUE(box.contains(Vector2(0.75, 0.75)));
    TEST_FALSE(box.contains(Vector2(0, 2)));
    TEST_FALSE(box.contains(Vector2(2, 0)));
    TEST_FALSE(box.contains(Vector2(0, -2)));
    TEST_FALSE(box.contains(Vector2(-2, 0)));
    TEST_EQUALS(box.getLow(), Vector2(0, 0));
    TEST_EQUALS(box.getHigh(), Vector2(1, 1));
}

TEST_CASE(BoundingBoxPts) {
    //Log::Scope scope (Log::TRACE);
    Vector2 points[] = {Vector2 (0, 0), Vector2 (1, 0), Vector2 (0, 1)};
    BoundingBox<f8, 2> box;
    TEST_TRUE(box.isEmpty());

    f8 span = box.addPoints(points, 3).getSpan().length();
    TEST_EQUALS(span, sqrt (2));
    TEST_FALSE(box.isEmpty());
    TEST_TRUE(box.contains(Vector2(0, 0)));
    TEST_TRUE(box.contains(Vector2(1, 0)));
    TEST_TRUE(box.contains(Vector2(0, 1)));
    TEST_TRUE(box.contains(Vector2(0.25, 0.25)));
    TEST_TRUE(box.contains(Vector2(0.75, 0.75)));
    TEST_FALSE(box.contains(Vector2(0, 2)));
    TEST_FALSE(box.contains(Vector2(2, 0)));
    TEST_FALSE(box.contains(Vector2(0, -2)));
    TEST_FALSE(box.contains(Vector2(-2, 0)));
    TEST_EQUALS(box.getLow(), Vector2(0, 0));
    TEST_EQUALS(box.getHigh(), Vector2(1, 1));
}
