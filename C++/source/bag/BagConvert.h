#pragma once

#include "BagThing.h"
#include "BagText.h"
#include "BagInteger.h"
#include "BagFloat.h"
#include "BagBool.h"

static inline
PtrToBagThing convert (PtrToBagThing bagThing, BagType desiredType) {
    if (bagThing) {
        // try to create a converted thing
        switch (bagThing->getType ()) {
            case BagType::TEXT: {
                switch (desiredType) {
                    case BagType::TEXT:
                        return bagThing;
                    case BagType::INTEGER:
                        return new BagInteger (strtol (ptr_downcast<BagText> (bagThing)->get ().get(), 0, 0));
                    case BagType::FLOAT:
                        return new BagFloat (strtod (ptr_downcast<BagText> (bagThing)->get ().get(), 0));
                    case BagType::BOOL:
                        return new BagBool ((ptr_downcast<BagText> (bagThing)->get () == Text::TRUE) or (strtol (ptr_downcast<BagText> (bagThing)->get ().get(), 0, 0) != 0));
                    case BagType::OBJECT:
                    case BagType::ARRAY:
                        // can't do anything with these...
                        break;
                }
                break;
            }
            case BagType::INTEGER: {
                s8 value = ptr_downcast<BagInteger> (bagThing)->get ();
                switch (desiredType) {
                    case BagType::TEXT:
                        return new BagText (Text () << value);
                    case BagType::INTEGER:
                        return bagThing;
                    case BagType::FLOAT:
                        return new BagFloat (value);
                    case BagType::BOOL:
                        return new BagBool (value != 0);
                    case BagType::OBJECT:
                    case BagType::ARRAY:
                        // can't do anything with these...
                        break;
                }
                break;
            }
            case BagType::FLOAT: {
                f8 value = ptr_downcast<BagFloat> (bagThing)->get ();
                switch (desiredType) {
                    case BagType::TEXT:
                        return new BagText (Text () << value);
                    case BagType::INTEGER:
                        return new BagInteger (s8 (round (value)));
                    case BagType::FLOAT:
                        return bagThing;
                    case BagType::BOOL:
                        return new BagBool (value != 0);
                    case BagType::OBJECT:
                    case BagType::ARRAY:
                        // can't do anything with these...
                        break;
                }
                break;
            }
            case BagType::BOOL: {
                bool value = ptr_downcast<BagBool> (bagThing)->get ();
                switch (desiredType) {
                    case BagType::TEXT:
                        return new BagText (Text () << value);
                    case BagType::INTEGER:
                        return new BagInteger (value ? 1 : 0);
                    case BagType::FLOAT:
                        return new BagFloat (value ? 1 : 0);
                    case BagType::BOOL:
                        return bagThing;
                    case BagType::OBJECT:
                    case BagType::ARRAY:
                        // can't do anything with these...
                        break;
                }
                break;
            }
            case BagType::OBJECT:
            case BagType::ARRAY:
                // can't do anything with these...
                break;

        }
    }

    // if we couldn't do anything, return an empty thing
    return PtrToBagThing ();
}
