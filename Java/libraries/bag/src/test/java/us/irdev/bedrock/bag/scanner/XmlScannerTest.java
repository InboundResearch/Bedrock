package us.irdev.bedrock.bag.scanner;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;

public class XmlScannerTest extends XmlScanner {
  private static final Logger log = LogManager.getLogger (XmlScannerTest.class);

  @Test
  public void testXmlScanner () {
    scan ("<xml><blah x=\"hello\"/></xml>");
  }

  @Override
  public void emit(String actionEmit, String token, String nextStateName) {
    log.info (actionEmit + ": (" + token + ") -> " + nextStateName);
  }
}
