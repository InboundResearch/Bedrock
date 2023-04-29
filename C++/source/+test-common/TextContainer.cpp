#include "Test.h"
#include "TextContainer.h"

TEST_MODULE_DEPENDENCIES (TextContainer, "Text")

TEST_CASE (TextLess) {
    Text    a ("hello");
    Text    b ("there");
    Text    c ("world");
    TextLess textLess;
    TEST_EQUALS (textLess (a, b), true);
    TEST_EQUALS (textLess (b, c), true);
    TEST_EQUALS (textLess (a, c), true);
    TEST_EQUALS (textLess (c, a), false);
}

TEST_CASE (TextSet) {
    TextSet ts;
    TEST_EQUALS (ts.size(), 0);
    ts.put ("Hello");
    TEST_EQUALS (ts.size(), 1);
    TEST_EQUALS (ts.contains("Hello"), true);
    ts.put ("World");
    TEST_EQUALS (ts.size(), 2);
    TEST_EQUALS (ts.contains(Text ("Hello")), true);
    TEST_EQUALS (ts.contains(Text ("World")), true);

    TEST_EQUALS (ts.contains(Text ("JOE")), false);
    TEST_EQUALS (ts.contains(Text ("Hell")), false);
    TEST_EQUALS (ts.contains(Text ("Hello1")), false);

    TEST_EQUALS (ts.toText (), "Hello, World");
    Log::debug () << ts << endl;

    vector<Text> keys = ts.keys ();
    TEST_EQUALS (keys[0], "Hello");
    TEST_EQUALS (keys[1], "World");
}

TEST_CASE (TextMap) {
    TextMap<Text>   tm;
    TEST_EQUALS (tm.size(), 0);
    tm.set ("Hello", "World");
    TEST_EQUALS (tm.size(), 1);
    TEST_NOT_EQUALS (tm.get ("Hello"), 0);
    TEST_EQUALS (*(tm.get ("Hello")), "World");

    tm.set ("xxx", "yyy");
    TEST_EQUALS (tm.size(), 2);
    TEST_XYOP (tm.get ("Hello"), 0, !=);
    TEST_EQUALS (*(tm.get ("Hello")), "World");
    TEST_XYOP (tm.get ("xxx"), 0, !=);
    TEST_EQUALS (*(tm.get ("xxx")), "yyy");

    tm.set ("Hello", "Joe");
    TEST_EQUALS (tm.size(), 2);
    TEST_NOT_EQUALS (tm.get ("Hello"), 0);
    TEST_EQUALS (*(tm.get ("Hello")), "Joe");
    TEST_NOT_EQUALS (tm.get ("xxx"), 0);
    TEST_EQUALS (*(tm.get ("xxx")), "yyy");

    TEST_EQUALS (tm.toText (), "Hello: Joe, xxx: yyy");
    Log::debug () << tm << endl;

    vector<Text> keys = tm.keys ();
    TEST_EQUALS (keys[0], "Hello");
    TEST_EQUALS (keys[1], "xxx");
}
