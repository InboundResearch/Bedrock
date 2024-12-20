package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;


/**
 * The FormatWriterText is a configurable text format writer for any format that uses a divider
 * between entries, and a divider between pairs.
 */
public class FormatWriterText extends FormatWriter {
    private static final Logger log = LogManager.getLogger (FormatWriterText.class);

    String entrySeparator;
    String pairSeparator;

    public FormatWriterText () { super (); }

    public FormatWriterText (String entrySeparator, String pairSeparator) {
        super ();
        this.entrySeparator = entrySeparator;
        this.pairSeparator = pairSeparator;
    }

    @Override
    public String write (BagArray bagArray) {
        var stringBuilder = new StringBuilder ();
        for (int i = 0, end = bagArray.getCount (); i < end; ++i) {
            stringBuilder.append (bagArray.getString (i)).append (entrySeparator);
        }
        return stringBuilder.toString ();
    }

    @Override
    public String write (BagObject bagObject) {
        var stringBuilder = new StringBuilder ();
        var keys = bagObject.keys ();
        for (var key : keys) {
            // the reader has a flag to accumulate values or overwrite them. accumulated values will be gathered into
            // an array - as the writer, we will assume the presence of the array means multiple lines
            var bagArray = bagObject.getBagArray (key);
            if (bagArray != null) {
                for (int i = 0, end = bagArray.getCount (); i < end; ++i) {
                    var value = bagArray.getString (i);
                    if (value != null) {
                        stringBuilder.append(key).append(pairSeparator).append(value).append(entrySeparator);
                    }
                }
            } else {
                var  value = bagObject.getString (key);
                if (value != null) {
                    stringBuilder.append(key).append(pairSeparator).append(value).append(entrySeparator);
                }
            }
        }
        return stringBuilder.toString ();
    }

    static {
        registerFormatWriter (MimeType.PROP, false, () -> new FormatWriterText ("\n", "="));
        registerFormatWriter (MimeType.URL, false, () -> new FormatWriterText ("&", "="));
    }
}
