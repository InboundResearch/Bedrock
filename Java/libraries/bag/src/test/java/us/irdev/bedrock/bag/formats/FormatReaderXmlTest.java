package us.irdev.bedrock.bag.formats;

import org.junit.jupiter.api.Test;
import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagArrayFrom;
import us.irdev.bedrock.bag.expr.Exprs;

import javax.swing.text.AbstractDocument;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static us.irdev.bedrock.bag.formats.FormatReaderXml.*;

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
        var root = BagArrayFrom.file (new File("data", "test.xml"));
        assertNotNull (root);
        assertEquals (6, root.getCount());
        var bagArray = queryPath (root, "node");
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "node");
        assertEquals (bagObject.getBoolean("SELECTED"), true);
        bagArray = bagObject.getBagArray(FormatReaderXml._CONTENT);

        var steps = queryPath (root, "node/steps/step");
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

        var objs = queryPath (root, "node/obj");
        assertEquals(2, objs.getCount());
        bagObject = objs.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "obj");
        assertEquals (bagObject.getString("abc"), "123");
        assertEquals (bagObject.getString("xyz"), "yes");

        bagObject = objs.getBagObject(1);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "obj");
        assertEquals (bagObject.getString("abc"), "456");
        assertEquals (bagObject.getString("xyz"), "no");
    }

    private void confirmHtmlDoc (BagArray root) {
        assertNotNull (root);
        var bagArray = queryPath (root, "html");
        assertEquals (bagArray.getCount(), 1);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "html");

        // check the last div node in the document for correctness, since if that is correct the
        // rest of the document likely got parsed correctly
        bagArray = queryPath (root, "html/body/div");
        assertEquals(2, bagArray.getCount());
        bagArray = queryPath (bagArray.getBagObject(1).getBagArray(_CONTENT), "a");
        bagObject = bagArray.getBagObject (0);
        assertEquals("a", bagObject.getString(_ELEMENT));
        assertEquals("Bedrock", bagObject.getString(_CONTENT));

        bagArray = queryPath (root, "script");
    }

    @Test
    public void testFormatReaderXmlQueryTree () {
        var root = BagArrayFrom.file (new File("data", "test.html"));
        var bagArray = queryTree(root, "class", "footer-link");
        assertEquals(1, bagArray.getCount());
        var bagObject = bagArray.getBagObject (0);
        assertEquals("a", bagObject.getString(_ELEMENT));
        assertEquals("Bedrock", bagObject.getString(_CONTENT));

        // verify it correctly fails to find something not in the tree
        bagArray = queryTree(root, "class", "BLARGH");
        assertEquals(0, bagArray.getCount());

        // verify it with a different type of criteria
        bagArray = queryTree(root, "id", "test-me");
        assertEquals(1, bagArray.getCount());
        bagObject = bagArray.getBagObject (0);
        assertEquals("abc", bagObject.getString("xyz"));
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
        bagArray = bagArray.query(Exprs.equality(FormatReaderXml._ELEMENT, "project"));
        assertEquals (bagArray.getCount(), 1);
        var bagObject = bagArray.getBagObject(0);
        assertNotNull (bagObject);
        assertEquals (bagObject.getString(FormatReaderXml._ELEMENT), "project");
        bagArray = bagObject.getBagArray(FormatReaderXml._CONTENT);

    }
}
