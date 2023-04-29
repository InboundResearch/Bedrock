#include "Test.h"
#include "Bag.h"

TEST_CASE(BagThings) {
    //Log::Scope scope (Log::TRACE);

    PtrToBagText ptrToBagText = new BagText ("Hello World");
    PtrToBagInteger ptrToBagInteger = new BagInteger (5);
    PtrToBagBool ptrToBagBool = new BagBool (true);
    PtrToBagFloat ptrToBagFloat = new BagFloat (6.5);

    TEST_EQUALS(ptrToBagText->toJson (), "\"Hello World\"");
    TEST_EQUALS(ptrToBagText->get (), "Hello World");
    TEST_EQUALS(ptrToBagInteger->toJson (), "5");
    TEST_EQUALS(ptrToBagInteger->get (), 5);
    TEST_EQUALS(ptrToBagBool->toJson (), "true");
    TEST_EQUALS(ptrToBagBool->get (), true);
    TEST_EQUALS(ptrToBagFloat->toJson (), "6.50000000");
    TEST_EQUALS(ptrToBagFloat->get (), 6.5);

    TEST_EQUALS(ptrToBagText->getType (), BagType::TEXT);
    TEST_EQUALS(ptrToBagInteger->getType (), BagType::INTEGER);
    TEST_EQUALS(ptrToBagBool->getType (), BagType::BOOL);
    TEST_EQUALS(ptrToBagFloat->getType (), BagType::FLOAT);
}
