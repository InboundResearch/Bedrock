package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.logger.*;
import java.util.Arrays;

import static us.irdev.bedrock.bag.formats.Utility.sortString;

public class FormatReaderParsed extends FormatReader {
    private static final Logger log = LogManager.getLogger (FormatReader.class);

    protected int index;
    protected int inputLength;
    protected int lineNumber;
    protected int lastLineIndex;
    protected boolean error;

    protected static final String WHITESPACE_CHARS = " \u00a0\t";
    protected static final char NEW_LINE = '\n';
    protected final char[] whitespaceChars;


    protected FormatReaderParsed () {
        whitespaceChars = sortString (WHITESPACE_CHARS);
    }

    public FormatReaderParsed (String input, boolean newLineIsWhitespace) {
        super (input);
        inputLength = (input != null) ? input.length () : 0;
        index = 0;
        lineNumber = 1;
        lastLineIndex = 0;
        this.whitespaceChars = sortString(newLineIsWhitespace ? WHITESPACE_CHARS + NEW_LINE : WHITESPACE_CHARS);
    }

    public FormatReaderParsed (String input) {
        this (input, true);
    }

    protected boolean notIn (char[] inChars, char c) {
        return Arrays.binarySearch(inChars, c) < 0;
    }

    protected boolean in (char[] inChars, char c) {
        return Arrays.binarySearch(inChars, c) >= 0;
    }

    /**
     *
     * @return
     */
    protected boolean check () {
        return (! error) && (index < inputLength);
    }

    protected boolean inspectForNewLine(char c) {
        if (c == NEW_LINE) {
            ++lineNumber;
            lastLineIndex = index;
            return true;
        }
        return false;
    }

    protected int consumeWhile (char[] inChars, boolean allowEscape) {
        var start = index;
        char c;
        while (check () && in (inChars, c = input.charAt (index))) {
            inspectForNewLine(c);

            // using the escape mechanism is like a free pass for the next character, but we don't
            // do any transformation on the substring, just return it as written after checking for
            // newlines
            if ((c == '\\') && allowEscape) {
                ++index;
                inspectForNewLine(input.charAt (index));
            }

            // consume the character
            ++index;
        }
        return start;
    }

    protected void consumeWhitespace () {
        consumeWhile (whitespaceChars, false);
    }

    protected int consumeUntil (char[] stopChars, boolean allowEscape) {
        var start = index;
        char c;
        while (check () && notIn (stopChars, c = input.charAt (index))) {
            inspectForNewLine(c);

            // using the escape mechanism is like a free pass for the next character, but we don't
            // do any transformation on the substring, just return it as written after checking for
            // newlines
            if ((c == '\\') && allowEscape) {
                ++index;
                inspectForNewLine(input.charAt (index));
            }

            // consume the character
            ++index;
        }
        return start;
    }

    /**
     *
     * @param c
     * @return
     */
    protected boolean expect(char c) {
        consumeWhitespace();

        // the next character should be the one we expect
        if (check() && (input.charAt (index) == c)) {
            inspectForNewLine(c);
            ++index;
            return true;
        }
        return false;
    }

    protected boolean expect(char[] chars) {
        consumeWhitespace();

        // the next character should be the one we expect
        char c;
        if (check() && in(chars, (c = input.charAt (index)))) {
            inspectForNewLine(c);
            ++index;
            return true;
        }
        return false;
    }

    protected boolean expect(String string) {
        consumeWhitespace();

        // the substring from here should be the one we expect
        // XXX TODO - make sure there are enough characters left in the stream to fulfill the substring request
        // XXX - think about whether case insensitivity should be allowed
        var stringLength = string.length();
        if (check() && input.substring (index, index + stringLength).equals(string)) {
            index += stringLength;
            return true;
        }
        return false;
    }

    /**
     *
     * @param c
     * @return
     */
    protected boolean require(char c) {
        return require (expect (c), "'" + c + "'");
    }

    protected boolean require(char[] chars) {
        return require (expect (chars), "[" + chars.toString() + "]");
    }

    protected boolean require(String string) {
        return require (expect (string), "\"" + string + "\"");
    }

    /**
     *
     * @param condition
     * @param explanation
     * @return
     */
    protected boolean require (boolean condition, String explanation) {
        if (! condition) {
            onReadError (explanation + " REQUIRED");
        }
        return condition;
    }

    /**
     *
     * @param errorMessage
     */
    protected void onReadError (String errorMessage) {

        // log the messages, we only need to output the line if this is the first time the error is
        // being reported
        if (! error) {
            // say where the error is
            log.error ("Error while parsing input on line " + lineNumber + ", near: ");
            // find the end of the current line. note: line endings could only be '\n' because the
            // input reader consumed the actual line endings for us and replaced them with '\n'
            var lineEnd = index;
            while ((lineEnd < inputLength) && (input.charAt (lineEnd) != NEW_LINE)) {
                ++lineEnd;
            }
            log.error (input.substring (lastLineIndex, lineEnd));

            // build the error message, by computing a carat line, and adding the error message to it
            var errorIndex = index - lastLineIndex;
            var caratChars = new char[errorIndex + 2];
            Arrays.fill (caratChars, ' ');
            caratChars[errorIndex] = '^';
            var carat = new String (caratChars) + errorMessage;

            log.error (carat);

            // set the error state
            error = true;
        }
    }

    protected String readString (char[] stopChars) {
        // " chars " | <chars>
        var result = (String) null;
        if (expect('"')) {
            // digest the string, and be sure to eat the end quote
            var start = consumeUntil (stopChars, true);
            result = input.substring (start, index++);
        }
        return result;
    }

    protected String readBareValueUntil (char[] stopChars) {
        // " chars " | <chars>
        var result = (String) null;
        var start = consumeUntil (stopChars, true);

        // capture the result if we actually consumed some characters
        if (index > start) {
            result = input.substring (start, index);
        }

        return result;
    }

    protected String readBareValueWhile (char[] inChars) {
        // " chars " | <chars>
        var result = (String) null;
        var start = consumeWhile (inChars, false);

        // capture the result if we actually consumed some characters
        if (index > start) {
            result = input.substring (start, index);
        }

        return result;
    }
}
