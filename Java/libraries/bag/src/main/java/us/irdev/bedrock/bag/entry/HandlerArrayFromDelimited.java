package us.irdev.bedrock.bag.entry;

import us.irdev.bedrock.bag.BagArray;

public class HandlerArrayFromDelimited extends HandlerComposite {
    private final String delimiter;

    public HandlerArrayFromDelimited (String delimiter) {
        this (delimiter, HandlerValue.HANDLER_VALUE);
    }

    public HandlerArrayFromDelimited (String delimiter, Handler handler) {
        super(handler);
        this.delimiter = delimiter;
    }

    @Override
    public Object getEntry (String input) {
        var inputEntries = input.split (delimiter);
        final var bagArray = new BagArray (inputEntries.length);
        for (String inputEntry : inputEntries) {
            var entry = handler.getEntry (inputEntry);
            if (entry != null) {
                bagArray.add (entry);
            }
        }
        return (bagArray.getCount () > 0) ? bagArray : null;
    }
}
