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
//    does not. we also focused on "happy path" handling, reading well-formed XML or HTML files, not
//    necessarily dealing with poorly written ones.
// 2) we read an XML data block as an array of elements, which are in-turn read as BagObjects.
// 3) elements have an element name, content (array of text entries with whitespace preserved), and
//    children. we save these in the BagObject as __element, __content, and __children, in addition
//    to whatever attributes are defined as key-value pairs within the element open tag.
// 4) error checking is limited.
// 5) HTML is not XML - in particular there are some elements that may or may not have bodies or
//    close tags according to the standards (e.g. meta, link, img, br, etc.). we handle these by
//    returning immediately after the end of the open tag (not reading a body), and by ignoring the
//    close tag if we see it.
public class FormatReaderXml extends FormatReader implements ArrayFormatReader {
  private static final Logger log = LogManager.getLogger (FormatReaderXml.class);

  private XmlScanner scanner;
  private Token<XmlToken> currentToken;
  private Set<String> voidElements = null;
  private String error = null;

  // names of the fields we use in elements
  public static final String _ELEMENT = "__element";
  public static final String _CONTENT = "__content";
  public static final String _CHILDREN = "__children";

  // a set of tokens for ignoring whitespace
  private static final Set<XmlToken> IGNORE_WHITESPACE = Stream.of(
          XmlToken.WHITESPACE
  ).collect(Collectors.toUnmodifiableSet());

  // a set of element names for void elements in standard HTML
  public static final Set<String> HTML_VOID_ELEMENTS = Stream.of(
          "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "source", "track", "wbr"
  ).collect(Collectors.toUnmodifiableSet());

  public FormatReaderXml() {}

  private FormatReaderXml(String input) {
    super (input);
    scanner = new XmlScanner();
  }

  private FormatReaderXml(String input, Set<String> voidElements) {
    super (input);
    scanner = new XmlScanner();
    this.voidElements = voidElements;
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
        log.debug (currentToken.toString());
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
            // ignore the close tag if it is a void element (but still read the rest of it)
            switch (currentToken.emitToken ()) {
              case CLOSE_ELEMENT_ANONYMOUS -> {
                // the scanner is constructed such that the next token must be END_CLOSE_ELEMENT, so
                // there is no need to check it. note this isn't standard, we support it for the
                // sake of simplicity in writing xml files.
                readToken ();
                return element;
              }
              case CLOSE_ELEMENT_NAME -> {
                var name = currentToken.value ();
                // check if it's a void element
                if ((voidElements == null) || (!voidElements.contains (name))) {
                  // a normal close tag should match the name of the current element
                  if (element.getString (_ELEMENT).equals (name)) {
                    if (readToken (IGNORE_WHITESPACE) && (currentToken.emitToken () == XmlToken.END_CLOSE_ELEMENT)) {
                      return element;
                    } else {
                      // XXX error
                      onError ("Bad token - " + currentToken.toString () + " (should be '" + element.getString (_ELEMENT) + "')");
                    }
                  }
                } else {
                  // for a void element we should just ignore a properly constructed close tag.
                  // technically, it shouldn't occur in the file at all, but lots of parsers ignore these
                  // close tags, so they still show up in files
                  readToken (IGNORE_WHITESPACE);
                  log.info("Ignoring close tag for void element (" + name + ")");
                }
              }
              default -> {
                onError("Unexpected token while reading close element: " + currentToken.toString());
              }
            }
          } else {
            onError("unexpected end of input while reading close element");
          }
        }
        default -> {
          onError("Unexpected token while reading body: " + currentToken.toString());
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
    onError("Unexpected token while reading attribute: " + currentToken.toString());
    return false;
  }

  private BagObject readElement() {
    if ((currentToken.emitToken() == XmlToken.BEGIN_OPEN_ELEMENT) && readToken(IGNORE_WHITESPACE) && (currentToken.emitToken() == XmlToken.OPEN_ELEMENT_NAME)) {
      var name = currentToken.value();
      var element = BagObject.open(_ELEMENT, name);
      while (readToken()) {
        switch(currentToken.emitToken()) {
          case END_OPEN_ELEMENT -> {
            if ((voidElements == null) || (! voidElements.contains(name))) {
              return readBody (element);
            } else {
              return element;
            }
          }
          case EMPTY_ELEMENT -> {
            return element;
          }
          case ATTRIBUTE_NAME -> {
            if (! readAttribute(element)) {
              // XXX ERROR - resolve these error conditions a bit better
              onError("D");
              return null;
            }
          }
          case WHITESPACE -> {}
          default -> {
            onError("Unexpected token while reading element tag: " + currentToken.toString());
            return null;
          }
        }
      }
    }
    // XXX error
    onError("End of input encountered without reading any elements");
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
    MimeType.addMimeTypeMapping (MimeType.XML, "text/xml");
    FormatReader.registerFormatReader (MimeType.XML, false, FormatReaderXml::new);

    MimeType.addExtensionMapping (MimeType.HTML, "htm");
    MimeType.addExtensionMapping (MimeType.HTML, "html");
    FormatReader.registerFormatReader (MimeType.HTML, false, (input) -> { return new FormatReaderXml(input, HTML_VOID_ELEMENTS); });

    MimeType.addExtensionMapping (MimeType.XHTML, "xhtml");
    FormatReader.registerFormatReader (MimeType.XHTML, false, FormatReaderXml::new);
  }
}
