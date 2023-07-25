package us.irdev.bedrock.bag.entry;

import us.irdev.bedrock.bag.BagArray;

/**
 * supporting a parsed format that uses an entry delimiter, with entries being quoted
 */
public class HandlerArrayFromParsed extends HandlerComposite {
    private final char delimiter;
    private final char quote;
    private final char escape;

    protected int index;
    protected int inputLength;
    protected int lineNumber;
    protected int lastLineIndex;
    protected boolean error;


    public HandlerArrayFromParsed (char delimiter, char quote, char escape, Handler handler) {
        super (handler);
        this.delimiter = delimiter;
        this.quote = quote;
        this.escape = escape;
    }

    public HandlerArrayFromParsed (char delimiter, Handler handler) {
        this (delimiter, '\"', '\\', handler);
    }

    private String readOneEntry(String input, int offset) {
        // consume whitespace
        return null;
    }

    @Override
    public Object getEntry (String input) {
        // a basic array parser that allows lines of delimited text with quoted sections and escaped
        // quotes within the quoted sections
        // example: 1,2,3,"4,5","6\"hello\""

        // TODO ? is this necessary when we have the


        return null;
    }
}
