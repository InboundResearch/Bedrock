package us.irdev.bedrock.bag.entry;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;

public class HandlerObjectFromPairsArray extends Handler {
    private final Handler arrayHandler;
    protected boolean accumulateEntries;

    public HandlerObjectFromPairsArray (Handler arrayHandler) {
        super ();
        this.arrayHandler = arrayHandler;
        accumulateEntries = false;
    }

    public HandlerObjectFromPairsArray accumulateEntries (boolean accumulateEntries) {
        this.accumulateEntries = accumulateEntries;
        return this;
    }

    @Override
    public Object getEntry (String input) {
        // read the bedrock array of the input, and check for success
        var bagArray = (BagArray) arrayHandler.getEntry (input);
        if (bagArray != null) {
            // create a bedrock object from the array of pairs
            var bagObject = new BagObject (bagArray.getCount ());
            bagArray.forEach (object -> {
                var pair = (BagArray) object;
                    if (accumulateEntries) {
                        bagObject.add (pair.getString (0), pair.getString (1));
                    } else {
                        bagObject.put (pair.getString (0), pair.getString (1));
                    }
            });

            // return the result
            return bagObject;
        }
        return null;
    }
}
