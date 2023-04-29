#pragma once

#include "Common.h"

class Pause {
    public:
        static void micro (uint microseconds) {
            // a busy-wait... thread yield resolution is probably measured in milliseconds (3 orders
            // of magnitude lower resolution than we want), so sleeping for short times isn't going
            // to be expedient
            auto startTime = chrono::high_resolution_clock::now();
            while (chrono::duration_cast<chrono::microseconds>(chrono::high_resolution_clock::now() - startTime).count() < microseconds) {}
        }

        static void milli (uint milliseconds) {
            this_thread::sleep_for(chrono::milliseconds(milliseconds));
        }
};
