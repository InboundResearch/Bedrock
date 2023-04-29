# Numbers

## Digits

A digit is a representation of a count of things in some base, where the base represents a set of discrete values. In classic human readable terms, we read and write in base 10, a.k.a. decimal, using the digit values 0-9. Computers work in base 2, a.k.a. binary, using the digit values 0 and 1. Programmers sometimes work in base 8, a.k.a. octal, using the digit values 0-7. Programmers often work in base 16, a.k.a. hexadecimal, using the digit values 0-15. We also use base 12 and base 60 in time keeping, and base 360 in angle measurement, though we tend to write all these numbers out in base 10 format.
 
Digits have associated symbols which we have already been using as they are typically well understood. 0-9 are taught to most people, but it can be convenient to use other symbols from the alphabet that have a known intrinsic ordering to represent digit values above 9. Hexadecimal numbers, for instance, are often written using the symbols a-f/A-F to represent the range 10-15.

## Numbers
Digits are written in a right-to-left list to represent values larger than the base. In this positional number system, each position <i>k</i> represents the digit times the base raised to <i>k<sup>th</sup></i> power, starting at the right where <i>k=0</i>.

```
       k = 3210
base  2    1101 = (1 * 2^3) + (1 * 2^2) + (0 * 2^1) + (1 * 2^0) = 13 in base 10
base 10    1234 = (1 * 10^3) + (2 * 10^2) + (3 * 10^1) + (4 * 10^0) = 1234 in base 10
base 16    100a = (1 * 16^3) + (0 * 16^2) + (0 * 16^1) + (10 * 16^0) = 4196
```

We refer to position 0 as the Least Significant Digit (LSD).

Numbers implicitly have an infinite number of leading zeroes, which we typically omit when writing them for convenience, though in some instances it can be useful to format numbers with leading zeroes.

## Digit Operations a.k.a. Math
We can manipulate digits performing the basic operations write, read, add, subtract, multiply, and divide. Composite operations like comparisons and negation are expressed in terms of one of the basic operations, sometimes with an implicit operand (negation is 0 - n, where the 0 is omitted).

All of the basic operations on a single digit can be performed, and the answer will need no more than two digits. We can combine these operations on a positional basis to do all the same operations on larger numbers.

## Big Integers

## Rational Numbers
