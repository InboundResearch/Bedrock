#include "Digit.h"

// the max value of a digit by radix, that repreents a "digit" that can be converted in isolation -
// used to repeatedly divide a number in the process of converting a bunch of digits to a string
uDigit	Digit::digitDivisorByRadix[] = {
        0, 0, 2147483648, 1162261467, 1073741824, 1220703125, 2176782336, 1977326743, 1073741824,
        3486784401, 1000000000, 2357947691, 429981696, 815730721, 1475789056, 2562890625, 268435456,
        410338673, 612220032, 893871739, 1280000000, 1801088541, 2494357888, 3404825447, 191102976,
        244140625, 308915776, 387420489, 481890304, 594823321, 729000000, 887503681, 1073741824,
        1291467969, 1544804416, 1838265625, 2176782336
};

// the maximum number of characters needed to represent a digit, by radix (0-36)
int	Digit::maxLengthByRadix[] = {
        0, 0, 32, 21, 16, 14, 13, 12, 11, 11, 10, 10, 9, 9, 9, 9, 8, 8, 8, 8, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
};

// the mapping from a radix'd digit value to a character, and back (the ascii table values
// corresponding to the characters), we support radix 2-36
const char* Digit::digit2char = "0123456789abcdefghijklmnopqrstuvwxyz";
uDigit	Digit::char2digit[] = {
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
		 0,  1,  2,  3,  4,  5,  6,  7,  8,  9,  0,  0,  0,  0,  0,  0,
		 0, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
		25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,  0,  0,  0,  0,  0,
		 0, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
		25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,  0,  0,  0,  0,  0
};



