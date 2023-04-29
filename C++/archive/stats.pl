#! /usr/bin/env perl

use strict;
use warnings;

use List::Util qw[min max];

# function to compute median of a set of data, assumes input is already sorted
sub median {
    # $array is an array reference
    my ($array) = @_;
    my $arraySize = scalar(@$array);
    my $midPoint = int ($arraySize / 2);
    return ($arraySize % 2 == 0) ? (($array->[$midPoint - 1] + $array->[$midPoint]) / 2.0) : $array->[$midPoint];
}

# functions to extract values from an array
sub getRange {
    # $array is an array reference
    my ($array, $low, $high) = @_;
    my $range = [];
    my $lastEntry = "xxx";
    print STDERR "    getRange ($low, $high)";
    for my $entry (@$array) {
        if ($entry ne $lastEntry) {
            $lastEntry = $entry;
            print STDERR "\n        Entry: " . sprintf ("%+0.06f", $entry) . " ";
        }
        if (($entry >= $low) && ($entry <= $high)) {
            push (@$range, $entry);
            print STDERR "+";
        } else {
            print STDERR "-";
        }
    }
    print STDERR "\n";
    return $range;
}

# function to get a sub-portion of an array
sub getSubArray {
    my ($array, $startIndex, $count) = @_;
    my $subArray = [];
    for (my $i = 0; $i < $count; ++$i) {
        push (@$subArray, $array->[$startIndex + $i]);
    }
    return $subArray;
}

sub makeStats {
    my ($min, $max, $mean) = @_;
    return { min => $min, max => $max, mean => $mean };
}

sub simpleStats {
    my ($mean) = @_;
    return makeStats ($mean, $mean, $mean);
}

# function to compute the mean of a range after eliminating outliers with the 
# Inter-Quartile Range (IQR) method
sub computeMean {
    my ($arrayIn) = @_;
    
    # sort the array
    my @array = sort {$a <=> $b} @$arrayIn;
    my $fenced;
    
    # check if the array is big enough to actually have quartiles...
    my $count = scalar (@array);
    if ($count > 5) {
        print STDERR "    computeMean on $count entries using IQR method\n";
        print STDERR "        check for outliers\n";
        
        # compute the quartiles
        my $halfLength = int ($count / 2.0);
        my $lowHalf = getSubArray (\@array, 0, $halfLength);
        my $highHalf = getSubArray (\@array, $halfLength + ($count % 2), $halfLength);
        my $q2 = median (\@array);
        my $q1 = median ($lowHalf);    
        my $q3 = median ($highHalf);
        print STDERR "        Q1: $q1, Q2: $q2, Q3: $q3\n";
        
        # compute the IQR fences and get the result
        my $iqr = ($q3 - $q1) * 1.5;
        if ($iqr < 0) {
            print STDERR "IQR < 0, SOMETHING IS WRONG\n";
        }
        my $fenceLow = $q1 - $iqr;
        my $fenceHigh = $q3 + $iqr;
        print STDERR "        IQR: $iqr, low: $fenceLow, high: $fenceHigh\n";
        $fenced = getRange (\@array, $fenceLow, $fenceHigh);
    } else {
        # not enough samples to bother eliminating outliers. just use what we've 
        # got to compute a mean
        $fenced = \@array;
    }
    
    # now compute the mean of the fenced region
    my $minEntry = $fenced->[0];
    my $maxEntry = $minEntry;
    my $sum = 0;
    for my $entry (@$fenced) {
        $sum += $entry;
        if ($entry > $maxEntry) {
            $maxEntry = $entry;
        }
        if ($entry < $minEntry) {
            $minEntry = $entry;
        }
    }
    $count = scalar (@$fenced);
    my $mean = $sum / scalar (@$fenced);
    print STDERR "    $count entries (min: $minEntry, max: $maxEntry, mean: $mean)\n";
    return makeStats ($minEntry,$maxEntry,$mean);
}

sub histogramWithCount {
    my ($arrayIn, $bucketCount) = @_;
    my $stats = computeMean ($arrayIn);
        
    # lets split the range over a fixed number of slices, padded by 2% of the
    # actual range
    my $range = $stats->{max} - $stats->{min};    
    my $padding = $range * 0.02;
    my $min = $stats->{min} - $padding;
    $range += (2 * $padding);
    if ($range > 0) {
        print STDERR "\n    HISTOGRAM:\n";
        my @buckets;
        for (my $i = 0; $i < $bucketCount; $i++) {
            push (@buckets, 0);
        }
        # = ( 0, 0, 0, 0, 0, 0, 0, 0, 0 );
        #my $bucketCount = scalar (@buckets);
        my $bucketSize = $range / $bucketCount;
        my $sampleCount = 0;
        for my $value (@$arrayIn) {
            if (($value >= $stats->{min}) && ($value <= $stats->{max})) {
                my $index = int (($value - $min) / $bucketSize);
                $buckets[$index]++;
                $sampleCount++;
            }
        }

        print STDERR "            min         max      %      0    1    2    3    4    5    6    7    8    9    X\n";
        print STDERR "           -----       -----   -----   +-----+----+----+----+----+----+----+----+----+----+\n";

        my $percentageString = "XXXXOXXXXOXXXXOXXXXOXXXXOXXXXOXXXXOXXXXOXXXXOXXXXO";
        for (my $i = 0; $i < $bucketCount; $i++) {
            my $bucketValue = $buckets[$i];
            my $low = $min + (($i / $bucketCount) * $range);
            my $high = $min + ((($i + 1) / $bucketCount) * $range);
            #my $rangeStr = sprintf ("%18s", sprintf ("%+0.03f -> %+0.03f", $low, $high));
            my $rangeStr = sprintf ("%20s", sprintf ("%8s -> %8s", sprintf ("%+0.03f", $low), sprintf ("%+0.03f", $high)));
            my $barPercentage = $bucketValue / $sampleCount;
            my $bar = int (($barPercentage * length ($percentageString)) + 0.5);
            my $barPercentageStr = sprintf ("%7s", sprintf ("%0.03f", $barPercentage * 100.0));
            print STDERR "        $rangeStr ($barPercentageStr) | " . substr ($percentageString, 0, $bar) . "\n";
        }
        print STDERR "\n";
    }
    return $stats;
}

sub histogram {
    my ($arrayIn) = @_;
    return histogramWithCount ($arrayIn, 7);
}

sub chart {
    my ($arrayIn) = @_;
    
    # the input array contains hashes with {value, minCorrectedValue, maxCorrectedValue, meanCorrectedValue}
    # sort numerically by value, convert the min, max, and mean to percentage error
    # plot them on an error chart similar to the histogram
    
    my @array = sort { $a->{value} <=> $b->{value} } @$arrayIn;

    # find the largest percentage we will deal with, and give it a little bit 
    # of padding for display purposes
    my $globalMax = -1;
    for my $entry (@array) {
        my $min = abs ($entry->{min} - $entry->{value}) / $entry->{value};
        my $mean = abs ($entry->{mean} - $entry->{value}) / $entry->{value};
        my $max = abs ($entry->{max} - $entry->{value}) / $entry->{value};
        $globalMax = max ($globalMax, $min, $mean, $max);
    }    
    #$globalMax = min ($globalMax * 1.1, 1);
    
    print STDERR "    CHART (scale, X = " . sprintf ("%0.02f", $globalMax * 100) . "%):\n";
    print STDERR "        value   mean%    0    1    2    3    4    5    6    7    8    9    X\n";
    print STDERR "        -----   -----   +-----+----+----+----+----+----+----+----+----+----+\n";
#                "       +0.398 (  0.398) | >"
    my $spacer =                           "                                                  ";    
    my $interior =                         "--------------------------------------------------";
    for my $entry (@array) {
        my $a = abs ($entry->{min} - $entry->{value}) / $entry->{value}; 
        my $mean = abs ($entry->{mean} - $entry->{value}) / $entry->{value};
        my $b = abs ($entry->{max} - $entry->{value}) / $entry->{value};

        # the entry contains values named 'min' and 'max', but these are true
        # names only within the domain of the variable - we want the min and max
        # errors associated with them...
        my $min = min ($a, $mean, $b);
        my $max = max ($a, $mean, $b);
        
        #print STDERR "(min: $min, mean: $mean, max: $max, globalMax: $globalMax)\n";
        print STDERR sprintf ("    %9s (%7s) | ", sprintf ("%+0.03f", $entry->{value}), sprintf ("%0.03f", $mean * 100));

        # if there are values to print...        
        if ($max > 0) {
            # compute the locations of the marks
            my $minMark = int ((($min / $globalMax) * length ($spacer)) + 0.5);
            my $meanMark = int ((($mean / $globalMax) * length ($spacer)) + 0.5);
            my $maxMark = int ((($max / $globalMax) * length ($spacer)) + 0.5);

            # put down spaces to pad out the min mark
            if ($minMark > 0) {         
                #print STDERR "[$minMark]";
                print STDERR substr ($spacer, 0, $minMark);
            }
            
            # there are several cases to consider...
            # a) all three marks are on the same spot
            # b) min and mean are on the same spot, max is later
            # c) min is by itself, mean and max are on the same spot
            # d) min, max, and mean are all on separate spots
            if ($maxMark - $minMark == 0) {
                # case a
                print STDERR "X";
            } else {
                if ($minMark == $meanMark) {
                    # case b
                    # do nothing
                } else {
                    # case c or d
                    print STDERR "<";
                    $minMark++;
                    if ($minMark < $meanMark) {
                        print STDERR substr ($interior, 0, $meanMark - $minMark);
                    }                    
                }

                # print the mean and move on
                print STDERR "X";
                $meanMark++;
                if ($meanMark < $maxMark) {
                    # case d
                    #print STDERR "[$maxMark-$meanMark]";
                    print STDERR substr ($interior, 0, $maxMark - $meanMark);
                }
                if ($meanMark <= $maxMark) {
                    print STDERR ">";
                }
            }
        }
        print STDERR "\n";
    }    
    
    # do you want RMS?
}

1;

