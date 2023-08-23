package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.bag.scanner.Token;
import us.irdev.bedrock.bag.scanner.XmlScanner;
import us.irdev.bedrock.bag.scanner.XmlToken;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;

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
//    not constrained to a single element, and we allow white space in some places the standard
//    does not.
// 2) we read an XML data block as an array of elements, which are in-turn read as BagObjects.
// 3) elements have an element name, content (array of text entries with whitespace preserved), and
//    children. we save these in the BagObject as _element, _content, and _children, in addition to
//    whatever attributes are defined as key-value pairs within the element open tag
// 4) error checking is limited
// 5) HTML is not XML - in particular there are some elements that may or may not have close tags
//    according to the standards (e.g. meta, link, img, br, etc.).
public class FormatReaderXml extends FormatReader implements ArrayFormatReader {
  private static final Logger log = LogManager.getLogger (FormatReaderXml.class);

  private XmlScanner scanner;
  private Token<XmlToken> currentToken;

  private String error = null;

  public static final String _ELEMENT = "_element";
  public static final String _CONTENT = "_content";
  public static final String _CHILDREN = "_children";

  private static final Set<XmlToken> IGNORE_WHITESPACE = Stream.of(XmlToken.WHITESPACE).collect(Collectors.toUnmodifiableSet());

  public FormatReaderXml() {}

  private FormatReaderXml(String input) {
    super (input);
    scanner = new XmlScanner();
  }

  private void onError(String error) {
    if (this.error == null) {
      this.error = error;
      log.error (error);
    }
  }

  private boolean readToken(Set<XmlToken> ignore) {
    while (true) {
      if ((currentToken = scanner.scanToken()) != null) {
        log.info (currentToken.toString());
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
          element.add (_CONTENT, currentToken.value ());
        }
        case BEGIN_OPEN_ELEMENT -> {
          element.add (_CHILDREN, readElement ());
        }
        case BEGIN_CLOSE_ELEMENT -> {
          if (readToken(IGNORE_WHITESPACE)) {
            if ((currentToken.emitToken () == XmlToken.CLOSE_ELEMENT_ANONYMOUS) ||
                    ((currentToken.emitToken () == XmlToken.CLOSE_ELEMENT_NAME) &&
                            element.getString (_ELEMENT).equals (currentToken.value ()))) {
              if (readToken(IGNORE_WHITESPACE) && (currentToken.emitToken () == XmlToken.END_CLOSE_ELEMENT)) {
                return element;
              } else {
                // XXX error
                onError("A");
              }
            } else {
              // XXX error
              onError("B - " + currentToken.toString() + " (should be '" + element.getString (_ELEMENT) + "')");
            }
          } else {
            onError("unexpected end of input");
          }
        }
      }
    }
    return element;
  }

  private boolean readAttribute(BagObject element) {
    // attrname S* = S* ['"] attrvalue ['"]
    if (currentToken.emitToken() == XmlToken.ATTRIBUTE_NAME){
      var attributeName = currentToken.value();
      if (readToken(IGNORE_WHITESPACE) && (currentToken.emitToken() == XmlToken.ATTRIBUTE_EQ) &&
              readToken(IGNORE_WHITESPACE) && (currentToken.emitToken() == XmlToken.OPEN_QUOTE) &&
              readToken() && (currentToken.emitToken() == XmlToken.ATTRIBUTE_VALUE)) {
        element.put(attributeName, currentToken.value());
        return readToken() && (currentToken.emitToken() == XmlToken.CLOSE_QUOTE);
      }
    }
    onError("unexpected token while reading attribute");
    return false;
  }

  private BagObject readElement() {
    if ((currentToken.emitToken() == XmlToken.BEGIN_OPEN_ELEMENT) && readToken(IGNORE_WHITESPACE) && (currentToken.emitToken() == XmlToken.OPEN_ELEMENT_NAME)) {
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
              onError("D");
              return null;
            }
          }
          case WHITESPACE -> {}
          default -> {
            // XXX error
            onError("E");
            return null;
          }
        }
      }
    }
    // XXX error
    onError("F");
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
    MimeType.addExtensionMapping (MimeType.XML, "pom");
    MimeType.addExtensionMapping (MimeType.XML, "xml");
    MimeType.addExtensionMapping (MimeType.HTML, "html");
    MimeType.addMimeTypeMapping (MimeType.XML, "text/xml");
    FormatReader.registerFormatReader (MimeType.XML, false, FormatReaderXml::new);
    FormatReader.registerFormatReader (MimeType.HTML, false, FormatReaderXml::new);
  }
}
