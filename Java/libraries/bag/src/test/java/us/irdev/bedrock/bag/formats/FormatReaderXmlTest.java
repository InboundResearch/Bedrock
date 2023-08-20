package us.irdev.bedrock.bag.formats;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.bag.BagArrayFrom;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FormatReaderXmlTest {
    @Test
    public void testFormatReaderXml () {
        var bagArray = BagArrayFrom.file (new File("data", "test.xml"));
        assertNotNull (bagArray);
        assertEquals (bagArray.getCount(), 1);
    }

}
