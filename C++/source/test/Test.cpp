#include "Test.h"

/*
TEST_CASE(ForceFailure) {
    TEST_ASSERTION(false);
}
*/

int main (int argc, char** argv) {
    // TODO process the arguments to "run one", override a test configuration, or force initialization of configuration

    cerr << "STARTING TESTS..." << endl;
    UnitTest::runAll();
	cerr << "TESTS COMPLETED SUCCESSFULLY!" << endl;

	UnitTest::writeConfiguration();

	return EXIT_SUCCESS;
}
