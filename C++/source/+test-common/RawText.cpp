#include "Test.h"
#include "RawText.h"

TEST_MODULE_DEPENDENCIES (RawText, "Buffer")

TEST_CASE (RawText) {
    //Log::Scope  scope (Log::DEBUG);

    const char* helloWorld = "Hello World";
    uint helloWorldLength = strlen (helloWorld);
    PtrToRawText rawText = RawText::make (helloWorld, helloWorldLength);
    TEST_EQUALS (rawText->getCapacity(), 16);
    TEST_EQUALS (rawText->getLength (), helloWorldLength);
    TEST_EQUALS (memcmp (helloWorld, rawText->get(), rawText->getLength()), 0);

    const char* hello = "Hello";
    uint helloLength = strlen (hello);
    rawText = RawText::make (helloWorld, helloLength);
    TEST_EQUALS (rawText->getCapacity(), 8);
    TEST_EQUALS (rawText->getLength (), helloLength);
    TEST_EQUALS (memcmp (hello, rawText->get(), rawText->getLength()), 0);

    TEST_EQUALS (rawText->append (hello, strlen (hello)), false);
    TEST_EQUALS (rawText->getLength (), helloLength);
    TEST_EQUALS (memcmp ("Hello", rawText->get(), rawText->getLength()), 0);

    rawText = RawText::make(50);
    rawText->set (hello, helloLength);
    TEST_EQUALS (rawText->getCapacity(), 64);
    TEST_EQUALS (rawText->getLength (), helloLength);
    TEST_EQUALS (memcmp (hello, rawText->get(), rawText->getLength()), 0);
    rawText->append (hello, helloLength);
    TEST_EQUALS (rawText->getLength (), helloLength * 2);
    TEST_EQUALS (memcmp ("HelloHello", rawText->get(), rawText->getLength()), 0);

    PtrToRawText empty = RawText::getEmpty();
    TEST_EQUALS (empty->getCapacity(), 0);
    TEST_EQUALS (empty->getLength(), 0);
    TEST_EQUALS ((empty->get ())[0], 0);
}
