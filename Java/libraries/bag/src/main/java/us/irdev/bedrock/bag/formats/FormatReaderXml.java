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

  public static final String _ELEMENT = "_element";
  public static final String _BODY = "_body";
  public static final String _CHILDREN = "_children";

  private static final Set<XmlToken> IGNORE_TOP_LEVEL = Stream.of(XmlToken.COMMENT, XmlToken.BODY, XmlToken.DECL, XmlToken.PROLOG).collect(Collectors.toUnmodifiableSet());
  private static final Set<XmlToken> IGNORE_IN_BODY = Stream.of(XmlToken.COMMENT, XmlToken.DECL, XmlToken.PROLOG).collect(Collectors.toUnmodifiableSet());
  private static final Set<XmlToken> IGNORE_WHITESPACE = Stream.of(XmlToken.WHITESPACE).collect(Collectors.toUnmodifiableSet());


  public FormatReaderXml() {}

  private FormatReaderXml(String input) {
    super (input);
    scanner = new XmlScanner();
  }

  private boolean readToken(Set<XmlToken> ignore) {
    while (true) {
      if ((currentToken = scanner.scanToken()) != null) {
        if ((ignore == null) || (! ignore.contains(currentToken.emitToken()))) {
          return true;
        }
      } else {
        return false;
      }
    }
  }

  private boolean readToken() {
    return readToken(null);
  }

  private boolean check(XmlToken token) {
    return (currentToken != null) && (currentToken.emitToken() == token);
  }

  private boolean expect(XmlToken token, Set<XmlToken> ignore) {
    if (check (token)) {
      return readToken(ignore);
    } else {
      return false;
    }
  }

  private boolean expect(XmlToken token) {
    return expect(token, null);
  }

  private BagObject readBody(BagObject element) {
    while (readToken ()) {
      switch (currentToken.emitToken ()) {
        case COMMENT, DECL, PROLOG -> {
        }
        case BODY -> {
          element.add (_BODY, currentToken.value ());
        }
        case BEGIN_OPEN_ELEMENT -> {
          element.add (_CHILDREN, readElement ());
        }
        case BEGIN_CLOSE_ELEMENT -> {
          if (readToken () && (currentToken.emitToken () == XmlToken.CLOSE_ELEMENT_NAME)) {
            // XXX add allow for anonymous close element (via an empty close_element_name)?
            // XXX it's not strictly allowed in XML, do we care?
            var elementName = currentToken.value ();
            if (elementName.equals ("") || element.getString (_ELEMENT).equals (elementName)) {
              while (readToken () && (currentToken.emitToken () == XmlToken.WHITESPACE)) { }
              if (currentToken.emitToken () == XmlToken.END_CLOSE_ELEMENT) {
                return element;
              } else {
                // XXX some sort of error?
              }
            } else {
              // continue until we get the proper close element
              // XXX this could be an error, or it could be some script code insode a script node that has what looks like a close tag...
            }
          }
        }
      }
    }
    return element;
  }

  private boolean readAttribute(BagObject element) {
    if (currentToken.emitToken() == XmlToken.ATTRIBUTE_NAME){
      var attributeName = currentToken.value();
      readToken();
      if (expect(XmlToken.ATTRIBUTE_EQ) && expect(XmlToken.OPEN_QUOTE) && check(XmlToken.ATTRIBUTE_VALUE)) {
        element.put(attributeName, currentToken.value());
        return readToken() && (currentToken.emitToken() == XmlToken.CLOSE_QUOTE);
      }
    }
    return false;
  }

  private BagObject readElement() {
    if (expect (XmlToken.BEGIN_OPEN_ELEMENT, IGNORE_WHITESPACE) && (currentToken.emitToken() == XmlToken.OPEN_ELEMENT_NAME)) {
      var element = BagObject.open(_ELEMENT, currentToken.value());
      while (readToken()) {
        switch(currentToken.emitToken()) {
          case END_OPEN_ELEMENT -> {
            return readBody(element);
          }
          case EMPTY_ELEMENT -> {
            return element;
          }
          case ATTRIBUTE_NAME -> {
            if (! readAttribute(element)) {
              // XXX ERROR
              return null;
            }
          }
          case WHITESPACE -> {}
          default -> {
            // XXX error
            return null;
          }
        }
      }
    }
    // XXX error
    return null;
  }

  public BagArray readBagArray () {
    scanner.start (input);

    BagArray array = null;
    while (readToken()) {
      if (currentToken.emitToken() == XmlToken.BEGIN_OPEN_ELEMENT) {
        var element = readElement ();
        if (array == null) {
          array = new BagArray();
        }
        array.add(element);
      }
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
