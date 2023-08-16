package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;

import static us.irdev.bedrock.bag.formats.Utility.sortString;

// reads a delimited table format, like CSV, or tab delimited... the result is an array of bag-objects
// representing the named columns. we either require a first line that contains the columns or a
// supplied array of column names. comment lines are allowed outside of an entry.
public class FormatReaderDelimited extends FormatReaderParsed implements ArrayFormatReader {
  private final char delimiter;
  private final char[] bareValueStopChars;
  private final char comment;

  private static final char[] COMMENT_STOP_CHARS = sortString ("\n");
  private static final char[] QUOTED_STRING_STOP_CHARS = sortString ("\"");

  // default public constructor so the static initializer will run
  public FormatReaderDelimited () {
    delimiter = comment = ' ';
    bareValueStopChars = null;
  }

  private FormatReaderDelimited (String input, char delimiter, char comment) {
    super (input, false);
    this.delimiter = delimiter;
    this.bareValueStopChars = sortString("\n" + delimiter);
    this.comment = comment;
  }

  public static FormatReaderDelimited formatReaderCSV (String input) {
    return new FormatReaderDelimited(input, ',', '#');
  }

  public static FormatReaderDelimited formatReaderTabDelimited (String input) {
    return new FormatReaderDelimited(input, '\t', '#');
  }

  private String readEntry() {
    String entry = readString(QUOTED_STRING_STOP_CHARS);
    return (entry != null) ? entry : readBareValueUntil (bareValueStopChars);
  }


  private BagArray readLine() {
    // comment lines get eaten and discarded
    while (expect(comment)) {
      consumeUntil(COMMENT_STOP_CHARS, true);
      expect (NEW_LINE);
    }

    // marker is at the beginning of a line
    if (check ()) {
      var line = new BagArray();
      do {
        line.add (readEntry ());
      } while (expect(delimiter));
      expect (NEW_LINE);
      return line;
    }
    return null;
  }

  @Override
  public BagArray readBagArray () {
    var bagArray = new BagArray ();
    BagArray line;
    while ((line = readLine()) != null) {
      bagArray.add (line);
    }
    return bagArray;
  }

  static {
    MimeType.addExtensionMapping (MimeType.CSV, "csv");
    MimeType.addMimeTypeMapping (MimeType.CSV);
    FormatReader.registerFormatReader (MimeType.CSV, false, (input) -> {return new FormatReaderDelimited(input, ',', '#');});
  }
}
