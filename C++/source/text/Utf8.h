#pragma once

#include "Log.h"

// this UTF-8 handling is modeled after the javascript at: https://github.com/mathiasbynens/utf8.js
// for the UTF-8 specification, see: https://encoding.spec.whatwg.org/#utf-8
class Utf8Decoder {
    public:
        enum {
            END_OF_BUFFER = 0,
            UNREAD_BUFFER = 0xfffe,
            ERROR = 0xffff
        };

    private:
        const byte* position;
        const byte* next;
        uint codePoint;

        uint readContinuationByte () {
            if (*next) {
                uint byteN = *next++;
                if ((byteN & 0xc0) == 0x80) {
                    return byteN & 0x3f;
                }
            }

            // error case - hit the end of the buffer in the middle of reading a code point
            throw RuntimeError("UTF-8: unexpected END_OF_BUFFER");
        }

    public:
        // the buffer must be null terminated...
        Utf8Decoder (const void* buffer) : position (0), next (static_cast<const byte*> (buffer)), codePoint (UNREAD_BUFFER) {
        }

        bool goNext () {
            position = next;
            try {
                // if we are at the beginning of a read sequence, and encounter the END_OF_BUFFER,
                // then we will just stop
                if (*next == END_OF_BUFFER) {
                    codePoint = END_OF_BUFFER;
                } else {
                    // Read the first byte, and decide how to proceed based on that
                    uint byte1 = *next++;
                    if ((byte1 & 0x80) == 0) {
                        // 1-byte sequence (0x00 - 0x7f)
                        codePoint = byte1;
                    } else if ((byte1 & 0xe0) == 0xc0) {
                        // 2-byte sequence (0xc2 - 0xdf)
                        uint byte2 = readContinuationByte();
                        codePoint = ((byte1 & 0x1f) << 6) | byte2;
                        if (codePoint < 0x80) {
                            throw RuntimeError("UTF-8: invalid continuation");
                        }
                    } else if ((byte1 & 0xf0) == 0xe0) {
                        // 3-byte sequence (0xe0 - 0xef)
                        uint byte2 = readContinuationByte();
                        uint byte3 = readContinuationByte();
                        codePoint = ((byte1 & 0x0f) << 12) | (byte2 << 6) | byte3;
                        if (codePoint >= 0x0800) {
                            if (codePoint >= 0xd800 and codePoint <= 0xdfff) {
                                throw RuntimeError(Text () << "UTF-8: lone surrogate U+" << hex (codePoint) << " is not a scalar value");
                            }
                        } else {
                            throw RuntimeError("UTF-8: invalid continuation");
                        }
                    } else if ((byte1 & 0xf8) == 0xf0) {
                        // 4-byte sequence (0xf0 - 0xf4)
                        uint byte2 = readContinuationByte();
                        uint byte3 = readContinuationByte();
                        uint byte4 = readContinuationByte();
                        codePoint = ((byte1 & 0x07) << 18) | (byte2 << 12) | (byte3 << 6) | byte4;
                        if ((codePoint < 0x010000) or (codePoint > 0x10ffff)) {
                            throw RuntimeError("UTF8: invalid continuation");
                        }
                    } else {
                        throw RuntimeError("UTF-8: invalid read");
                    }
                }
            } catch (RuntimeError& runtimeError) {
                // set the error condition, and reset so that we will consistently hit that error
                // condition if we keep trying to advance
                Log::exception (runtimeError);
                codePoint = ERROR;
                next = position;
            }
            //Log::debug () << "Utf8::goNext (" << hex (codePoint) << ")" << endl;
            return ((codePoint != END_OF_BUFFER) and (codePoint != ERROR));
        }

        void skip (void) {
            // function to skip ahead in the buffer to find the next valid code point
        }

        uint operator * () {
            return codePoint;
        }

        const byte* getCurrent () {
            return position;
        }
};

