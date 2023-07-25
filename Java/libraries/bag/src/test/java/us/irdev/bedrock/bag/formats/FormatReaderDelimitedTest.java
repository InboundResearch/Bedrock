package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagArrayFrom;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatReaderDelimitedTest {
  @Test
  public void testFormatReaderDelimited () {
    var bagArray = BagArrayFrom.file (new File ("data", "test.csv"));

    // make sure the test has the right number of lines (accounting for comments, etc.)
    assertEquals (9, bagArray.getCount ());

    // make sure a particular element is the correct string
    var row7 = bagArray.getBagArray(7);
    var entry3 = row7.getString(3);
    assertEquals ("16.967403411865234\\\"xxx\\\"", entry3);

    // make sure an empty comma row has 4 entries
    var row8 = bagArray.getBagArray(8);
    assertEquals (4, row8.getCount());
  }
}
