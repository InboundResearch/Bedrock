#pragma once

#include "PtrTo.h"

// Buffer is a block of memory, this is not a resizable entity
MAKE_PTR_TO(Buffer) {
    protected:
        uint capacity;
        uint length;
        byte bytes[1];

        // private constructor
        Buffer (uint _capacity) : capacity (_capacity), length (0) {}

        // we want to allocate RawText objects on 16-byte memory boundaries for performance, so this
        // structure gives us a tool to do that
        struct BlockType { byte a[16]; };

        // function to encapsulate making the raw text pointer, using an already mapped capacity
        static void* makeBufferPtr (uint capacity) {
            // we want to allocate Buffer objects on 16-byte memory boundaries for performance
            uint totalAlloc = sizeof (Buffer) + capacity;
            uint blocksToAlloc = (totalAlloc / sizeof(BlockType)) + ((totalAlloc % sizeof(BlockType)) ? 1 : 0);
            return new BlockType[blocksToAlloc];
        }

    public:
        static PtrToBuffer make (uint capacity) {
            return new (makeBufferPtr (capacity)) Buffer (capacity);
        }

        static PtrToBuffer make (const byte* source, uint sourceLength) {
            PtrToBuffer buffer = new (makeBufferPtr (sourceLength)) Buffer (sourceLength);
            buffer->set (source, sourceLength);
            return buffer;
        }

        ~Buffer () {}

        void operator delete (void* ptr) {
            // the memory was allocated using the new byte[] method
            delete[] reinterpret_cast<BlockType*> (ptr);
        }

        bool set (const byte* source, uint sourceLength) {
            if (sourceLength <= capacity) {
                memcpy (&bytes[0], source, sourceLength);
                length = sourceLength;
                return true;
            } else {
                //throw RuntimeError (Text ("Buffer: ") << "Source length (" << sourceLength << ") is greater than the capacity (" << capacity << ")");
            }
            return false;
        }

        byte* get () {
            return bytes;
        }

        const byte* get () const {
            return bytes;
        }

        byte* fill (uint sourceLength) {
            if (sourceLength <= capacity) {
                length = sourceLength;
                return bytes;
            }
            return 0;
        }

        uint getLength () const {
            return length;
        }

        void setLength (uint sourceLength) {
            length = sourceLength;
        }

        uint getCapacity () const {
            return capacity;
        }

        int compare (const byte* source, uint sourceLength) const {
            uint minLength = min (length, sourceLength);
            int comparison = memcmp (bytes, source, minLength);
            if ((comparison != 0) or (length == sourceLength)) {
                return signum (comparison);
            } else {
                return signum ((int) length - (int) sourceLength);
            }
        }

        int compare (const Buffer* buffer) {
            return compare (buffer->bytes, buffer->length);
        }
};
