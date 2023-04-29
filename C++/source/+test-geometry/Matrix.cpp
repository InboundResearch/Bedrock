#include "Test.h"
#include "Matrix.h"

#define TEST_ARRAY_SIZE 10
#define TEST_COUNT 10000

#define NM(m, n) ((n * TEST_ARRAY_SIZE) + m)

// this test checks that the mechanism for indexing a 2 dimensional array is largely irrelevant,
// from a performance point of view. we use this finding to justify a non-storage based approach to
// indexing (just math)
TEST_CASE(AccessTimes) {
    //Log::Scope scope (Log::TRACE);

    f8* nm[TEST_ARRAY_SIZE];
    f8 data[TEST_ARRAY_SIZE * TEST_ARRAY_SIZE];
    f8 average;
    for (uint i = 0; i < TEST_ARRAY_SIZE; ++i) {
        nm[i] = &data[0] + (i * TEST_ARRAY_SIZE);
    }

    // setup a random number generator
    random_device randomDevice;
    mt19937 twister (randomDevice ());
    std::uniform_real_distribution<f8> dist(0.0, nextafter(1.0, numeric_limits<f8>::max ()));
    std::uniform_int_distribution<uint> distmn(0, 9);

    // fill the data with random numbers
    for (uint i = 0; i < (TEST_ARRAY_SIZE * TEST_ARRAY_SIZE); ++i) {
        data[i] = dist (twister);
    }

    u8 elapsedArithmetic = 0;
    u8 elapsedIndexed = 0;
    for (uint j = 0; j < 100; ++j) {
        {
            auto startTime = chrono::high_resolution_clock::now();

            average = 0;
            for (uint i = 0; i < TEST_COUNT; ++i) {
                uint m = distmn (twister);
                uint n = distmn (twister);
                average += data[NM(m, n)];
            }
            average /= TEST_COUNT;

            auto stopTime = chrono::high_resolution_clock::now();

            elapsedArithmetic += chrono::duration_cast<chrono::microseconds>(stopTime - startTime).count ();
            Log::debug () << "arithmetic access took : " << elapsedArithmetic << "us (average value: " << average << ")" << endl;
        }

        {
            auto startTime = chrono::high_resolution_clock::now();

            average = 0;
            for (uint i = 0; i < TEST_COUNT; ++i) {
                uint m = distmn (twister);
                uint n = distmn (twister);
                average += nm[n][m];
            }
            average /= TEST_COUNT;

            auto stopTime = chrono::high_resolution_clock::now();

            elapsedIndexed += chrono::duration_cast<chrono::microseconds>(stopTime - startTime).count();
            Log::debug () << "indexed access took : " << elapsedIndexed << "us (average value: " << average << ")" << endl;
        }
    }
    TEST_EQUALS_EPS (f8 (elapsedArithmetic) / f8 (elapsedIndexed), 1, 2.5e-2);
}

TEST_CASE(Identity) {
    Log::debug () << "pre-matrix" << endl;
    Matrix<f8, 4, 4> m;
    Log::debug () << "matrix created" << endl;
    m.identity();
    Log::debug () << "matrix identity" << endl;
    TEST_EQUALS(m(0, 1), 0);
    TEST_EQUALS(m(0, 0), 1);
    TEST_EQUALS(m(1, 1), 1);
    TEST_EQUALS(m(2, 2), 1);
    TEST_EQUALS(m(3, 3), 1);
}
