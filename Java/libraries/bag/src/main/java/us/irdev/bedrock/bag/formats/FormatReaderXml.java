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

// reads an XML-ish formatted string (in Java/UTF-8 encoding), without some of the constraints or
// extended capabilities (namespaces, schema support/enforcement, etc.). The intent is to support
// basic data extraction from XML format, maven POM files, and HTML from web pages. See the
// description of XML at https://www.w3.org/TR/xml11/
//
// NOTES:
// 1) we support data in XML-ish format, not strict XML documents. For instance, we allow white
//    space in some places the standard does not, we allow anonymous end tags ("</>"), attributes
//    without values, etc.
// 2) we focused on "happy path" handling, reading reasonably well-formed XML or HTML files, not
//    necessarily dealing with poorly-formed examples. error checking is limited. diagnostics for
//    the location of an error in the file is a future feature.
// 3) we read an XML data block as an array of elements (a BagArray of BagObjects). elements have an
//    element name and content (including child nodes) stored as an array. we save these in the
//    BagObject with the keys __element and __content, in addition to whatever attributes are
//    defined as key-value pairs within the element open tag. prolog and decl tags are delivered
//    without parsing their bodies.
// 4) element content is interleaved with the children as strings. this allows the document to be
//    recreated from the internal representation (though we don't actually support this). users will
//    probably want to extract child elements separately, but we felt it was important to retain the
//    file ordering of content.
// 4) HTML is not XML, but we leverage the XML parser structure to read HTML. the primary difference
//    is there are some HTML elements that do not have bodies, and therefore do not have close tags.
//    these are referred to as void elements, and are in common use (e.g. meta, link, img, br,
//    etc.). furthermore, most browsers allow these elements to be presented with or without close
//    tags. we handle these by returning the found element immediately after the end of the open tag
//    (not reading any content), and by ignoring the close tag if we see it. this could allow some
//    poorly formed HTML content to be read, by randomly interspersing close tags for void elements.
//    most browsers seem to allow this as well.
public class FormatReaderXml extends FormatReader implements ArrayFormatReader {
  private static final Logger log = LogManager.getLogger (FormatReaderXml.class);

  private XmlScanner scanner;
  private Token<XmlToken> currentToken;
  private Set<String> voidElements = null;
  private String error = null;

  // names of the fields we use in elements
  public static final String _ELEMENT = "__element";
  public static final String _CONTENT = "__content";

  // a set of tokens for ignoring whitespace
  private static final Set<XmlToken> IGNORE_WHITESPACE = Stream.of(
          XmlToken.WHITESPACE
  ).collect(Collectors.toUnmodifiableSet());

  // a set of element names for void elements in standard HTML
  public static final Set<String> HTML_VOID_ELEMENTS = Stream.of(
          "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "source", "track", "wbr"
  ).collect(Collectors.toUnmodifiableSet());

  public FormatReaderXml() {}

  protected FormatReaderXml(String input) {
    super (input);
    scanner = new XmlScanner();
  }

  protected FormatReaderXml(String input, Set<String> voidElements) {
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
    while ((error == null) && ((currentToken = scanner.scanToken()) != null)) {
      log.info (currentToken.toString());
      if ((ignore == null) || (! ignore.contains(currentToken.emitToken()))) {
        return true;
      }
    }
    currentToken = null;
    return false;
  }

  private boolean readToken() {
    return readToken(null);
  }

  private BagArray readContent (String elementName, BagArray array) {
    while (readToken ()) {
      switch (currentToken.emitToken ()) {
        case COMMENT, DECL, PROLOG -> array.add (BagObject
                .open(_ELEMENT, "__" + currentToken.emitToken())
                .add(_CONTENT, currentToken.value())
        );
        case CONTENT -> array.add (currentToken.value ());
        case BEGIN_OPEN_ELEMENT -> array.add (readElement ());
        case BEGIN_CLOSE_ELEMENT -> {
          if (readToken(IGNORE_WHITESPACE)) {
            // ignore the close tag if it is a void element (but still read the rest of it)
            switch (currentToken.emitToken ()) {
              case CLOSE_ELEMENT_ANONYMOUS -> {
                // the scanner is constructed such that the next token must be END_CLOSE_ELEMENT, so
                // there is no need to check it. note this isn't standard, we support it for the
                // sake of simplicity in writing xml files.
                readToken ();
                return array;
              }
              case CLOSE_ELEMENT_NAME -> {
                var name = currentToken.value ();
                // check if it's a void element
                if ((voidElements == null) || (!voidElements.contains (name))) {
                  // a normal close tag should match the name of the current element
                  if (elementName.equals (name)) {
                    if (readToken (IGNORE_WHITESPACE) && (currentToken.emitToken () == XmlToken.END_CLOSE_ELEMENT)) {
                      return array;
                    } else {
                      onError("Unexpected token while reading close element tag: " + currentToken.toString());
                    }
                  } else {
                    onError ("Bad token - " + currentToken.toString () + " (should be '" + elementName + "')");
                  }
                } else {
                  // for a void element we should just ignore a properly constructed close tag.
                  // technically, it shouldn't occur in the file at all, but lots of parsers ignore these
                  // close tags, so they still show up in files
                  readToken (IGNORE_WHITESPACE);
                  log.warn("Ignoring close tag for void element (" + name + ")");
                }
              }
              default -> {
                onError("Unexpected token while reading close element tag: " + currentToken.toString());
              }
            }
          } else {
            onError("unexpected end of input while reading close element");
          }
        }
        default -> {
          onError("Unexpected token while reading content: " + currentToken.toString());
        }
      }
    }
    return array;
  }

  private BagObject readContent(String elementName, BagObject element) {
    if ((voidElements == null) || (! voidElements.contains(elementName))) {
      var content = readContent(elementName, new BagArray());
      if (content.getCount() > 0) {
        element.add(_CONTENT, content);
      }
    }
    return element;
  }

  private boolean readAttribute(BagObject element) {
    // attrname S* = S* ['"] attrvalue ['"]
    if (currentToken.emitToken() == XmlToken.ATTRIBUTE_NAME) {
      var attributeName = currentToken.value ();

      // strict XML requires attributes to be name="value" or name='value'. old HTML allows just the
      // name to show up, so we support it with a boolean type.
      if (readToken (IGNORE_WHITESPACE)) {
        switch (currentToken.emitToken ()) {
          case ATTRIBUTE_EQ -> {
            if (readToken (IGNORE_WHITESPACE) && (currentToken.emitToken () == XmlToken.OPEN_QUOTE) &&
                    readToken () && (currentToken.emitToken () == XmlToken.ATTRIBUTE_VALUE)) {
              element.put (attributeName, currentToken.value ());
              return readToken () && (currentToken.emitToken () == XmlToken.CLOSE_QUOTE);
            }
          }
          case END_OPEN_ELEMENT -> {
            element.put (attributeName, true);
            return true;
          }
        }
      }
    }
    return false;
  }

  private BagObject readElement() {
    if ((currentToken.emitToken() == XmlToken.BEGIN_OPEN_ELEMENT) && readToken(IGNORE_WHITESPACE) && (currentToken.emitToken() == XmlToken.OPEN_ELEMENT_NAME)) {
      var elementName = currentToken.value();
      var element = BagObject.open(_ELEMENT, elementName);
      while (readToken()) {
        switch(currentToken.emitToken()) {
          case END_OPEN_ELEMENT -> {
            return readContent(elementName, element);
          }
          case EMPTY_ELEMENT -> {
            return element;
          }
          case ATTRIBUTE_NAME -> {
            if (readAttribute(element)) {
              if (currentToken.emitToken() == XmlToken.END_OPEN_ELEMENT) {
                return readContent (elementName, element);
              }
            }else {
              onError("Unexpected token while reading attribute: " + currentToken.toString());
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
    onError("Unexpected end of input while reading element tag open");
    return null;
  }

  public BagArray readBagArray () {
    // initialize the scanner with the input
    scanner.start (input);
    error = null;

    // read the content
    var array = new BagArray ();
    do{
      log.info("pump");
      readContent ("__root", array);
    } while (currentToken != null);
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
