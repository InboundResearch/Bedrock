#pragma once

#include "Log.h"

template<typename ValueType>
class Statistics {
    public:
        enum Outliers {
            ELIMINATE_OUTLIERS,
            KEEP
        };

    private:
        vector<ValueType> array;
        Text name;
        ValueType min;
        ValueType mean;
        ValueType median;
        ValueType max;
        ValueType variance;

        // compute the median on a sorted array
        static ValueType computeMedian (const ValueType* array, uint count) {
            int midpoint = count >> 1;
            return ((count & 0x01) == 0) ? ((array[midpoint - 1] + array[midpoint]) * 0.5) : array[midpoint];
        }

        void init (const Text& _name, const ValueType* _array, uint count, Outliers outliers) {
            name = _name;
            min = numeric_limits<ValueType>::max ();
            max = numeric_limits<ValueType>::lowest ();
            mean = median = variance = 0;

            // none of this is meaningful unless there are samples
            if (count > 0) {
                // copy, and sort the array to make medians easy
                array.assign(_array, _array + count);
                sort (array.begin (), array.end ());

                // if we want to eliminate outliers, and there are enough samples to perform IQR...
                if ((outliers == ELIMINATE_OUTLIERS) and (count > 5)) {
                    // using the Inter-Quartile Range (IQR) method for eliminating outliers
                    // https://en.wikipedia.org/wiki/Interquartile_range
                    int halfLength = count >> 1;
                    ValueType q1 = computeMedian (array.data (), halfLength);
                    ValueType q3 = computeMedian (array.data () + halfLength + (count & 0x01), halfLength);

                    // compute the IQR fences and get the result (1.5 is a "magic" number
                    // associated with the method, because John Tukey chose this number when he
                    // invented the box and whisker plot method, and it has worked well enough ever
                    // since)
                    ValueType iqr = (q3 - q1) * 1.5;
                    ValueType fenceLow = q1 - iqr;
                    ValueType fenceHigh = q3 + iqr;

                    vector<ValueType> filtered;
                    ValueType lastValue = array.back();
                    Log& log = Log::debug () << "IQR (q1: " << q1 << ", q3: " << q3 << ", iqr: " << iqr << ", min: " << fenceLow << ", max: " << fenceHigh << ")";
                    for (typename vector<ValueType>::iterator it = array.begin (); it != array.end (); ++it) {
                        ValueType value = *it;
                        if (value != lastValue) {
                            log << endl << value << " ";
                            lastValue = value;
                        }

                        if ((value >= fenceLow) and (value <= fenceHigh)) {
                            filtered.push_back(value);
                            log << "+";
                        } else {
                            log << "-";
                        }
                    }
                    log << endl;

                    array = filtered;
                    count = array.size ();
                }

                // compute the median
                median = computeMedian (array.data (), count);

                // compute the mean, capture min and max along the way
                for (typename vector<ValueType>::iterator it = array.begin (); it != array.end (); ++it) {
                    ValueType value = *it;
                    min = std::min (value, min);
                    max = std::max (value, max);
                    mean += value;
                }
                mean /= ValueType (count);

                // compute the variance
                for (typename vector<ValueType>::iterator it = array.begin (); it != array.end (); ++it) {
                    ValueType value = *it;
                    ValueType delta = value - mean;
                    variance += delta * delta;
                }
                variance /= ValueType (count);
            }
        }

    public:
        Statistics (const Text& _name, const ValueType* _array, uint _count, Outliers outliers = KEEP) {
            init (_name, _array, _count, outliers);
        }

        Statistics (const Text& _name, const vector<ValueType>& _array, Outliers outliers = KEEP) {
            init (_name, _array.data (), _array.size (), outliers);
        }

        int getCount () const { return array.size (); }
        ValueType getMin () const { return min; }
        ValueType getMean () const { return mean; }
        ValueType getMedian() const { return median; }
        ValueType getMax () const { return max; }
        ValueType getVariance () const { return variance; }
        ValueType getStdDev () const { return sqrt (variance); }

        const Statistics& report () const {
            cerr << "STATS (" << name << " - ";
            uint count = array.size ();
            if (count > 0) {
                cerr <<
                            "count: " << count << ", " <<
                            "mean: " << mean << ", " <<
                            "range: [" << min << " - " << max << "], " <<
                            "median: " << median << ", " <<
                            "variance: " << variance << ", " <<
                            "stddev: " << getStdDev ();
            } else {
                cerr << "count: 0";
            }
            cerr << ")" << endl;
            return *this;
        }
};
