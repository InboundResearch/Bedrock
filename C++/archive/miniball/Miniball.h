#include <cassert>
#include <algorithm>
#include <list>
#include <ctime>
#include <limits>

// Global Functions
template <typename ScalarType>
inline ScalarType square (ScalarType r) { return r * r; }

// point type - tuple of scalars
// coordinate iterator

// Functors
// functor to map a point iterator to the corresponding coordinate iterator;
// generic version for points whose coordinate containers have begin()
template < typename PointIterator_, typename CoordinateIterator_ >
struct CoordinateAccessor {
    typedef PointIterator_ PointIterator;
    typedef CoordinateIterator_ CoordinateIterator;
    inline  CoordinateIterator operator() (PointIterator it) const { return (*it).begin(); }
};

// partial specialization for points whose coordinate containers are arrays
template < typename PointIterator_, typename CoordinateIterator_ >
struct CoordinateAccessor<PointIterator_, CoordinateIterator_*> {
    typedef PointIterator_ PointIterator;
    typedef CoordinateIterator_* CoordinateIterator;
    inline  CoordinateIterator operator() (PointIterator it) const { return *it; }
};

// Class Declaration

template <typename CoordinateAccessor>
class Miniball {
    private:

        // The iterator type to go through the input points
        typedef typename CoordinateAccessor::PointIterator PointIterator;
        // The iterator type to go through the coordinates of a single point.
        typedef typename CoordinateAccessor::CoordinateIterator CoordinateIterator;
        // The coordinate type
        typedef typename std::iterator_traits<CoordinateIterator>::value_type ScalarType;
        // The iterator to go through the support points
        typedef typename std::list<PointIterator>::iterator SupportIterator;

        typedef ScalarType* Coordinate;

        // data members...
        const int dimension; // dimension
        const int dimensionPlus1;
        PointIterator pointsBegin;
        PointIterator pointsEnd;
        CoordinateAccessor coordinateAccessor;

        //...for the algorithms
        std::list<PointIterator> supportPoints;
        SupportIterator supportPointsEnd;
        int fsize;   // number of forced points
        int ssize;   // number of support points

        // ...for the ball updates
        Coordinate  currentCenter;
        ScalarType  currentSquaredRadius;
        Coordinate* c;
        ScalarType* squaredRadius;

        // helper arrays
        ScalarType* q0;
        ScalarType* z;
        ScalarType* f;
        Coordinate* v;
        Coordinate* a;

        // by how much do we allow points outside?
        ScalarType defaultTolerance;

        void moveToFront (SupportIterator j){
            if (supportPointsEnd == j) {
                supportPointsEnd++;
            }
            supportPoints.splice (supportPoints.begin(), supportPoints, j);
        }

        void algorithmMoveToFront (SupportIterator n){
            // Algorithm 1: mtf_mb (L_{n-1}, B), where L_{n-1} = [L.begin, n)
            // B: the set of forced points, defining the current ball
            // S: the superset of support points computed by the algorithm
            // --------------------------------------------------------------
            // from B. Gaertner, Fast and Robust Smallest Enclosing Balls, ESA 1999,
            // http://www.inf.ethz.ch/personal/gaertner/texts/own_work/esa99_final.pdf

            //   PRE: B = S
            assert (fsize == ssize);

            supportPointsEnd = supportPoints.begin();
            if ((fsize) == dimensionPlus1) {
                return;
            }

            // incremental construction
            for (SupportIterator i = supportPoints.begin(); i != n;)
            {
                // INV: (supportPointsEnd - supportPoints.begin() == |S|-|B|)
                assert (std::distance (supportPoints.begin(), supportPointsEnd) == ssize - fsize);

                // let i move to the next point, and j holds a reference to the last one
                SupportIterator j = i++;

                // if the point j is outside the current circle
                if ((excess(*j) > 0) and (push(*j))) {   // B := B + p_i
                    algorithmMoveToFront (j);            // algorithmMoveToFront (L_{i-1}, B + p_i)
                    pop();                               // B := B - p_i
                    moveToFront(j);
                }
            }
            // POST: the range [L.begin(), supportPointsEnd) stores the set S\B
        }

        void algorithmPivot (PointIterator n) {
            // Algorithm 2: pivot_mb (L_{n-1}), where L_{n-1} = [L.begin, n)
            // --------------------------------------------------------------
            // from B. Gaertner, Fast and Robust Smallest Enclosing Balls, ESA 1999,
            // http://www.inf.ethz.ch/personal/gaertner/texts/own_work/esa99_final.pdf
            ScalarType old_sqr_r;
            const ScalarType* c;
            PointIterator pivot, k;
            ScalarType e, max_e, squaredRadius;
            CoordinateIterator p;
            unsigned int loops_without_progress = 0;
            do {
                old_sqr_r = currentSquaredRadius;
                squaredRadius = currentSquaredRadius;

                pivot = pointsBegin;
                max_e = 0;
                for (k = pointsBegin; k != n; ++k) {
                    p = coordinateAccessor(k);
                    e = -squaredRadius;
                    c = currentCenter;
                    for (int j = 0; j < dimension; ++j) {
                        e += square<ScalarType>(*p++ - *c++);
                    }
                    if (e > max_e) {
                        max_e = e;
                        pivot = k;
                    }
                }

                if ((squaredRadius < 0) or (max_e > (squaredRadius * defaultTolerance))) {
                    // check if the pivot is already contained in the support set
                    if (std::find(supportPoints.begin(), supportPointsEnd, pivot) == supportPointsEnd) {
                        assert (fsize == 0);
                        if (push (pivot)) {
                            algorithmMoveToFront(supportPointsEnd);
                            pop();
                            pivotMoveToFront(pivot);
                        }
                    }
                }
                if (old_sqr_r < currentSquaredRadius) {
                    loops_without_progress = 0;
                } else {
                    ++loops_without_progress;
                }
            } while (loops_without_progress < 2);
        }

        void pivotMoveToFront (PointIterator j){
            supportPoints.push_front(j);
            if (std::distance(supportPoints.begin(), supportPointsEnd) == (dimension + 2)) {
              supportPointsEnd--;
            }
        }

        ScalarType excess (PointIterator pointIterator) const {
            CoordinateIterator p = coordinateAccessor(pointIterator);
            ScalarType e = -currentSquaredRadius;
            ScalarType* c = currentCenter;
            for (int k=0; k<dimension; ++k){
              e += square<ScalarType>(*p++-*c++);
            }
            return e;
        }

        void pop () {
            --fsize;
        }

        bool push (PointIterator pointIterator) {
            int i, j;
            ScalarType eps = square<ScalarType>(std::numeric_limits<ScalarType>::epsilon());

            CoordinateIterator cit = coordinateAccessor(pointIterator);
            CoordinateIterator p = cit;

            if (fsize == 0) {
                for (i = 0; i < dimension; ++i) {
                    q0[i] = *p++;
                }
                for (i = 0; i < dimension; ++i) {
                    c[0][i] = q0[i];
                }
                squaredRadius[0] = 0;
            }
            else {
                // set v_fsize to Q_fsize
                for (i = 0; i < dimension; ++i) {
                    v[fsize][i] = *p++ - q0[i];
                }

                // compute the a_{fsize,i}, i < fsize
                for (i = 1; i < fsize; ++i) {
                    a[fsize][i] = 0;
                    for (j = 0; j < dimension; ++j) {
                        a[fsize][i] += v[i][j] * v[fsize][j];
                    }
                    a[fsize][i] *= (2 / z[i]);
                }

                // update v_fsize to Q_fsize-\bar{Q}_fsize
                for (i = 1; i < fsize; ++i) {
                    for (j = 0; j < dimension; ++j) {
                        v[fsize][j] -= a[fsize][i] * v[i][j];
                    }
                }

                // compute z_fsize
                z[fsize] = 0;
                for (j = 0; j < dimension; ++j) {
                    z[fsize] += square<ScalarType> (v[fsize][j]);
                }
                z[fsize] *= 2;

                // reject push if z_fsize too small
                if (z[fsize] < (eps * currentSquaredRadius)) {
                    return false;
                }

                // update c, squaredRadius
                p=cit;
                ScalarType e = -squaredRadius[fsize-1];
                for (i = 0; i < dimension; ++i) {
                    e += square<ScalarType> (*p++ - c[fsize-1][i]);
                }
                f[fsize] = e / z[fsize];

                for (i = 0; i < dimension; ++i) {
                    c[fsize][i] = c[fsize-1][i] + (f[fsize] * v[fsize][i]);
                }
                squaredRadius[fsize] = squaredRadius[fsize-1] + (e * f[fsize] / 2);
            }
            currentCenter = c[fsize];
            currentSquaredRadius = squaredRadius[fsize];
            ssize = ++fsize;
            return true;
        }

        ScalarType getSuboptimality () const {
            ScalarType* l = new ScalarType[dimensionPlus1];
            ScalarType min_l = 0;
            l[0] = ScalarType(1);
            for (int i=ssize-1; i>0; --i) {
              l[i] = f[i];
              for (int k=ssize-1; k>i; --k)
            l[i]-=a[k][i]*l[k];
              if (l[i] < min_l) min_l = l[i];
              l[0] -= l[i];
            }
            if (l[0] < min_l) min_l = l[0];
            delete[] l;
            if (min_l < 0)
              return -min_l;
            return 0;
        }

        void createArrays(){
            // preallocate a big block of memory, and split it up over everything - this will help
            // with cache coherency
            int dimdim = dimension * dimensionPlus1;
            int scalarCount = (3 * dimdim) + (3 * dimensionPlus1) + dimension;
            int pointerCount = 3 * dimensionPlus1;
            int bufferSize = (scalarCount * sizeof (ScalarType)) + (pointerCount * sizeof (ScalarType*));
            char* buffer = new char[bufferSize];

            ScalarType** pointerBuffer = (ScalarType**) buffer;
            c = pointerBuffer;
            v = c + dimensionPlus1;
            a = v + dimensionPlus1;

            ScalarType* scalarBuffer = (ScalarType*) (a + dimensionPlus1);

            c = new ScalarType*[dimensionPlus1]; c[0] = scalarBuffer;
            v = new ScalarType*[dimensionPlus1]; v[0] = c[0] + dimdim;
            a = new ScalarType*[dimensionPlus1]; a[0] = v[0] + dimdim;
            for (int i = 1; i < dimensionPlus1; ++i) {
                c[i] = c[0] + (i * dimension);
                v[i] = v[0] + (i * dimension);
                a[i] = a[0] + (i * dimension);
            }
            squaredRadius = a[0] + dimdim; //new ScalarType[dimensionPlus1];
            q0 = squaredRadius + dimensionPlus1; //new ScalarType[dimension];
            z = q0 + dimension; //new ScalarType[dimensionPlus1];
            f = z + dimensionPlus1; //new ScalarType[dimensionPlus1];
        }

        void deleteArrays() {
            delete[] c;
        }

    public:
        // The iterator type to go through the support points
        typedef typename std::list<PointIterator>::const_iterator SupportPointIterator;

        // PRE:  [begin, end) is a nonempty range
        // POST: computes the smallest enclosing ball of the points in the range
        //       [begin, end); the functor a maps a point iterator to an iterator
        //       through the dimension coordinates of the point
        Miniball (int dimension_, PointIterator begin, PointIterator end, CoordinateAccessor coordinateAccessor_ = CoordinateAccessor()) :
                dimension (dimension_),
                dimensionPlus1 (dimension_ + 1),
                pointsBegin (begin),
                pointsEnd (end),
                coordinateAccessor (coordinateAccessor_),
                supportPoints(),
                supportPointsEnd (supportPoints.begin()),
                fsize(0),
                ssize(0),
                currentCenter (NULL),
                currentSquaredRadius (ScalarType(-1)),
                c (NULL),
                squaredRadius (NULL),
                q0 (NULL),
                z (NULL),
                f (NULL),
                v (NULL),
                a (NULL),
                defaultTolerance (ScalarType(10) * std::numeric_limits<ScalarType>::epsilon())
        {
            assert (pointsBegin != pointsEnd);
            createArrays();

            // set initial center
            for (int j=0; j<dimension; ++j) {
                c[0][j] = 0;
            }
            currentCenter = c[0];

            // compute miniball
            algorithmPivot (pointsEnd);
        }

        // POST: returns a pointer to the first element of an array that holds
        //       the dimension coordinates of the center of the computed ball
        const ScalarType* getCenter () const {
            return currentCenter;
        }

        // POST: returns the squared radius of the computed ball
        ScalarType getSquaredRadius () const {
            return currentSquaredRadius;
        }

        // POST: returns the number of support points of the computed ball;
        //       the support points form a minimal set with the same smallest
        //       enclosing ball as the input set; in particular, the support
        //       points are on the boundary of the computed ball, and their
        //       number is at most dimension+1
        int getSupportPointCount () const {
            assert (ssize < dimension+2);
            return ssize;
        }

        // POST: returns an iterator to the first support point
        SupportPointIterator getSupportPointsBegin () const {
            return supportPoints.begin();
        }

        // POST: returns a past-the-end iterator for the range of support points
        SupportPointIterator getSupportPointsEnd () const {
            return supportPointsEnd;
        }

        // POST: returns the maximum excess of any input point w.r.t. the computed
        //       ball, divided by the squared radius of the computed ball. The
        //       excess of a point is the difference between its squared distance
        //       from the center and the squared radius; Ideally, the return value
        //       is 0. subopt is set to the absolute value of the most negative
        //       coefficient in the affine combination of the support points that
        //       yields the center. Ideally, this is a convex combination, and there
        //       is no negative coefficient in which case subopt is set to 0.
        ScalarType getRelativeError (ScalarType& subopt) const{
            ScalarType e, max_e = 0;
            // compute maximum absolute excess of support points
            for (SupportPointIterator it = getSupportPointsBegin(); it != getSupportPointsEnd(); ++it) {
                e = excess (*it);
                if (e < 0) e = -e;
                if (e > max_e) {
                    max_e = e;
                }
            }
            // compute maximum excess of any point
            for (PointIterator i = pointsBegin; i != pointsEnd; ++i)
              if ((e = excess (i)) > max_e)
            max_e = e;

            subopt = getSuboptimality();
            assert (currentSquaredRadius > 0 or max_e == 0);
            return (currentSquaredRadius == 0 ? 0 : max_e / currentSquaredRadius);
        }


        // POST: return true if the relative error is at most tol, and the
        //       suboptimality is 0; the default tolerance is 10 times the
        //       coordinate type's machine epsilon
        bool is_valid () const {
            ScalarType suboptimality;
            return ((getRelativeError (suboptimality) <= defaultTolerance) and (suboptimality == 0));
        }

        // POST: deletes dynamically allocated arrays
        ~Miniball(){
            deleteArrays();
        }
};
