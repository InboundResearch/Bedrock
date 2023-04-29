#include "Test.h"
#include "Text.h"

TEST_MODULE_DEPENDENCIES (Text, "RawText")

TEST_CASE (Text0) {
    TEST_EQUALS(sizeof(Text), sizeof(void*));
}

TEST_CASE (Text1) {
    const char* hello = "Hello";
    const char* world = "World";
    Text helloText = hello;
    TEST_EQUALS(helloText, hello);
    TEST_EQUALS(helloText.length(), 5);

    Text worldText (world);
    TEST_EQUALS(worldText, world);
    TEST_EQUALS(worldText.length(), 5);

    TEST_EQUALS(helloText.compare(worldText), signum('H' - 'W'));
    TEST_EQUALS(worldText.compare(helloText), signum('W' - 'H'));
    TEST_EQUALS(helloText < worldText, true);
    TEST_EQUALS(helloText <= worldText, true);
    TEST_EQUALS(worldText > helloText, true);
    TEST_EQUALS(worldText >= helloText, true);
    TEST_EQUALS(helloText == helloText, true);
    TEST_EQUALS(helloText != helloText, false);
    TEST_EQUALS(helloText == "", false);
    TEST_EQUALS(helloText != "", true);
    TEST_EQUALS(helloText != 0, true);

    Text helloWorldText = helloText + " " + worldText;
    TEST_XYOP(helloWorldText, "Hello World", ==);
    TEST_EQUALS(helloWorldText.length(), 11);

    helloWorldText.clear ();
    TEST_EQUALS(helloWorldText.length(), 0);

    helloWorldText.clear () << world << "-" << hello;
    TEST_NOT_EQUALS(helloWorldText, "Hello World");
    TEST_EQUALS(helloWorldText, "World-Hello");

    helloWorldText.clear () += worldText;
    TEST_NOT_EQUALS(helloWorldText, "Hello World");
    TEST_EQUALS(helloWorldText, "World");

    Text emptyText;
    TEST_EQUALS(emptyText, "");
    TEST_EQUALS(emptyText.isEmpty (), true);
}

TEST_CASE (TextFind) {
    Text longString = "This is a long string that we want to find an offset in xxx";
    TEST_EQUALS(longString.find ("long"), 10);
    TEST_EQUALS(longString.find ("short"), -1);
    TEST_EQUALS(longString.find ("This"), 0);
    TEST_EQUALS(longString.find ("xxx"), (int) (longString.length()) - 3);
}

TEST_CASE (TextSubstring) {
    Text longString = "This is a long string";
    TEST_EQUALS(longString.substring (0), "This is a long string");
    TEST_EQUALS(longString.substring (10), "long string");
    TEST_EQUALS(longString.substring (10, 100), "long string");
    TEST_EQUALS(longString.substring (10, 4), "long");
    TEST_EQUALS(longString.substring (50, 4), "");
}

TEST_CASE (TextSplitEmpty) {
    Text longString = "abcdefghijk";
    vector<Text> splitResult = longString.splitFirst ("/");
    TEST_EQUALS(splitResult.size (), 1);
    TEST_EQUALS(splitResult[0], longString);
}

TEST_CASE (TextSplit) {
    Text longString = "xxx##yyy##zzz##aaa";
    vector<Text> splitResult = longString.splitFirst ("##");
    TEST_EQUALS(splitResult.size (), 2);
    TEST_EQUALS(splitResult[0], "xxx");
    TEST_EQUALS(splitResult[1], "yyy##zzz##aaa");

    splitResult = longString.split ("##");
    TEST_EQUALS(splitResult.size (), 4);
    TEST_EQUALS(splitResult[0], "xxx");
    TEST_EQUALS(splitResult[1], "yyy");
    TEST_EQUALS(splitResult[2], "zzz");
    TEST_EQUALS(splitResult[3], "aaa");

    longString = "xxx##yyy##zzz##aaa##";
    splitResult = longString.split ("##");
    TEST_EQUALS(splitResult.size (), 5);
    TEST_EQUALS(splitResult[0], "xxx");
    TEST_EQUALS(splitResult[1], "yyy");
    TEST_EQUALS(splitResult[2], "zzz");
    TEST_EQUALS(splitResult[3], "aaa");
    TEST_EQUALS(splitResult[4].isEmpty (), true);

    longString = "##xxx##yyy##zzz##aaa##";
    splitResult = longString.split ("##");
    TEST_EQUALS(splitResult.size (), 6);
    TEST_EQUALS(splitResult[0].isEmpty (), true);
    TEST_EQUALS(splitResult[1], "xxx");
    TEST_EQUALS(splitResult[2], "yyy");
    TEST_EQUALS(splitResult[3], "zzz");
    TEST_EQUALS(splitResult[4], "aaa");
    TEST_EQUALS(splitResult[5].isEmpty (), true);

    longString = ",,a,,";
    splitResult = longString.split (",");
    TEST_EQUALS(splitResult.size (), 5);
    TEST_EQUALS(splitResult[0].isEmpty (), true);
    TEST_EQUALS(splitResult[1].isEmpty (), true);
    TEST_EQUALS(splitResult[2], "a");
    TEST_EQUALS(splitResult[3].isEmpty (), true);
    TEST_EQUALS(splitResult[4].isEmpty (), true);
}

TEST_CASE (TextReplace) {
    Text longString = "abcd--efgh";
    Text replaceResult = longString.replace ("--", "");
    TEST_EQUALS(replaceResult.length (), 8);
    TEST_EQUALS(replaceResult, "abcdefgh");

    replaceResult = longString.replace ("--", "&");
    TEST_EQUALS(replaceResult.length (), 9);
    TEST_EQUALS(replaceResult, "abcd&efgh");

    replaceResult = longString.replace ("gh", "&");
    TEST_EQUALS(replaceResult.length (), 9);
    TEST_EQUALS(replaceResult, "abcd--ef&");

    replaceResult = longString.replace ("ab", "&");
    TEST_EQUALS(replaceResult.length (), 9);
    TEST_EQUALS(replaceResult, "&cd--efgh");

    replaceResult = longString.replace ("xy", "&");
    TEST_EQUALS(replaceResult.length (), 10);
    TEST_EQUALS(replaceResult, longString);

    longString = "a b c d";
    replaceResult = longString.replace (" ", "-");
    TEST_EQUALS(replaceResult.length (), 7);
    TEST_EQUALS(replaceResult, "a-b-c-d");

    longString = " a b c d";
    replaceResult = longString.replace (" ", "-");
    TEST_EQUALS(replaceResult.length (), 8);
    TEST_EQUALS(replaceResult, "-a-b-c-d");

    longString = "a b c d ";
    replaceResult = longString.replace (" ", "-");
    TEST_EQUALS(replaceResult.length (), 8);
    TEST_EQUALS(replaceResult, "a-b-c-d-");
}
