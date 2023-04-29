#pragma once

#include "Bag.h"
#include "Utf8.h"
#include "File.h"

// this Json reader is loosely modeled after a JSON parser grammar from http://www.json.org, and
// implemented for the bedrock libraries in Java (https://github.com/brettonw/Bedrock). the main
// difference is that we ignore differences between value types (all of them will be text in their
// internal representation), and assume the input is a well formed string representation of a
// BagObject or BagArray in JSON-ish format

class Json {
    private:
        const Text& input;
        Utf8Decoder decoder;
        int lineNumber;
        const byte* lastLine;
        bool error;

        static set<uint> bareValueStopChars;
        static set<uint> quotedStringStopChars;

        void populateSet (set<uint>& set, const char* chars) {
            if (set.size () == 0) {
                Utf8Decoder decoder (chars);
                while (decoder.goNext ()) {
                    set.insert(*decoder);
                }
            }
        }

        Json (const Text& _input) : input (_input), decoder (input.get()), lineNumber (1), error (false) {
            populateSet (bareValueStopChars, " \u00a0\t\n:{}[]\",");
            populateSet (quotedStringStopChars, "\n\"");

            // start the parser
            decoder.goNext ();
            lastLine = decoder.getCurrent();
        }

        void onReadError (const Text& errorMessage) {

            // log the messages, we only need to output the line if this is the first time the error is
            // being reported
            if (! error) {
                // say where the error is
                Log& log = Log::error ();
                log << "Error while parsing input on line " << lineNumber << ", near: " << endl;

                // find the end of the current line. note: line endings could only be '\n' because the
                // input reader consumed the actual line endings for us and replaced them with '\n'
                const byte* errorPosition = decoder.getCurrent();
                while (*decoder and (*decoder != '\n')) {
                    decoder.goNext ();
                }
                Text errorLine ((const char*)(lastLine), decoder.getCurrent() - lastLine);
                log << errorLine << endl;

                // build the error message, by computing a carat line, and adding the error message to it
                Text caratLine;
                uint spaces = errorPosition - lastLine;
                for (uint i = 0; i < spaces; ++i) {
                    caratLine << " ";
                }
                caratLine << "^";
                log << caratLine << endl << errorMessage << endl;

                // set the error state
                error = true;
            }
        }

        bool check () {
            return (not error) and (*(decoder.getCurrent()));
        }

        void consumeWhiteSpace () {
            // consume white space (space, carriage return, tab, etc.
            while (check ()) {
                switch (*decoder) {
                    // tab, space, nbsp (\u00a0)
                    case '\t': case ' ': case 0xc2a0:
                        decoder.goNext();
                        break;
                    // carriage return - the file reader converts all returns to \n
                    case '\n':
                        decoder.goNext();
                        ++lineNumber;
                        lastLine = decoder.getCurrent();
                        break;
                    default:
                        return;
                }
            }
        }

        bool expect(uint c) {
            consumeWhiteSpace ();

            // the next character should be the one we expect
            if (check() and (*decoder == c)) {
                decoder.goNext();
                return true;
            }
            return false;
        }

        bool require (bool condition, const Text& explanation) {
            if (! condition) {
                throw RuntimeError(Text ("Json: ") << explanation << " required");;
            }
            return condition;
        }

        bool require(uint c) {
            return require (expect (c), Text ("'") << char (c) << "'");
        }

        bool notIn (const set<uint>& stopChars, uint c) {
            return (stopChars.find(c) == stopChars.end());
        }

        const byte* consumeUntilStop (const set<uint>& stopChars) {
            const byte* start = decoder.getCurrent();
            uint c;
            while (check () and notIn (stopChars, (c = *decoder))) {
                // using the escape mechanism is like a free pass for the next character, but we
                // don't do any transformation on the substring, just return it as written
               if (c == '\\') {
                   decoder.goNext();
               }
               decoder.goNext();
            }
            return start;
        }

        Text readString () {
            // " chars "
            if (expect('"')) {
                // digest the string, and eat the end quote
                const byte* start = consumeUntilStop (quotedStringStopChars);
                const byte* stop = decoder.getCurrent();
                decoder.goNext ();
                Text string ((const char*)(start), stop - start);
                Log::trace () << "readString (" << string << ")" << endl;
                return string;
            }

            // if require fails, it will throw an exception, so this won't happen
            return Text ();
        }

        Text readBareString () {
            // <allowed chars>

            // technically, we're being sloppy allowing some bare values that would normally have
            // to be quoted strings, but it's part of the simplified structure we support. this
            // allows us to read valid JSON files without handling every single pedantic case.
            const byte* start = consumeUntilStop (bareValueStopChars);
            const byte* stop = decoder.getCurrent();
            Text string ((const char*)(start), stop - start);
            Log::trace () << "readBareString (" << string << ")" << endl;
            return string;
        }

        PtrToBagThing readValue () {
            // <Value> ::= <String> | <Object> | <Array>
            consumeWhiteSpace ();

            PtrToBagThing value;
            if (check ()) {
                switch (*decoder) {
                    case '{': value = ptr_upcast<BagThing> (readBagObject ()); break;
                    case '[': value = ptr_upcast<BagThing> (readBagArray ()); break;
                    case '"': value = ptr_upcast<BagThing> (PtrToBagText (new BagText (readString ()))); break;
                    default: value = ptr_upcast<BagThing> (PtrToBagText (new BagText (readBareString ()))); break;
                }
            }
            return value;
        }

        bool storeValue (PtrToBagObject bagObject, Text key) {
            // the goal here is to try to read a "value" from the input stream, and store it into the
            // BagObject. BagObject can NOT store null values, so we have a special handling case to
            // make sure we properly convert "null" string to null value - as distinguished from a failed
            // read, which returns null value to start. the method returns true if a valid value was
            // fetched from the stream, regardless of whether a null value was stored in the BagObject.
            PtrToBagThing value = readValue ();
            if (value) {
                // special case for "null"
                if (not ((value->getType() == BagType::TEXT) and (ptr_downcast<BagText> (value)->get() == "null"))) {
                    bagObject->put (key, value);
                }
                return true;
            }
            return false;
        }

        bool readPair (PtrToBagObject bagObject) {
            // <Pair> ::= <String> : <Value>
            Text key = readString ();
            return key and (key.length () > 0) and require (':') and require (storeValue (bagObject, key), "Valid value");
        }

        bool readMembers (PtrToBagObject bagObject) {
            // <Members> ::= <Pair> | <Pair> , <Members>
            bool result = true;
            if (readPair (bagObject)) {
                while (expect (',')) {
                    result = require (readPair (bagObject), "Valid pair (<String>:<Value>)");
                }
            }
            return result;
        }

        PtrToBagObject readBagObject () {
            // <Object> ::= { } | { <Members> }
            PtrToBagObject bagObject = new BagObject ();
            return (expect('{') and readMembers (bagObject) and require(expect ('}'), "Valid pair (<String>:<Value>) or '}'")) ? bagObject : PtrToBagObject ();
        }

        bool storeValue (PtrToBagArray bagArray) {
            // the goal here is to try to read a "value" from the input stream, and store it into the
            // BagArray. BagArrays can store null values, so we have a special handling case to make
            // sure we properly convert "null" string to null value - as distinguished from a failed
            // read, which returns null value to start.the method returns true if a valid value was
            // fetched from the stream (in which case it was added to the BagArray)
            PtrToBagThing value = readValue ();
            if (value) {
                // special case for "null"
                if ((value->getType() == BagType::TEXT) and (ptr_downcast<BagText> (value)->get() == "null")) {
                    value = PtrToBagThing ();
                }
                bagArray->add (value);
                return true;
            }
            return false;
        }

        bool readElements (PtrToBagArray bagArray) {
            // <Elements> ::= <Value> | <Value> , <Elements>
            bool result = true;
            if (storeValue (bagArray)) {
                while (expect (',')) {
                    result = require (storeValue (bagArray), "Valid value");
                }
            }
            return result;
        }

        PtrToBagArray readBagArray () {
            // <Array> :: [ ] | [ <Elements> ]
            PtrToBagArray bagArray = new BagArray ();
            return (expect('[') and readElements (bagArray) and require(']')) ? bagArray : PtrToBagArray ();
        }

    public:
        static PtrToBagObject readBagObject (const Text& text) {
            try {
                Json json (text);
                try {
                    return json.readBagObject();
                } catch (RuntimeError& runtimeError) {
                    json.onReadError (runtimeError.getMessage());
                }
            } catch (RuntimeError& runtimeError) {
                // this indicates a UTF read failure in the first code
                Log::exception(runtimeError);
            }
            return PtrToBagObject ();
        }

        static PtrToBagArray readBagArray (const Text& text) {
            try {
                Json json (text);
                try {
                    return json.readBagArray();
                } catch (RuntimeError& runtimeError) {
                    json.onReadError (runtimeError.getMessage());
                }
            } catch (RuntimeError& runtimeError) {
                // this indicates a UTF read failure in the first code
                Log::exception(runtimeError);
            }
            return PtrToBagArray ();
        }

        static PtrToBagThing readBagThing (const Text& text) {
            try {
                Json json (text);
                try {
                    return json.readValue();
                } catch (RuntimeError& runtimeError) {
                    json.onReadError (runtimeError.getMessage());
                }
            } catch (RuntimeError& runtimeError) {
                // this indicates a UTF read failure in the first code
                Log::exception(runtimeError);
            }
            return PtrToBagThing ();
        }

        static PtrToBagThing readFile (const PtrToFile& file) {
            if (file->getExists ()) {
                Text text = file->readText();
                return readBagThing(text);
            }
            return PtrToBagThing ();
        }

        static PtrToBagThing readFile (const Text& filename) {
            return readFile (new File (filename));
        }
};
