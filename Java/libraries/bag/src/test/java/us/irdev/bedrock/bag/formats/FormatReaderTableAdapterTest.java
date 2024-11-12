package us.irdev.bedrock.bag.formats;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.bag.BagArrayFrom;
import us.irdev.bedrock.bag.BagTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatReaderTableAdapterTest {
  @Test
  public void testFormatReaderDelimited () {
    var bagArray = BagArrayFrom.file (new File ("data", "test.csv"));

    BagTest.report(bagArray.getCount(), 8, "verify the reader got the right number of data rows (accounting for comments)");

    // make sure a particular element is the correct string
    var row7 = bagArray.getBagObject(6);
    var entry3 = row7.getString("mag lon (deg)");
    BagTest.report (entry3, "16.967403411865234\\\"xxx\\\"", "verify we correctly read and placed the row elements in the object");

    // make sure an empty comma row has no entries
    var row8 = bagArray.getBagObject(7);
    BagTest.report(row8.getCount(), 0, "verify an empty row in the file has no entries");
  }
}
