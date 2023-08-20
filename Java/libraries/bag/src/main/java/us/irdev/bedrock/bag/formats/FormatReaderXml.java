package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.bag.scanner.Token;
import us.irdev.bedrock.bag.scanner.XmlScanner;
import us.irdev.bedrock.bag.scanner.XmlToken;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class FormatReaderXml extends FormatReader implements ArrayFormatReader {
  private XmlScanner scanner;
  private Token<XmlToken> currentToken;

  private static final String _ELEMENT = "_element";
  private static final String _BODY = "_body";
  private static final String _CHILDREN = "_children";

  private static final Set<XmlToken> IGNORE = Stream.of(
          XmlToken.COMMENT, XmlToken.BODY, XmlToken.DECL, XmlToken.PROLOG
  ).collect(Collectors.toUnmodifiableSet());

  public FormatReaderXml() {}

  private FormatReaderXml(String input) {
    super (input);
    scanner = new XmlScanner();
  }

  private boolean readToken(XmlToken ignore) {
    while (true) {
      currentToken = scanner.scanToken();
      if ((currentToken = scanner.scanToken()) != null) {
        if (currentToken.emitToken() != ignore) {
          return true;
        }
      } else {
        return false;
      }
    }
  }

  private boolean readToken(Set<XmlToken> ignore) {
    while (true) {
      currentToken = scanner.scanToken();
      if ((currentToken = scanner.scanToken()) != null) {
        if (! ignore.contains(currentToken.emitToken())) {
          return true;
        }
      } else {
        return false;
      }
    }
  }

  private boolean check(XmlToken token) {
    return (currentToken != null) && (currentToken.emitToken() == token);
  }

  private boolean expect(XmlToken token) {
    if (check (token)) {
      readToken();
      return true;
    } else {
      return false;
    }
  }

  private boolean readAndExpect(XmlToken token) {
    if (readToken () && (currentToken.emitToken() == token)) {
      readToken();
      return true;
    }
    return false;
  }

  private BagObject readBody(BagObject element) {
    while (readToken() != null) {
      switch (currentToken.emitToken()) {
        case BODY -> { element.add(_BODY, currentToken.value()); }
        case BEGIN_OPEN_ELEMENT -> { element.add(_CHILDREN, readElement ()); }
        case BEGIN_CLOSE_ELEMENT -> {
          if ((readToken() != null) && (currentToken.emitToken() == XmlToken.CLOSE_ELEMENT_NAME) &&
                  (element.getString(_ELEMENT).equals(currentToken.value())) &&
                  readAndExpect(XmlToken.END_CLOSE_ELEMENT)) {
            return element;
          } else {
            // XXX continue until we get the proper close element?
          }
        }
      }
    }
    return element;
  }

  private void readAttribute(BagObject element) {
    if (check(XmlToken.ATTRIBUTE_NAME)) {
      var attributeName = currentToken.value();
      if (readAndExpect(XmlToken.ATTRIBUTE_EQ) && expect(XmlToken.OPEN_QUOTE) && check(XmlToken.ATTRIBUTE_VALUE)) {
        element.put(attributeName, currentToken.value());
        readAndExpect(XmlToken.CLOSE_QUOTE);
      }
    }
  }

  private BagObject readElement() {
    if (check(XmlToken.BEGIN_OPEN_ELEMENT) && readToken(XmlToken.WHITESPACE) && (currentToken.emitToken() == XmlToken.OPEN_ELEMENT_NAME)) {
      var element = BagObject.open(_ELEMENT, currentToken.value());
      while (readToken(XmlToken.WHITESPACE)) {
        switch(currentToken.emitToken()) {
          case END_OPEN_ELEMENT -> { return readBody(element); }
          case EMPTY_ELEMENT -> {
            readToken(IGNORE);
            return element;
          }
          case ATTRIBUTE_NAME -> { readAttribute(element); }
          default -> {
            // error
          }
        }
      }
    }
    return null;
  }

  public BagArray readBagArray () {
    scanner.start (input);
    readToken(IGNORE);

    BagArray array = null;
    BagObject element;
    while ((element = readElement()) != null) {
      if (array == null) {
        array = new BagArray();
      }
      array.add(element);
      element = readElement();
    }
    return array;
  }

  static {
    MimeType.addExtensionMapping (MimeType.XML, "xml");
    MimeType.addExtensionMapping (MimeType.HTML, "html");
    FormatReader.registerFormatReader (MimeType.XML, false, FormatReaderXml::new);
    FormatReader.registerFormatReader (MimeType.HTML, false, FormatReaderXml::new);
  }
}
