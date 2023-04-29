#pragma once

#include "BagThing.h"

MAKE_PTR_TO_SUB(BagBool, BagThing) {
    protected:
        bool value;

    public:
        BagBool (bool _value) : BagThing (BagType::BOOL), value (_value) {}
        virtual ~BagBool () {}
        virtual Text toJson () const { return value ? Text::TRUE : Text ::FALSE; }
        virtual f8 sortValue () const { return value ? 1.0 : 0.0; };
        bool get () { return value; }
};
