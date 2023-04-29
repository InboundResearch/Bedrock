#include <cstdlib>
#include <iostream>

using namespace std;

#include "Miniball.h"

int main (int argc, char* argv[])
{
    typedef double ScalarType;

    int dimension = 3;
    int n = 1000000;

    // initialize random number generator
    double seed;
    if (argc != 2) {
        seed = 0;
    } else {
        seed = atoi(argv[1]);
    }
    srand (seed);

    // generate random points and store them in a 2-d array
    ScalarType* data = new ScalarType[n * dimension];
    ScalarType** allPoints = new ScalarType*[n];
    for (int i = 0; i < n; ++i) {
        ScalarType* point = allPoints[i] = data + (i * dimension);
        for (int j = 0; j < dimension; ++j) {
            point[j] = (rand () % 100);
        }
    }

    // define the types of iterators through the points and their coordinates
    typedef ScalarType* const* PointIterator;
    typedef const ScalarType* CoordIterator;

    // create an instance of Miniball
    // ------------------------------
    typedef Miniball <CoordinateAccessor<PointIterator, CoordIterator>> MB;
    MB miniball (dimension, allPoints, allPoints + n);

    // output results
    // --------------
    // center
    cout << "Center:\n  ";
    const ScalarType* center = miniball.getCenter();
    for(int i=0; i<dimension; ++i, ++center)
    cout << *center << " ";
    cout << endl;

    // squared radius
    cout << "Squared radius:\n  ";
    cout << miniball.getSquaredRadius() <<  endl;

    // number of support points
    cout << "Number of support points:\n  ";
    cout << miniball.getSupportPointCount() << endl;

    // support points on the boundary determine the smallest enclosing ball, 0 = first point
    cout << "Support point indices (numbers refer to the input order):\n  ";
    for (MB::SupportPointIterator iterator = miniball.getSupportPointsBegin (); iterator != miniball.getSupportPointsEnd (); ++iterator) {
        cout << ((*iterator) - allPoints) << " ";
    }
    cout << endl;

    // relative error: by how much does the ball fail to contain all points?
    //                 tiny positive numbers come from roundoff and are ok
    cout << "Relative error:\n  ";
    ScalarType suboptimality;
    cout << miniball.getRelativeError (suboptimality) <<  endl;

    // suboptimality: by how much does the ball fail to be the smallest
    //                enclosing ball of its support points? should be 0
    //                in most cases, but tiny positive numbers are again ok
    cout << "Suboptimality:\n  ";
    cout << suboptimality <<  endl;

    // validity: the ball is considered valid if the relative error is tiny
    //           (<= 10 times the machine epsilon) and the suboptimality is zero
    cout << "Validity:\n  ";
    cout << (miniball.is_valid() ? "ok" : "possibly invalid") << endl;

    // clean up
    delete[] data;

    return EXIT_SUCCESS;
}
