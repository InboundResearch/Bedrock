#pragma once

#include "MatrixHelper.h"

template<typename Scalar, uint rows, uint cols>
class Matrix {
    public:
        typedef Matrix<Scalar, rows, cols> Mat;

    private:
        Scalar  nm[cols * rows];

    public:
        Matrix () {}
        Matrix (const Mat& matrix) {
            TupleHelper<Scalar, cols * rows>::copy(nm, matrix.nm);
        }

        Scalar& operator () (uint row, uint column) {
            return nm[column + (cols * row)];
        }

        template <typename enable_if<cols == rows, void>::type* = nullptr>
        Matrix& identity () {
            MatrixIdentityHelper<Scalar, rows, rows, rows>::identity(nm);
            return *this;
        }

        template<uint otherCols>
        static Matrix<Scalar, rows, otherCols>& multiply (const Matrix<Scalar, rows, cols>& left, const Matrix<Scalar, cols, otherCols>& right, Matrix<Scalar, rows, otherCols>& result) {
            //MatrixMultiplyHelper<Scalar, ...>
            return result;
        }

        // fill
        // identity
        // transpose
        // invert

        // operators
        // multiply
        // scalar multiplication
        // add
};


// 2d and 3d matrices
// scale, translate, rotate<Axis>
// frustum viewmatrix, lookfrom, etc.
