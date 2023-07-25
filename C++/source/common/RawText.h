#pragma once

#include "Buffer.h"

// RawText is generally presumed to be a buffer containing a valid UTF-8 encoding of text. As such,
// no wide-char support is provided, nor is there a direct indexing capability. the user must read
// the text buffer as a stream text as a stream. NOTE: this means that count and length might be two
// different things...
MAKE_PTR_TO_SUB(RawText, Buffer) {
    private:
        // we will use capacities that are powers of 2 in size to make growing strings logarithmic
        // in time complexity, but after a certain size it's probably better to grow incrementally.
        enum { MAX_CAPACITY = 16384 };

        // an empty string is generally useful, but we don't want lots of them running around
        static PtrToRawText empty;

        static uint mapCapacity (uint capacity) {
            if (capacity < MAX_CAPACITY) {
                // bump capacity to the next power of 2 above it
                uint bitsNeeded = static_cast<uint> (ceil (log2 (capacity)));
                capacity = 1 << bitsNeeded;
            } else {
                // make the size be the next multiple of MAX_CAPACITY
                capacity = ((capacity / MAX_CAPACITY) + 1) * MAX_CAPACITY;
            }
            return capacity;
        }

        // capacity is always one less than the total capacity, so there is always room to include
        // a null terminator for the string
        RawText (uint _capacity) : Buffer (_capacity) {
            bytes[0] = 0;
        }

        RawText (uint _capacity, const char* source, uint sourceLength) : Buffer (_capacity) {
            set (source, sourceLength);
        }

    public:
        static PtrToRawText make (uint capacity) {
            capacity = mapCapacity (capacity);
            return new (makeBufferPtr (capacity)) RawText (capacity);
        }

        static PtrToRawText make (uint capacity, const char* source, uint sourceLength) {
            capacity = mapCapacity (capacity);
            return new (makeBufferPtr (capacity)) RawText (capacity, source, sourceLength);
        }

        static PtrToRawText make (const char* source, uint sourceLength) {
            return make (sourceLength, source, sourceLength);
        }

        ~RawText () {}
/*
        void operator delete (void* ptr) {
            // the memory was allocated using the new byte[] method
            delete[] reinterpret_cast<BlockType*> (ptr);
        }
*/
        bool set (const char* source, uint sourceLength) {
            if (Buffer::set ((const byte*) (source), sourceLength)) {
                bytes[length] = 0;
                return true;
            }
            return false;
        }

        /*
        bool set (const char* source) {
            return set (source, strlen (source));
        }
        */

        char* fill (uint sourceLength) {
            if (sourceLength <= capacity) {
                length = sourceLength;
                bytes[length] = 0;
                return (char*) bytes;
            }
            return 0;
        }

        bool append (const char* source, uint sourceLength) {
            if ((length + sourceLength) <= capacity) {
                memcpy (&bytes[length], source, sourceLength);
                length += sourceLength;
                bytes[length] = 0;
                return true;
            }
            return false;
        }

        /*
        bool append (const RawText* rawText) {
            return append (rawText->bytes, rawText->length);
        }
        */

        /*
        bool append (const char* source) {
            return append (source, strlen (source));
        }
        */

        static PtrToRawText getEmpty () {
            if (! empty) {
                empty = new RawText (0);
            }
            return empty;
        }
};
