#include "Test.h"
#include "Tuple.h"

TEST_CASE(TupleSize) {
    // confirm that we haven't "accidentally" added virtual overhead to the Tuple types
    TEST_EQUALS(sizeof(Vector2), sizeof(f8)*2);
    TEST_EQUALS(sizeof(Vector3), sizeof(f8)*3);
}

TEST_CASE(Tuple) {
    //Log::Scope scope (Log::TRACE);

    Vector2 a;
    TEST_EQUALS (a[U], 0);
    TEST_EQUALS (a[V], 0);
    TEST_EQUALS (a, Vector2::ZERO);
    TEST_EQUALS (a.normL1(), 0);
    TEST_EQUALS (a.normL2(), 0);
    TEST_EQUALS (a.normInf(), 0);
    TEST_EQUALS (a.normPower(1), 0);
    TEST_EQUALS (a.normPower(2), 0);
    TEST_EQUALS (a.normPower(10), 0);

    TEST_EQUALS (a.norm(), a.normL2());
    TEST_EQUALS (a.length(), a.normL2());

    Vector2 uv (1, 2);
    TEST_EQUALS (uv[U], 1);
    TEST_EQUALS (uv[V], 2);
    TEST_XYOP (uv, Vector2::ZERO, !=);
    TEST_EQUALS (uv.maxCoordinate(), Y);

    Vector3 abc (3, 4, 5);
    TEST_EQUALS (abc[X], 3);
    TEST_EQUALS (abc[Y], 4);
    TEST_EQUALS (abc[Z], 5);
    TEST_EQUALS (abc.maxCoordinate(), Z);

    Vector3 def (8, 7, 6);
    TEST_EQUALS (def[X], 8);
    TEST_EQUALS (def[Y], 7);
    TEST_EQUALS (def[Z], 6);
    TEST_EQUALS (def.maxCoordinate(), X);

    Vector3 xyz = def - abc;
    TEST_EQUALS (xyz, Vector3 (5, 3, 1));
    TEST_EQUALS (xyz[X], 5);
    TEST_EQUALS (xyz[Y], 3);
    TEST_EQUALS (xyz[Z], 1);
}

TEST_CASE(TupleConstructors) {
    //Log::Scope scope (Log::TRACE);

    // basic empty constructor
    Vector3 a;
    TEST_EQUALS(a, Vector3 (0, 0, 0));

    // fill constructor
    Vector3 b (1);
    TEST_EQUALS(b, Vector3 (1, 1, 1));

    // copy constructor
    Vector3 c (b);
    TEST_EQUALS(c, Vector3 (1, 1, 1));

    // direct
    Vector2 direct2 (3, 4);
    TEST_EQUALS(direct2[X], 3); TEST_EQUALS(direct2[Y], 4);

    Vector3 direct3 (7, 8, 9);
    TEST_EQUALS(direct3[X], 7); TEST_EQUALS(direct3[Y], 8); TEST_EQUALS(direct3[Z], 9);

    // from an array of doubles
    double xyz[] = {2, 2, 2};
    Vector3 d (xyz);
    TEST_EQUALS(d, Vector3 (2, 2, 2));

    // from a bigger tuple
    Vector2 f (d);
    TEST_EQUALS(f, Vector2 (2, 2));

    // from a smaller tuple with fill
    Vector3 g (f, 5);
    TEST_EQUALS(g, Vector3 (2, 2, 5));
}

TEST_CASE(TupleAssignment) {
    // NOTE: there are no assignment operators explicitly declared
    //Log::Scope scope (Log::TRACE);
    Vector3 a;

    // basic assignment from a tuple
    a = Vector3 (1, 1, 1);
    TEST_EQUALS(a, Vector3 (1, 1, 1));

    // assignment from an array
    double xyz[] = {2, 4, 6};
    a = xyz;
    TEST_EQUALS(a, Vector3 (2, 4, 6));

    // assignment from an initializer
    a = {5, 9, 11};
    TEST_EQUALS(a, Vector3 (5, 9, 11));
}

TEST_CASE(TupleEquality) {
    //Log::Scope scope (Log::TRACE);

#if (TUPLE_COMPARISON_TYPE == 2) or (TUPLE_COMPARISON_TYPE == 3)

    TEST_XYOP(Vector2 (2, 2), Vector2 (2.45, 2), !=);
    {
        Vector2::Scope epsilon (0.5);
        TEST_EQUALS(Vector2 (2, 2), Vector2 (2.45, 2));
        TEST_NOT_EQUALS(Vector2 (2, 2), Vector2 (2.55, 2));
    }
    TEST_XYOP(Vector2 (2, 2), Vector2 (2.45, 2), !=);
#endif

    TEST_EQUALS(Vector2 (1, 1) * 2, Vector2 (2, 2));
    TEST_NOT_EQUALS(Vector2 (2, 2), Vector2 (2.45, 2));
}

TEST_CASE(TupleNorm) {
    //Log::Scope scope (Log::TRACE);

    Vector2 a (3, 4);
    TEST_EQUALS (a[U], 3);
    TEST_EQUALS (a[V], 4);
    TEST_NOT_EQUALS (a, Vector2::ZERO);

    TEST_EQUALS (a.normL1(), 7);
    TEST_EQUALS_ULP (a.normL2(), 5);
    TEST_EQUALS (a.normInf(), 4);
    TEST_EQUALS_ULP (a.normPower(1), 7);
    TEST_EQUALS_ULP (a.normPower(2), 5);

    TEST_EQUALS_ULP (a.norm(), a.normL2());
    TEST_EQUALS_ULP (a.length(), a.normL2());
}

TEST_CASE(TupleUnit) {
    //Log::Scope scope (Log::TRACE);

    Vector2 a = (Vector2 (2, 0)).normalized();
    TEST_EQUALS(a.norm (), 1); TEST_EQUALS(a[X], 1); TEST_EQUALS(a[Y], 0); TEST_TRUE(a.isUnit());

    Vector2 b = (Vector2 (0, 2)).normalized();
    TEST_EQUALS(b.norm (), 1); TEST_EQUALS(b[X], 0); TEST_EQUALS(b[Y], 1); TEST_TRUE(b.isUnit());

    Vector2 c = (Vector2 (-2, 0)).normalized();
    TEST_EQUALS(c.norm (), 1); TEST_EQUALS(c[X], -1); TEST_EQUALS(c[Y], 0); TEST_TRUE(c.isUnit());

    Vector2 d = (Vector2 (0, -2)).normalized();
    TEST_EQUALS(d.norm (), 1); TEST_EQUALS(d[X], 0); TEST_EQUALS(d[Y], -1); TEST_TRUE(d.isUnit());

    Vector2 e = (Vector2 (2, 2)).normalized();
    TEST_EQUALS_ULP(e.norm (), 1); TEST_EQUALS_EPS(e.norm (), 1, 1e-9); TEST_EQUALS(e[X], e[Y]); TEST_TRUE(e.isUnit());

    Vector2 f = (Vector2 (-2, 2)).normalized();
    TEST_EQUALS_ULP(f.norm (), 1); TEST_EQUALS_EPS(f.norm (), 1, 1e-9); TEST_EQUALS(-f[X], f[Y]); TEST_TRUE(f.isUnit());

    Vector2 g = (Vector2 (-2, -2)).normalized();
    TEST_EQUALS_ULP(g.norm (), 1); TEST_EQUALS_EPS(g.norm (), 1, 1e-9); TEST_EQUALS(g[X], g[Y]); TEST_TRUE(g.isUnit());

    Vector2 h = (Vector2 (2, -2)).normalized();
    TEST_EQUALS_ULP(h.norm (), 1);TEST_EQUALS_EPS(h.norm (), 1, 1e-9); TEST_EQUALS(h[X], -h[Y]); TEST_TRUE(h.isUnit());
}

TEST_CASE(TupleOps) {
    //Log::Scope scope (Log::TRACE);

    TEST_EQUALS (-Vector2 (1, 0), Vector2 (-1, 0));

    TEST_EQUALS (Vector2 (1, 0) * 4, Vector2 (4, 0));
    TEST_EQUALS (Vector2 (0, 1) * 4, Vector2 (0, 4));
    TEST_EQUALS (Vector2 (1, 1) * 4, Vector2 (4, 4));

    TEST_EQUALS (4 * Vector2 (1, 0), Vector2 (4, 0));
    TEST_EQUALS (4 * Vector2 (0, 1), Vector2 (0, 4));
    TEST_EQUALS (4 * Vector2 (1, 1), Vector2 (4, 4));

    TEST_EQUALS (Vector2 (4, 0) / 4, Vector2 (1, 0));
    TEST_EQUALS (Vector2 (0, 4) / 4, Vector2 (0, 1));
    TEST_EQUALS (Vector2 (4, 4) / 4, Vector2 (1, 1));

    TEST_EQUALS (Vector2 (1, 0) + Vector2 (1, 0), Vector2 (2, 0));
    TEST_EQUALS (Vector2 (1, 0) - Vector2 (1, 0), Vector2 (0, 0));

    TEST_EQUALS (Vector2 (1, 0).perpendicular(), Vector2 (0, -1));
    TEST_EQUALS (Vector2 (1, 0) DOT Vector2 (1, 0), 1);
    TEST_EQUALS (Vector2 (1, 0) CROSS Vector2 (0, 1), 1);

    // Vector3
    TEST_EQUALS (-Vector3 (1, 0, -1), Vector3 (-1, 0, 1));

    TEST_EQUALS (Vector3 (1, 0, 2) * 4, Vector3 (4, 0, 8));
    TEST_EQUALS (Vector3 (0, 1, 2) * 4, Vector3 (0, 4, 8));
    TEST_EQUALS (Vector3 (1, 1, 2) * 4, Vector3 (4, 4, 8));

    TEST_EQUALS (4 * Vector3 (1, 0, 2), Vector3 (4, 0, 8));
    TEST_EQUALS (4 * Vector3 (0, 1, 2), Vector3 (0, 4, 8));
    TEST_EQUALS (4 * Vector3 (1, 1, 2), Vector3 (4, 4, 8));

    TEST_EQUALS (Vector3 (4, 0, 1) / 4, Vector3 (1, 0, 0.25));
    TEST_EQUALS (Vector3 (0, 4, 1) / 4, Vector3 (0, 1, 0.25));
    TEST_EQUALS (Vector3 (4, 4, 1) / 4, Vector3 (1, 1, 0.25));

    TEST_EQUALS (Vector3 (1, 0, 1) + Vector3 (1, 0, 2), Vector3 (2, 0, 3));
    TEST_EQUALS (Vector3 (1, 0, 1) - Vector3 (1, 0, 2), Vector3 (0, 0, -1));

    Log::debug() << "Vector: " << Vector3 (1, 0, 1) << ", Normalized: " << Vector3 (1, 0, 1).normalized() << endl;

    TEST_EQUALS_ULP (Vector3 (1, 0, 1).normalized () DOT Vector3 (1, 0, 1).normalized (), 1);
    TEST_EQUALS (Vector3 (0, 1, 0) DOT Vector3 (1, 0, 0), 0);
    TEST_EQUALS (Vector3 (1, 0, 0) CROSS Vector3 (0, 1, 0), Vector3 (0, 0, 1));
}


