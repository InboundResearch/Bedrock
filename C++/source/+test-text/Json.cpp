#include "Test.h"
#include "Json.h"

TEST_CASE(Simple) {
    //Log::Scope scope (Log::TRACE);
    PtrToBagObject bagObject = Json::readBagObject("{ \"xxx\": 1234, \"yyy\": \"abc\" }");
    TEST_EQUALS(bagObject->getText("xxx"), "1234");
    TEST_EQUALS(bagObject->getText("yyy"), "abc");
}

TEST_CASE(Array) {
    //Log::Scope scope (Log::TRACE);
    PtrToBagArray bagArray = Json::readBagArray("[ \"xxx\", 1234, \"yyy\", \"abc\" ]");
    TEST_EQUALS(bagArray->getText(1), "1234");
    TEST_EQUALS(bagArray->getText(3), "abc");
    EXPECT_FAIL(bagArray->getText(5));
}

TEST_CASE(SimpleError) {
    //Log::Scope scope (Log::TRACE);
    PtrToBagObject bagObject = Json::readBagObject("{ \"xxx\": 1234,, \"yyy\": \"abc\" }");
    TEST_FALSE(bagObject);
}

TEST_CASE(EmptyObject) {
    //Log::Scope scope (Log::TRACE);
    PtrToBagObject bagObject = Json::readBagObject("{}");
    TEST_TRUE(bagObject);
}
