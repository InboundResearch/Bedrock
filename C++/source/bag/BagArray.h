#pragma once

#include "BagSort.h"

class BagArray : public BagContainer {
    protected:
        vector<PtrToBagThing> value;
        friend class BagArraySort;

        PtrToBagArray add (BagThing* bagThing) {
            value.push_back (bagThing);
            return this;
        }

        BagArray (const BagArray& source) : BagContainer (BagType::ARRAY), value (source.value) {}

        template<typename SortType>
        static PtrToBagArray withSortAdapter (const PtrToBagArray& source, const SortType& sortType) {
            SortAdapter<SortType> sortAdapter (sortType);
            PtrToBagArray copy = new BagArray (*source);
            ::sort (copy->value.begin(), copy->value.end(), sortAdapter);
            return copy;
        }

    public:
        BagArray () : BagContainer (BagType::ARRAY) {}
        BagArray (const PtrToBagArray& source) : BagContainer (BagType::ARRAY), value (source->value) {}

        virtual ~BagArray () {}

        virtual Text toJson () const {
            Text prepend = "";
            Text out;
            for (vector<PtrToBagThing>::const_iterator it = value.begin (); it != value.end (); ++it) {
                out << prepend << (*it)->toJson ();
                prepend = ",";
            }
            return enclose (out, "[", "]");
        }

        template<typename BagThingSubtype>
        PtrToBagArray add (PtrTo<BagThingSubtype> bagThing) {
            value.push_back (ptr_upcast<BagThing>(bagThing));
            return this;
        }

        PtrToBagArray add(const Text& thing) {
            return add (new BagText (thing));
        }

        // old C++ - we either add this explicit conversion, or remove the
        // overload that handles bool types, as the compiler implicitly
        // chooses that conversion over this one
        PtrToBagArray add(const char* thing) {
            return add (Text (thing));
        }

        PtrToBagArray add(s4 thing) {
            return add (new BagInteger (thing));
        }

        PtrToBagArray add(s8 thing) {
            return add (new BagInteger (thing));
        }

        PtrToBagArray add(f8 thing) {
            return add (new BagFloat (thing));
        }

        PtrToBagArray add(bool thing) {
            return add (new BagBool (thing));
        }

        uint size () const {
            return value.size();
        }

        PtrToBagThing get (uint index) {
            return (index < value.size ()) ? value[index] : PtrToBagThing ();
        }

        const PtrToBagThing get (uint index) const {
            return (index < value.size ()) ? value[index] : PtrToBagThing ();
        }

        virtual const PtrToBagThing get (const Text& path) const {
            vector<Text> parts = path.splitFirst ("/");
            uint index = atoi (parts[0].get ());
            PtrToBagThing ptrToBagThing = get (index);
            if ((parts.size () == 2) and ptrToBagThing) {
                switch (ptrToBagThing->getType ()) {
                    case BagType::OBJECT:
                    case BagType::ARRAY:
                        return ptr_downcast<BagContainer> (ptrToBagThing)->get (parts[1]);
                    default:
                        return PtrToBagThing ();
                }
            }
            return ptrToBagThing;
        }

        #define GET_TYPE(bagType, bagTypeType, baseType)                                            \
            baseType get##bagType (uint index) const {                                              \
                PtrToBagThing bagThing = convert (get (index), BagType::bagTypeType);               \
                if (bagThing) {                                                                     \
                    PtrToBag##bagType bag##bagType = ptr_downcast<Bag##bagType> (bagThing);         \
                    baseType thing = bag##bagType->get ();                                          \
                    return thing;                                                                   \
                }                                                                                   \
                throw RuntimeError (Text ("BagArray: can't fetch index ") << index <<               \
                    " (of " << value.size () << ") as (" << BagType::bagTypeType << ")");           \
            }

        GET_TYPE(Text, TEXT, Text);
        GET_TYPE(Integer, INTEGER, s8);
        GET_TYPE(Float, FLOAT, f8);
        GET_TYPE(Bool, BOOL, bool);

        #undef GET_TYPE
        #define GET_TYPE(bagType, baseType)                                                         \
            baseType getBag##bagType (uint index) const  {                                          \
                PtrToBagThing bagThing = get (index);                                               \
                if (bagThing) {                                                                     \
                    PtrToBag##bagType bag##bagType = ptr_downcast<Bag##bagType> (bagThing);         \
                    return bag##bagType;                                                            \
                }                                                                                   \
                throw RuntimeError (Text ("BagArray: can't fetch index ") << index <<               \
                    " (of " << value.size () << ")");                                               \
            }

        GET_TYPE(Array, PtrToBagArray);
        GET_TYPE(Object, PtrToBagObject);

        #undef GET_TYPE

        static PtrToBagArray sort (const PtrToBagArray& source, SortDir sortDir = ASCENDING) {
            //Log::debug() << "BagArray: " << "simple sort" << endl;
            return withSortAdapter (source, SimpleSort (sortDir));
        }

        static PtrToBagArray sort (const PtrToBagArray& source, const Text& field, SortDir sortDir = ASCENDING) {
            //Log::debug() << "BagArray: " << "field sort" << endl;
            return withSortAdapter (source, FieldSort (field, sortDir));
        }

        static PtrToBagArray sort (const PtrToBagArray& source, const DbSort& dbSort) {
            //Log::debug() << "BagArray: " << "db sort" << endl;
            return withSortAdapter (source, dbSort);
        }
};
