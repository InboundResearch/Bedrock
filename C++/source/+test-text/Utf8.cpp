#include "Test.h"
#include "Utf8.h"

// utf-8 test file: https://www.w3.org/2001/06/utf-8-wrong/UTF-8-test.html
// https://en.wikipedia.org/wiki/UTF-8

TEST_CASE(TestUtf8Simple) {
    //Log::Scope scope (Log::TRACE);
    const char* str = "Hello World";
    Utf8Decoder decoder (str);
    while (decoder.goNext ()) {
        uint compare = (uint) *str++;
        TEST_EQUALS(*decoder, compare);
    }
    TEST_EQUALS(decoder.goNext (), false);
}

// useful test resource: https://www.utf8-chartable.de/unicode-utf8-table.pl?start=12288&number=128
TEST_CASE(TestUtf8Japanese) {
    // Japanese characters are 3-byte sequences
    // 5 chars - A, I, U, E, O
    byte str[] = { 0xe3, 0x81, 0x82, 0xe3, 0x81, 0x84, 0xe3, 0x81, 0x86, 0xe3, 0x81, 0x88, 0xe3, 0x81, 0x8a, 0 };
    uint compareTo[] = { 0x3042, 0x3044, 0x3046, 0x3048, 0x304A, 0 };
    uint* xxx = &compareTo[0];
    Utf8Decoder decoder (str);
    while (decoder.goNext ()) {
        uint compare = *xxx++;
        TEST_EQUALS(*decoder, compare);
    }
    TEST_EQUALS(decoder.goNext (), false);
}

TEST_CASE(TestUtf8Mixed) {
    // 5 chars - A, I, U, E, O - interspersed with "Hello"
    byte str[] = { 0xe3, 0x81, 0x82, 'h', 0xe3, 0x81, 0x84, 'e', 0xe3, 0x81, 0x86, 'l', 0xe3, 0x81, 0x88, 'l', 0xe3, 0x81, 0x8a, 'o', 0 };
    uint compareTo[] = { 0x3042, 'h', 0x3044, 'e', 0x3046, 'l', 0x3048, 'l', 0x304A, 'o', 0 };
    uint* xxx = &compareTo[0];
    Utf8Decoder decoder (str);
    while (decoder.goNext ()) {
        uint compare = *xxx++;
        TEST_EQUALS(*decoder, compare);
    }
    TEST_EQUALS(decoder.goNext (), false);
}

TEST_CASE(TestUtf8Wiki) {
    // 4 chars from the wiki page
    byte str[] = { 0x24, 0xc2, 0xa2, 0xe2, 0x82, 0xac, 0xf0, 0x90, 0x8d, 0x88, 0 };
    uint compareTo[] = { 0x0024, 0x00a2, 0x20ac, 0x10348, 0 };
    uint* xxx = &compareTo[0];
    Utf8Decoder decoder (str);
    while (decoder.goNext ()) {
        uint compare = *xxx++;
        TEST_EQUALS(*decoder, compare);
    }
    TEST_EQUALS(decoder.goNext (), false);
}

// need 2-byte sequences and 4-byte sequences
