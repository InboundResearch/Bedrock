package us.irdev.bedrock.site;

import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.servlet.Tester;
import us.irdev.bedrock.logger.*;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class Service_Test extends Service {
    private static final Logger log = LogManager.getLogger (Service_Test.class);

    public static final String HEADERS = "headers";

    Tester tester;

    public Service_Test () {
        tester = new Tester (this);
    }

    private BagObject bagObjectFromPost (BagObject query) throws IOException {
        return tester.bagObjectFromPost(new BagObject(), query);
    }

    @Test
    public void testPostIP () throws IOException {
        BagObject query = BagObject.open (EVENT, IP_ADDRESS);
        BagObject response = bagObjectFromPost (query);
        assertEquals(OK, response.getString(STATUS));
        String ipAddress = response.getBagObject (RESPONSE).getString (IP_ADDRESS);
        assertNotNull(ipAddress);
        log.info (IP_ADDRESS + ": " + ipAddress);
    }

    @Test
    public void testPostOk () throws IOException {
        BagObject query = BagObject.open (EVENT, OK);
        BagObject response = bagObjectFromPost (query);
        assertEquals(OK, response.getString(STATUS));
    }

    @Test
    public void testPostHeaders () throws IOException {
        BagObject query = BagObject.open (EVENT, HEADERS);
        BagObject response = bagObjectFromPost (query);
        assertEquals(OK, response.getString(STATUS));
    }

    @Test
    public void testEmptyPost () throws IOException {
        BagObject response = bagObjectFromPost (new BagObject ());
        assertEquals(ERROR, response.getString(STATUS));
    }
}
