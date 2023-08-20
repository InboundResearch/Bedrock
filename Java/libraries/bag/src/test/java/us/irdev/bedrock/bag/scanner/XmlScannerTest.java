package us.irdev.bedrock.bag.scanner;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;

public class XmlScannerTest implements Receiver<XmlToken> {
  private static final Logger log = LogManager.getLogger (XmlScannerTest.class);

  @Test
  public void testXmlScannerProlog () {
    new XmlScanner().scanString("<?xml blah blah ?><xml>Fish<!-- comment --> <blah x=\"hello\" jar = 'xxx' />Shark</xml>", this);
  }

  @Test
  public void testXmlScannerComment () {
    new XmlScanner().scanString("<xml>Fish<!-- comment --> <blah x=\"hello\" jar = 'xxx' />Shark</xml>", this);
  }

  @Test
  public void testXmlScannerComment2 () {
    new XmlScanner().scanString("<xml>Fish<!-- comment <blah x=\"hello\" jar = 'xxx' />Shark --></xml>", this);
  }

  @Test
  public void testXmlScanner () {
    new XmlScanner().scanString("<xml>Fish <blah x=\"hello\" jar = 'xxx' />Shark</xml>", this);
  }

  @Test
  public void testXmlScannerError () {
    new XmlScanner().scanString("<xml /x>Fish <blah x=\"hello\" jar = 'xxx' />Shark</xml>", this);
  }

  @Test
  public void testXmlScannerDecl () {
    new XmlScanner().scanString("<?xml?><!DOCTYPE HTML blah \"xxx\">Fish <blah x=\"hello\" jar = 'xxx' />Shark</xml>", this);
  }

  @Override
  public void handleToken(Token<XmlToken> token) {
    log.info (token.emitToken() + " (" + token.value() + ")");
  }
}
