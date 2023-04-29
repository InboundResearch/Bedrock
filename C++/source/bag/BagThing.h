#pragma once

#include "RuntimeError.h"

enum struct BagType : uint {
    TEXT,
    INTEGER,
    FLOAT,
    BOOL,
    ARRAY,
    OBJECT
};

#define STREAM_OUT_ENUM(x) case BagType::x: stream << #x; break;
static inline
ostream& operator << (ostream& stream, BagType bagType) {
    switch (bagType) {
        STREAM_OUT_ENUM(TEXT);
        STREAM_OUT_ENUM(INTEGER);
        STREAM_OUT_ENUM(FLOAT);
        STREAM_OUT_ENUM(BOOL);
        STREAM_OUT_ENUM(ARRAY);
        STREAM_OUT_ENUM(OBJECT);
    }
    return stream;
}
#undef STREAM_OUT_ENUM

MAKE_PTR_TO(BagThing) {
    protected:
    BagType type;

        static Text enclose (const Text& value, const Text& beginEnclosure, const Text& endEnclosure) {
            return Text () << beginEnclosure << value << endEnclosure;
        }

        static Text enclose (const Text& value, const Text& enclosure) {
            return enclose (value, enclosure, enclosure);
        }

        static Text enquote (const Text& value) {
            return enclose (value, "\"");
        }

    public:
        BagThing (BagType _type) : type (_type) {}
        virtual ~BagThing () {}

        BagType getType () const { return type; };
        virtual Text toJson () const = 0;
        virtual f8 sortValue () const = 0;
};
