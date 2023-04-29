package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.bag.entry.Handler;
import us.irdev.bedrock.bag.entry.HandlerArrayFromDelimited;
import us.irdev.bedrock.bag.entry.HandlerObjectFromPairsArray;

public class FormatReaderComposite extends FormatReader implements ArrayFormatReader, ObjectFormatReader {
    private Handler handler;

    public FormatReaderComposite () {}

    public FormatReaderComposite (String input, Handler handler) {
        super (input);
        this.handler = handler;
    }

    @Override
    public BagArray readBagArray () {
        return (BagArray) handler.getEntry (input);
    }

    @Override
    public BagObject readBagObject () {
        return (BagObject) handler.getEntry (input);
    }

    /*
    public static FormatReaderComposite basicArrayReader (String input, String arrayDelimiter, String ignore) {
        return new FormatReaderComposite (input, new HandlerArrayFromDelimited (arrayDelimiter).ignore (ignore));
    }
    */

    public static FormatReaderComposite basicArrayReader (String input, String arrayDelimiter) {
        return new FormatReaderComposite (input, new HandlerArrayFromDelimited (arrayDelimiter));
    }

    public static FormatReaderComposite basicObjectReader (String input, String arrayDelimiter, String pairDelimiter, boolean accumulateEntries) {
        return new FormatReaderComposite (input, new HandlerObjectFromPairsArray (
                new HandlerArrayFromDelimited (arrayDelimiter, new HandlerArrayFromDelimited (pairDelimiter))
        ).accumulateEntries (accumulateEntries));
    }

    public static FormatReaderComposite basicObjectReader (String input, String arrayDelimiter, String pairDelimiter) {
        return basicObjectReader (input, arrayDelimiter, pairDelimiter, false);
    }

    static {
        MimeType.addExtensionMapping (MimeType.PROP, "properties");
        registerFormatReader (MimeType.PROP, false, (input) -> basicObjectReader (input, "\n", "="));

        MimeType.addExtensionMapping (MimeType.URL, "url");
        registerFormatReader (MimeType.URL, false, (input) -> basicObjectReader (input, "&", "="));
    }
}
