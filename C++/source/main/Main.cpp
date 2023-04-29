#include "Common.h"

int main (int argc, char** argv) {
	cerr << "Main" << endl;

	for (int i = 0; i < argc; ++i) {
	    cerr << "Argument (" << i << "): " << argv[i] << endl;
	}

	return EXIT_SUCCESS;
}
