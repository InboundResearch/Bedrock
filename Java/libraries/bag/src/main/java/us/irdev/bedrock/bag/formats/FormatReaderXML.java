package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.bag.scanner.XmlScanner;

import static us.irdev.bedrock.bag.formats.Utility.sortString;

// reads an XML-ish formatted string, without some of the arbitrary constraints, and without some of
// the esoteric capabilities (namespaces, etc.). The intent is to support basic data in XML format
// and things like HTML from web pages or Maven POM files. See the description of XML at
// https://www.w3.org/TR/xml11/
//
// NOTES:
// 1) we support data in XML-ish format, not strict XML documents. For instance, the top level is
//    not constrained to a single element
// 2) we read an XML data block as an array of elements, which are in-turn read as BagObjects.
// 3) elements have an element name, body (array of text entries with whitespace preserved), and
//    children. we save these in the BagObject as _element, _body, and _children, in addition to
//    whatever attributes are defined as key-value pairs within the element open tag
// 4) error checking is limited
public class FormatReaderXML extends FormatReader implements ArrayFormatReader {
  public FormatReaderXML () {}

  private FormatReaderXML (String input) {
    super (input);
  }

  public BagArray readBagArray () {
    BagArray array = null;
    return array;
  }
}
