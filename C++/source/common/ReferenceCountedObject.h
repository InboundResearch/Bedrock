#pragma once

#include "Common.h"

class ReferenceCountedObject {
    private:
        uint referenceCount;

        #ifndef _NDEBUG_
        uint debugId;
        static uint nextDebugId;
        #endif

    public:
        ReferenceCountedObject (void){
            referenceCount = 0;
            #ifndef _NDEBUG_
            debugId = ++nextDebugId;
            #endif
        }

        ReferenceCountedObject (const ReferenceCountedObject& object){
            referenceCount = 0;
            #ifndef _NDEBUG_
            debugId = ++nextDebugId;
            #endif
        }

        virtual ~ReferenceCountedObject (void) {};

        void addRef (void){
            referenceCount++;
        }

        uint removeRef (void){
            return --referenceCount;
        }

        uint getReferenceCount (void) const{
            return referenceCount;
        }
};
