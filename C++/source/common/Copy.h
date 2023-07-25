#pragma once

template<typename ValueType, unsigned int size>
class Copy {
    public:
        static void copy (ValueType* destination, const ValueType* source) {
            *destination++ = *source++;
            Copy<ValueType, size - 1>::copy (destination, source);
        }
};

// explicit specialization to stop the recursion
template<typename ValueType>
class Copy<ValueType, 0> {
    public:
        static void copy (ValueType* destination, const ValueType* source) {}
};
