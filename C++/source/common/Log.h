#pragma once

#include "RuntimeError.h"

class Log {
    public:
        enum {
            TRACE = 0,
            DEBUG = 1,
            INFO  = 2,
            WARN  = 3,
            ERROR = 4
        };

        class Level {
            private:
                uint level;

            public:
                Level (uint _level) : level (_level) {}
                uint get () const { return level; }
        };

        class Scope {
            private:
                uint filterLevel;
                ostream* stream;

            public:
                Scope () : filterLevel (Log::base ().getFilterLevel ()), stream (Log::base ().getStream ()) { }

                Scope (uint newFilterLevel) : filterLevel (Log::base ().getFilterLevel ()), stream (Log::base ().getStream ()) {
                    Log::base ().setFilterLevel (newFilterLevel);
                }

                // XXX TODO: a Scope constructor that takes a named scope, and sets the new scope
                //           from that, like from a settings file.

                ~Scope () {
                    Log::base ()
                        .setFilterLevel (filterLevel)
                        .setStream (*stream);
                 }
        };

    private:
        ostream* stream;
        uint filterLevel;
        uint level;

        Log (ostream& _stream, uint _filterLevel = INFO, uint _level = INFO) : stream (&_stream), filterLevel (_filterLevel), level (_level) {}

        static Log* log;
        static Log& singleton () {
            if (log == 0) {
            // XXX TODO: load the default settings from a file...
                log = new Log (cerr);
            }
            return *log;
        }

    public:
        template <typename InputType>
        Log& operator << (const InputType& input) {
            if (level >= filterLevel) {
                (*stream) << input;
            }
            return *this;
        }

        // this is the endl handler
        Log& operator << (std::ostream&(*f)(std::ostream&)) {
            if (level >= filterLevel) {
                (*stream) << f;
            }
            return *this;
        }

        Log& operator << (const Level& _level) {
            level = _level.get ();
            return *this;
        }

        ostream* getStream () {
            return stream;
        }

        Log& setStream (std::ostream& _stream) {
            stream = &_stream;
            return *this;
        }

        uint getFilterLevel () {
            return filterLevel;
        }

        Log& setFilterLevel (uint _filterLevel) {
            filterLevel = _filterLevel;
            return *this;
        }

        // global static accessors
        static Log& base () { return singleton (); }
        static Log& trace () { return singleton () << Level (TRACE) << "[TRACE] "; }
        static Log& debug () { return singleton () << Level (DEBUG) << "[DEBUG] "; }
        static Log& info ()  { return singleton () << Level (INFO)  << "[ INFO] "; }
        static Log& warn ()  { return singleton () << Level (WARN)  << "[ WARN] "; }
        static Log& error () { return singleton () << Level (ERROR) << "[ERROR] "; }
        static Log& exception (RuntimeError& runtimeError) { return singleton () << Level (ERROR) << "[EXCPT] " << runtimeError.getMessage () << endl; }
};
