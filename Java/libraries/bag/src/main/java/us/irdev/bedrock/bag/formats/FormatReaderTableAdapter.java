package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;

public class FormatReaderTableAdapter extends FormatReader implements ArrayFormatReader {
  private final FormatReaderDelimited formatReader;
  private final String[] titlesSrc;

  public FormatReaderTableAdapter() {
    formatReader = null;
    titlesSrc = null;
  }

  public FormatReaderTableAdapter(String input, char delimiter, char comment, String[] titlesSrc) {
    super (input);
    formatReader = new FormatReaderDelimited(input, delimiter, comment);
    this.titlesSrc = titlesSrc;
  }

  public FormatReaderTableAdapter(String input, char delimiter, char comment) {
    this (input, delimiter, comment, null);
  }

  @Override
  public BagArray readBagArray () {
    var bagArray = formatReader.readBagArray();

    // gather the strings for the titles, if we don't already have them, the first data row in the
    // read file is it...
    final String[] titles;
    if (titlesSrc != null) {
      titles = titlesSrc;
    } else {
      titles = bagArray.getBagArray(0).toArray(String.class);
      bagArray = bagArray.subset(1);
    }

    // convert the array to a database format, only if the field has value
    var result = bagArray.map(array -> {
      var bagObject = new BagObject();
      for (int i = 0, end = titles.length; i < end; ++i) {
          bagObject.put (titles[i], ((BagArray) array).getString(i));
      }
      return bagObject;
    });

    // return the mapped result
    return result;
  }

  static {
    MimeType.addExtensionMapping (MimeType.CSV, "csv");
    MimeType.addMimeTypeMapping (MimeType.CSV);
    FormatReader.registerFormatReader (MimeType.CSV, false, (input) -> {return new FormatReaderTableAdapter(input, ',', '#');});
  }
}
