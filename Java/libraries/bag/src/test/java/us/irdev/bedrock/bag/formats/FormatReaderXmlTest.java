package us.irdev.bedrock.bag.formats;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagArrayFrom;
import us.irdev.bedrock.bag.expr.Exprs;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FormatReaderXmlTest {
    @Test
    public void testFormatReaderXmlFromEmptyString () {
        var reader = new FormatReaderXml ("");
        var bagArray = reader.readBagArray();
        assertNotNull(bagArray);
        assertEquals(bagArray.getCount(), 0);
    }

    @Test
    public void testFormatReaderXmlFromFile () {
        var bagArray = BagArrayFrom.file (new File("data", "test.xml"));
        assertNotNull (bagArray);
        assertEquals (bagArray.getCount(), 1);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "node");
        assertEquals (bagObject.getBoolean("SELECTED"), true);
        bagArray = bagObject.getBagArray(FormatReaderXml._CONTENT);

        var steps = bagArray.query(Exprs.equality(FormatReaderXml._ELEMENT, "steps"));
        bagObject = steps.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "steps");
        steps = bagObject.getBagArray(FormatReaderXml._CONTENT).query(Exprs.equality(FormatReaderXml._ELEMENT, "step"));
        assertEquals(steps.getCount (), 3);
        bagObject = steps.getBagObject(0);
        assertEquals(bagObject.getString(FormatReaderXml._ELEMENT), "step");
        assertEquals(bagObject.getString(FormatReaderXml._CONTENT), "one");
        bagObject = steps.getBagObject(1);
        assertEquals(bagObject.getString(FormatReaderXml._ELEMENT), "step");
        assertEquals(bagObject.getString(FormatReaderXml._CONTENT), "two");
        bagObject = steps.getBagObject(2);
        assertEquals(bagObject.getString(FormatReaderXml._ELEMENT), "step");
        assertEquals(bagObject.getString(FormatReaderXml._CONTENT), "three");

/*
        bagObject = bagArray.getBagObject(1);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "obj");
        assertEquals (bagObject.getString("abc"), "123");
        assertEquals (bagObject.getString("xyz"), "yes");

        bagObject = bagArray.getBagObject(2);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "obj");
        assertEquals (bagObject.getString("abc"), "456");
        assertEquals (bagObject.getString("xyz"), "no");
        */
    }

    private void confirmHtmlDoc (BagArray bagArray) {
        assertNotNull (bagArray);
        assertEquals (bagArray.getCount(), 2);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "html");
        bagArray = bagObject.getBagArray(FormatReaderXml._CONTENT);
        assertEquals(bagArray.getCount(), 3);
        bagObject = bagArray.getBagObject(0);
        assertEquals(bagObject.getString(FormatReaderXml._ELEMENT), "head");
    }

    @Test
    public void testFormatReaderXmlWithHtmlFromFile () {
        confirmHtmlDoc(BagArrayFrom.file (new File("data", "test.html")));
    }

    @Test
    public void testFormatReaderXmlWithHtmlFromUrl () {
        confirmHtmlDoc(BagArrayFrom.url("https://bedrock.brettonw.com"));
    }

    @Test
    public void testFormatReaderXmlWithPomFromUrl () {
        //var bagArray = BagArrayFrom.url("https://raw.githubusercontent.com/InboundResearch/Bedrock/development/Java/pom.xml", MimeType.XML);
        var bagArray = BagArrayFrom.url("https://repo1.maven.org/maven2/us/irdev/bedrock/bedrock/2.2.6/bedrock-2.2.6.pom");
        assertNotNull (bagArray);
        assertEquals (bagArray.getCount(), 1);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "project");
        bagArray = bagObject.getBagArray(FormatReaderXml._CONTENT);

    }
}
