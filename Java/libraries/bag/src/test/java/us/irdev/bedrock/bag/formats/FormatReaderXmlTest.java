package us.irdev.bedrock.bag.formats;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.bag.BagArrayFrom;

import java.io.File;
import java.util.logging.XMLFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FormatReaderXmlTest {
    @Test
    public void testFormatReaderXml () {
        var bagArray = BagArrayFrom.file (new File("data", "test.xml"));
        assertNotNull (bagArray);
        assertEquals (bagArray.getCount(), 1);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "node");
        bagArray = bagObject.getBagArray(FormatReaderXml._CHILDREN);
        bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "steps");
        var steps = bagObject.getBagArray(FormatReaderXml._CHILDREN);
        assertEquals(steps.getCount (), 3);
        bagObject = bagArray.getBagObject(1);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "obj");
        assertEquals (bagObject.getString("abc"), "123");
        bagObject = bagArray.getBagObject(2);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "obj");
        assertEquals (bagObject.getString("xyz"), "no");
    }

}
