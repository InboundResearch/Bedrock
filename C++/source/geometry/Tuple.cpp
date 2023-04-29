#include "Tuple.h"

template<>
f4 Tuple<f4, 2>::epsilon = 1e-6;
template<>
f4 Tuple<f4, 3>::epsilon = 1e-6;
template<>
f4 Tuple<f4, 4>::epsilon = 1e-6;

template<>
f8 Tuple<f8, 2>::epsilon = 1e-10;
template<>
f8 Tuple<f8, 3>::epsilon = 1e-10;
template<>
f8 Tuple<f8, 4>::epsilon = 1e-10;


