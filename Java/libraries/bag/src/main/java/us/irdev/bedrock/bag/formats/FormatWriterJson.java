package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;

public class FormatWriterJson extends FormatWriter {
    static final String[] CURLY_BRACKETS = { "{", "}" };
    static final String[] SQUARE_BRACKETS = { "[", "]" };

    public FormatWriterJson () { super (); }

    private String getJsonString (Object object) {
        if (object != null) {
            switch (object.getClass ().getCanonicalName ()) {
                case "java.lang.String": return quote ((String) object);
                //case "BagObject":
                case "us.irdev.bedrock.bag.BagObject":
                    return write ((BagObject) object);
                //case "BagArray":
                case "us.irdev.bedrock.bag.BagArray":
                    return write ((BagArray) object);

                // we omit the default case, because there should not be any other types stored in
                // the Bag classes - as in, they would not make it into the container, as the
                // "objectify" method will gate that
            }
        }
        // if we stored a null, we need to emitToken it as a value. This will only happen in the
        // array types, and is handled on the parsing side with a special case for reading
        // the bare value 'null' (not quoted)
        return "null";
    }

    @Override
    public String write (BagObject bagObject) {
        var stringBuilder = new StringBuilder ();
        var separator = "";
        var keys= bagObject.keys();
        for (var key : keys) {
            stringBuilder
                    .append (separator)
                    .append (quote (key))
                    .append (":")
                    .append (getJsonString (bagObject.getObject (key)));
            separator = ",";
        }
        return enclose(stringBuilder.toString(), CURLY_BRACKETS);
    }

    @Override
    public String write (BagArray bagArray) {
        var stringBuilder = new StringBuilder ();
        var separator = "";
        for (int i = 0, end = bagArray.getCount(); i < end; ++i) {
            stringBuilder
                    .append(separator)
                    .append(getJsonString(bagArray.getObject(i)));
            separator = ",";
        }
        return enclose(stringBuilder.toString(), SQUARE_BRACKETS);
    }

    static {
        FormatWriter.registerFormatWriter (MimeType.DEFAULT, false, FormatWriterJson::new);
    }
}
