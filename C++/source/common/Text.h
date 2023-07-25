#pragma once

#include "RawText.h"

class Text {
    private:
        PtrToRawText ptr;

    public:
        Text () : ptr (RawText::getEmpty ()) {}
        Text (const Text& text) : ptr (text.ptr) {}
        Text (const char* source, uint sourceLength) : ptr (RawText::make (source, sourceLength)) {}
        Text (const char* source) : ptr (source ? RawText::make (source, strlen (source)) : RawText::getEmpty ()) {}
        //Text (const PtrToBuffer& buffer) : ptr ((buffer->getLength () > 0) ? RawText::make ((const char*) (buffer->get ()), buffer->getLength ()) : RawText::getEmpty ()) {}
        Text (const PtrToRawText& _ptr) : ptr (_ptr) {}

        // do not subclass this type
        ~Text () {
            ptr = 0;
        }

        uint length () const {
            return ptr->getLength();
        }

        bool isEmpty () {
            return (length () == 0);
        }

        uint capacity () const {
            return ptr->getCapacity();
        }

        const char* get () const {
            return (const char*) (ptr->get ());
        }

        char* get () {
            return (char*) (ptr->get ());
        }

        operator const char* () const {
            return get ();
        }

        operator char* () {
            return get ();
        }

        Text& set (const Text& text) {
            ptr = text.ptr;
            return *this;
        }

        Text& set (const char* source, uint sourceLength) {
            ptr = RawText::make (source, sourceLength);
            return *this;
        }

        Text& set (const char* source) {
            ptr = source ? RawText::make (source, strlen (source)) : RawText::getEmpty ();
            return *this;
        }

        Text& operator = (const Text& text) {
            return set (text);
        }

        Text& operator = (const char* source) {
            return set (source);
        }

        Text& clear () {
            ptr = RawText::getEmpty ();
            return *this;
        }

        PtrToRawText getRawText () const {
            return ptr;
        }

        bool operator == (const Text& text) const {
            return ((ptr == text.ptr) || (compare (text) == 0));
        }

        bool operator == (const char* source) const {
            return (compare (source) == 0);
        }

        bool operator != (const Text& text) const {
            return ((ptr != text.ptr) && (compare (text) != 0));
        }

        bool operator != (const char* source) const {
            return (compare (source) != 0);
        }

        bool operator < (const Text& text) const {
            return (compare (text) < 0);
        }

        bool operator < (const char* source) const {
            return (compare (source) < 0);
        }

        bool operator <= (const Text& text) const {
            return (compare (text) <= 0);
        }

        bool operator <= (const char* source) const {
            return (compare (source) <= 0);
        }

        bool operator >= (const Text& text) const {
            return (compare (text) >= 0);
        }

        bool operator >= (const char* source) const {
            return (compare (source) >= 0);
        }

        bool operator > (const Text& text) const {
            return (compare (text) > 0);
        }

        bool operator > (const char* source) const {
            return (compare (source) > 0);
        }

        Text makeUnique () const {
            return Text (RawText::make(get (), length ()));
        }

        // character access
        char at (uint index) const {
            return (ptr->get ())[index];
        }

        char operator [] (uint index) const {
            return at (index);
        }

        // compare
        int compare (const char* source, uint sourceLength) const {
            return ptr->compare((const byte*) (source), sourceLength);
        }

        int compare (const char* source) const {
            return ptr->compare((const byte*) (source), source ? strlen (source) : 0);
        }

        int compare (const Text& text) const {
            return compare (text.get (), text.length());
        }

        // append
        Text& append (const char* source, uint sourceLength) {
            if (! ptr->append(source, sourceLength)) {
                PtrToRawText newPtr = RawText::make (length () + sourceLength, (const char*) (ptr->get()), ptr->getLength ());
                newPtr->append (source, sourceLength);
                ptr = newPtr;
            }
            return *this;
        }

        Text& append (const char* source) {
            return append (source, source? strlen (source) : 0);
        }

        Text& append (const Text& text) {
            return append (text.get (), text.length());
        }

        int find (const Text& pattern, int offset = 0) const {
            if (static_cast<uint> (offset) < ptr->getLength ()) {
                const char* base = get ();
                const char* match = strstr(base + offset, pattern.get ());
                if (match != 0) {
                    return (match - base);
                }
            }
            return -1;
        }

        Text substring (uint subStart, uint subLength) const {
            uint textLength = length ();
            if (subStart < textLength) {
                subLength = min (textLength - subStart, subLength);
                if (subLength > 0) {
                    return Text (get () + subStart, subLength);
                }
            }
            return Text ();
        }

        Text substring (uint start) const {
            return substring (start, length() - start);
        }

        // split
        vector<Text> splitFirst (const Text& pattern) const {
            vector<Text> result;
            int match = find (pattern);
            if (match >= 0) {
                result.push_back (substring (0, match));
                result.push_back (substring (match + pattern.length ()));
            } else {
                result.push_back (*this);
            }
            return result;
        }

        vector<Text> split (const Text& pattern) const {
            vector<Text> result;
            vector<Text> splitResult = splitFirst (pattern);
            while (splitResult.size() == 2) {
                result.push_back (splitResult[0]);
                splitResult = splitResult[1].splitFirst (pattern);
            }
            result.push_back (splitResult[0]);
            return result;
        }

        Text replace (const Text& pattern, const Text& value) const {
            vector<Text> parts = split (pattern);
            Text result = parts[0];
            for (int i = 1, end = parts.size(); i < end; ++i) {
                result << value << parts[i];
            }
            return result;
        }

        // format

        // operators
        Text& operator += (const Text& text) {
            return append (text);
        }

        Text& operator += (const char* source) {
            return append (source);
        }

        Text& operator << (const Text& text) {
            return append (text);
        }

        Text& operator << (const char* source) {
            return append (source);
        }

        Text& operator << (bool value) {
            return append (value ? TRUE : FALSE);
        }

        // XXX TODO temporary hack, should do something smarter here
        template<typename ValueType>
        Text&  operator << (const ValueType& value) {
            ostringstream out;
            out << fixed << setprecision(8) << value;
            return append (out.str ().c_str ());
        }

        // Text& operator >> (to a value type)

        uint getReferenceCount (void) const{
            return ptr->getReferenceCount();
        }

        bool isUnique () const {
            return ptr.isUnique();
        }

        Text& makeUnique () {
            if (not isUnique()) {
                ptr = RawText::make ((const char*) (ptr->get ()), ptr->getLength ());
            }
            return *this;
        }

        // public statics
        static const Text TRUE;
        static const Text FALSE;
};

inline
bool operator == (const char* lhs, const Text& rhs) {
    return rhs == lhs;
}

inline
bool operator != (const char* lhs, const Text& rhs) {
    return rhs != lhs;
}

inline
Text operator + (const Text& lhs, const Text& rhs) {
    return lhs.makeUnique().append (rhs);
}

inline
Text operator + (const char* lhs, const Text& rhs) {
    return Text (lhs).append (rhs);
}

inline
Text operator + (const Text& lhs, const char* rhs) {
    return lhs.makeUnique().append (rhs);
}

inline
ostream& operator << (ostream& ostr, const Text& text) {
    return ostr << (const char*) text;
}

#define END_LINE    "\n";

// I'm not really sure where these should go...
inline
static Text hex (u8 value, const char* lead = "0x") {
    uint bitsNeeded = static_cast<u8> (ceil (log2 (value)));
    uint bytesNeeded = max ((bitsNeeded >> 3) + ((bitsNeeded & 0x07) ? 1u : 0), 1u);
    ostringstream out;
    out << lead << setfill ('0') << setw (bytesNeeded * 2) << nouppercase << hex << value;
    return Text (out.str ().c_str ());
}

inline
static u8 fromHex (const char* hexString) {
    return strtoul (hexString, 0, 16);
}

inline Text hexBytes (const void* ptr, uint length, uint lineLength = 32, uint groupLength = 2) {
    Text result;
    if (length > lineLength) {
        result << END_LINE;
    }
    const byte* bytes = static_cast<const byte*> (ptr);
    const char* separator;
    const char* lineSeparator = "";
    for (uint i = 0; i < length; ++i) {
        if ((i % lineLength) == 0) {
            result << lineSeparator;
            separator = "";
            lineSeparator = END_LINE;
        }
        if ((i % groupLength) == 0) {
            result << separator;
            separator = " ";
        }
        result << hex (bytes[i], "");
    }
    return result;
}

