package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.bag.BagObjectFrom;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormatReaderTest {

    @Test
    public void testFormatReader () {
        BagObject bagObject = BagObjectFrom.file (new File ("data", "title.properties"));
        assertTrue (bagObject.getString ("note").equals ("this is a test"));
    }
}
