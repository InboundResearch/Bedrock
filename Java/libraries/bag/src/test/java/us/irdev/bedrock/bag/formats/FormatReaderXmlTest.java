package us.irdev.bedrock.bag.formats;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagArrayFrom;
import us.irdev.bedrock.bag.SourceAdapter;
import us.irdev.bedrock.bag.SourceAdapterHttp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FormatReaderXmlTest {
    @Test
    public void testFormatReaderXmlFromString () {

        var reader = new FormatReaderXml ();
    }

    @Test
    public void testFormatReaderXmlFromFile () {
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
        bagObject = steps.getBagObject(0);
        assertEquals(bagObject.getString(FormatReaderXml._ELEMENT), "step");
        assertEquals(bagObject.getString(FormatReaderXml._CONTENT), "one");
        bagObject = steps.getBagObject(1);
        assertEquals(bagObject.getString(FormatReaderXml._ELEMENT), "step");
        assertEquals(bagObject.getString(FormatReaderXml._CONTENT), "two");
        bagObject = steps.getBagObject(2);
        assertEquals(bagObject.getString(FormatReaderXml._ELEMENT), "step");
        assertEquals(bagObject.getString(FormatReaderXml._CONTENT), "three");


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
    }

    @Test
    public void testFormatReaderXmlWithHtmlFromUrl () {
        var bagArray = BagArrayFrom.url("https://bedrock.brettonw.com");
        assertNotNull (bagArray);
        assertEquals (bagArray.getCount(), 1);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "html");
        bagArray = bagObject.getBagArray(FormatReaderXml._CHILDREN);
    }

    private static BagArray fetchBagArray (String urlString, String mimeType) {
        try {
            var url = URI.create (urlString).toURL();
            var sourceAdapter = new SourceAdapterHttp (url);
            if (mimeType != null) {
                sourceAdapter.setMimeType (mimeType);
            }
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            return null;
        }
    }

    private static BagArray fetchBagArray (String urlString) {
        return fetchBagArray(urlString, null);
    }

    @Test
    public void testFormatReaderXmlWithPomFromUrl () {
        //var bagArray = fetchBagArray("https://raw.githubusercontent.com/InboundResearch/Bedrock/development/Java/pom.xml", MimeType.XML);
        var bagArray = fetchBagArray("https://repo1.maven.org/maven2/us/irdev/bedrock/bedrock/2.2.6/bedrock-2.2.6.pom");

        assertNotNull (bagArray);
        assertEquals (bagArray.getCount(), 1);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "project");
        bagArray = bagObject.getBagArray(FormatReaderXml._CHILDREN);

    }
}
