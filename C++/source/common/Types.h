#pragma once

// a few basic types, somewhat pedantic...
typedef unsigned char   byte;
typedef          int    sint;

typedef unsigned char   uchar;
typedef unsigned short  ushort;
typedef unsigned int    uint;
typedef unsigned long   ulong;

// if you want to care about the actual size of a storage type in use, these are guaranteed
typedef int8_t          s1;
typedef int16_t         s2;
typedef int32_t         s4;
typedef int64_t         s8;

typedef uint8_t         u1;
typedef uint16_t        u2;
typedef uint32_t        u4;
typedef uint64_t        u8;

typedef float           f4;
typedef double          f8;

// a few definitions to substitute for the standard names
#define bit_and         &
#define bit_or          |
#define complement      ~

// c++ doesn't provide a general ability to change how you view a block of memory in a fundamental
// type, especially when optimizing code that assumes dependence or independence of access to
// variables. but sometimes, that is what we need to do. unfortunately, the compiler will complain
// about "punning" if we do something like "if ((*(ulong*) &aFloat) == (*(ulong*) &anotherFloat))".
//
// this casting operator allows us to explicitly say that we want to change how the compiler treats
// a value in memory. note that it creates a temporary, a copy of the original to accomplish this,
// so avoids the punning problem.
template<typename CastToType, typename CastFromType>
CastToType union_cast (CastFromType& fromValue) {
    union {CastFromType fromValue; CastToType toValue;} unionValue = { .fromValue = fromValue };
    return unionValue.toValue;
}
