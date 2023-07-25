#pragma once

#include "BagObject.h"

// sort helpers
enum SortDir { DESCENDING, ASCENDING };

class SimpleSort {
    private:
        SortDir sortDir;

    public:
        SimpleSort (SortDir _sortDir) : sortDir (_sortDir) {}

        double compare (const PtrToBagThing& leftThing,const PtrToBagThing& rightThing) const {
            // strategy:
            // start by trying to do a numeric comparison, if that's not possible, convert
            // both sides to strings and do a string comparison
            double leftThingSortValue = leftThing->sortValue();
            double delta = leftThingSortValue - rightThing->sortValue();
            if ((delta == 0) and (leftThingSortValue == 0)) {
                //Log::debug() << "SimpleSort: " << "comparing as text ([" << leftThing->toText() << "], [" <<  rightThing->toText () << "])" << endl;
                delta = static_cast<double> (leftThing->toJson().compare(rightThing->toJson ()));
            }
            return (sortDir == ASCENDING) ? delta : -delta;
        }
};

class FieldSort {
    private:
        Text field;
        SimpleSort simpleSort;

    public:
        FieldSort (const Text& _field, SortDir sortDir) : field (_field), simpleSort (sortDir) {}

        double compare (const PtrToBagObject& leftObject, const PtrToBagObject& rightObject) const {
            return simpleSort.compare (leftObject->get(field), rightObject->get(field));
        }

        double compare (const PtrToBagThing& leftThing,const PtrToBagThing& rightThing) const {
            return compare (ptr_downcast<BagObject> (leftThing), ptr_downcast<BagObject> (rightThing));
        }
};

class DbSort {
    private:
        vector<FieldSort> fields;

    public:
        DbSort (const Text& field, SortDir sortDir = ASCENDING) {
            fields.push_back (FieldSort (field, sortDir));
        }

        DbSort& operator () (const Text& field, SortDir sortDir = ASCENDING) {
            fields.push_back (FieldSort (field, sortDir));
            return *this;
        }

        double compare (const PtrToBagObject& leftObject, const PtrToBagObject& rightObject) const {
            for (vector<FieldSort>::const_iterator i = fields.begin (); i != fields.end (); ++i) {
                double result = i->compare (leftObject, rightObject);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }

        double compare (const PtrToBagThing& leftThing,const PtrToBagThing& rightThing) const {
            return compare (ptr_downcast<BagObject> (leftThing), ptr_downcast<BagObject> (rightThing));
        }
};

template<typename SortType>
struct SortAdapter {
    const SortType& sortType;

    SortAdapter (const SortType& _sortType) : sortType (_sortType) {}

    // should left be ordered before right?
    bool operator () (const PtrToBagThing& leftThing,const PtrToBagThing& rightThing) {
        return sortType.compare (leftThing, rightThing) < 0;
    }
};
