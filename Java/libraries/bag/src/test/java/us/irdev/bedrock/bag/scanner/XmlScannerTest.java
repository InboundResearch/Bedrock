package us.irdev.bedrock.bag.scanner;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;

public class XmlScannerTest extends XmlScanner {
  private static final Logger log = LogManager.getLogger (XmlScannerTest.class);

  @Test
  public void testXmlScannerProlog () {
    scanString("<?xml blah blah ?><xml>Fish<!-- comment --> <blah x=\"hello\" jar = 'xxx' />Shark</xml>");
  }

  @Test
  public void testXmlScannerComment () {
    scanString("<xml>Fish<!-- comment --> <blah x=\"hello\" jar = 'xxx' />Shark</xml>");
  }

  @Test
  public void testXmlScannerComment2 () {
    scanString("<xml>Fish<!-- comment <blah x=\"hello\" jar = 'xxx' />Shark --></xml>");
  }

  @Test
  public void testXmlScanner () {
    scanString("<xml>Fish <blah x=\"hello\" jar = 'xxx' />Shark</xml>");
  }

  @Test
  public void testXmlScannerError () {
    scanString("<xml /x>Fish <blah x=\"hello\" jar = 'xxx' />Shark</xml>");
  }

  @Test
  public void testXmlScannerDecl () {
    scanString("<xml><!DOCTYPE blah blah \"xxx\">Fish <blah x=\"hello\" jar = 'xxx' />Shark</xml>");
  }

  @Override
  public void emit(String actionEmit, String token, String nextStateName) {
    log.info (actionEmit + ": (" + token + ") -> " + nextStateName);
  }
}
