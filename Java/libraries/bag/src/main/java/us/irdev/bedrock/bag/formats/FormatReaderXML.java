package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;

// reads an XML-ish formatted string, without some of the arbitrary constraints, and without some of
// the esoteric capabilities (namespaces, etc.). The intent is to support basic data in XML format
// and things like Maven POM files. See the description of XML at https://www.w3.org/TR/xml11/
//
// NOTES:
// 1) we support data in XML format, not XML documents. The top level is not constrained to a single
//    element, for instance.
// 2) we read an XML data block as an array of tags, which are read as BagObjects.
// 3) tags have a tag name, body (whitespace preserved), and children. we save these in the
//    BagObject as _tag, _body, and _children, in addition to whatever attributes are defined as
//    key-value pairs within the tag declaration.
// 4) Error checking is limited
public class FormatReaderXML extends FormatReaderParsed implements ArrayFormatReader {
  public FormatReaderXML () {}

  private FormatReaderXML (String input) {
    super (input);
  }

  private BagObject readAttributes(BagObject tag) {
    return tag;
  }

  private static final char[] ELEMENT_OPEN_TAG_STOP_CHARS = sortString (" />");
  private BagObject readElement () {
    if (expect ('<')) {
      // tag name - consume until whitespace or tag stop chars [/>\s]
      String value = readBareValueUntil ();
      var tag = readAttributes (BagObject.open ("_tag", value));
      // attributes - consume whitespace
      // close -> body -> tagStop
      // end [/>]
    }
    return null;
  }

  public BagArray readBagArray () {
    BagArray array = null;
    BagObject element;
    while ((element = readElement ()) != null) {
      if (array == null) {
        array = BagArray.open (element);
      } else {
        array.add (element);
      }
    }
    return array;
  }
}
