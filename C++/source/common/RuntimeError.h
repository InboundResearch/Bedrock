#pragma once

#include "Text.h"

class RuntimeError: public exception {
    protected:
        Text error;
    public:
        explicit RuntimeError(const Text& _error): error (_error) {}
        virtual ~RuntimeError(){}
        virtual const char* what() { return error.get (); }
        Text getMessage () { return error; }
};
