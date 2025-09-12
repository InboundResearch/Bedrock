package us.irdev.bedrock.bag;

import us.irdev.bedrock.bag.formats.MimeType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SourceAdapterHttpTest {
    private static String baseUrl() {
        return System.getProperty("TEST_SERVER_BASE_URL", "http://localhost:8081");
    }
    @Test
    public void testSourceAdapterHttpGet () {
        try {
            SourceAdapter sourceAdapter = new SourceAdapterHttp (baseUrl() + "/api?event=ip-address");
            BagObject responseBagObject = BagObjectFrom.string (sourceAdapter.getStringData (), sourceAdapter.getMimeType ());
            BagTest.report (responseBagObject.getString ("response/ip-address") != null, true, "Got a valid response");
        } catch (IOException exception ){
            BagTest.report (true, false, "An exception is a failure");
        }
    }

    @Test
    public void testSourceAdapterHttpPost () {
        try {
            BagObject bagObject = new BagObject ()
                    .put ("login", "brettonw")
                    .put ("First Name", "Bretton")
                    .put ("Last Name", "Wade");
            SourceAdapter sourceAdapter = new SourceAdapterHttp (baseUrl() + "/api?event=echo-post", bagObject, MimeType.JSON);
            BagObject responseBagObject = BagObjectFrom.string (sourceAdapter.getStringData (), sourceAdapter.getMimeType ());
            BagTest.report (responseBagObject.getString ("login"), "brettonw", "Got a valid response");
        } catch (IOException exception ){
            BagTest.report (true, false, "An exception is a failure");
        }
    }
}
