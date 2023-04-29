#pragma once

#include "TupleHelper.h"


template<typename Scalar, uint n, uint m, uint size>
class MatrixIdentityHelper {
    public:
        // only defined for square matrices
        static void identity (Scalar* destination) {
            *destination = (m == n) ? 1.0 : 0.0;
            //cerr << "m: " << m << ", n: " << n << ", v: " << *destination << endl;
            MatrixIdentityHelper<Scalar, n, m - 1, size>::identity (destination + 1);
        }
};

template<typename Scalar, uint n, uint size>
class MatrixIdentityHelper<Scalar, n, 0, size> {
    public:
        // only defined for square matrices
        static void identity (Scalar* destination) {
            MatrixIdentityHelper<Scalar, n - 1, size, size>::identity (destination);
        }
};

template<typename Scalar, uint size>
class MatrixIdentityHelper<Scalar, 1, 0, size> {
    public:
        // only defined for square matrices
        static void identity (Scalar* destination) { }
};


