package us.irdev.bedrock.service;

import us.irdev.bedrock.bag.*;
import us.irdev.bedrock.servlet.Tester;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class Bootstrap_Test extends Base {
    Tester tester;

    public Bootstrap_Test () {
        super("xxx.json");
        tester = new Tester (this);
    }

    public void handleEventHello (Event event) {
        event.ok (BagObject.open  ("testing", "123"));
    }

    public void handleEventGoodbye (Event event) {
        event.ok (BagObject.open ("testing", "456"));
    }

    public void handleEventDashName (Event event) {
        event.ok ();
    }

    private BagObject bagObjectFromPost (BagObject query) throws IOException {
        return tester.bagObjectFromPost(new BagObject (), query);
    }

    private void assertQuery(BagObject bagObject, BagObject query) {
        assertEquals(Base.OK, bagObject.getString(STATUS));
        assertEquals(bagObject.getBagObject(QUERY), query);
    }

    @Test
    public void testAttribute () {
        assertNotNull(getContext());
        assertSame(getAttribute(SERVLET), this);
    }

    @Test
    public void testBadInstall () {
        String event = "JUNK";
        assertFalse (install ("JUNK"));
    }

    @Test
    public void testUnknownEvent () throws IOException {
        BagObject query = BagObject.open (EVENT, "nohandler");
        assertEquals(ERROR, bagObjectFromPost(query).getString(STATUS));
    }

    @Test
    public void testMissingHandler () throws IOException {
        BagObject query = BagObject.open (EVENT, "no-handler");
        assertEquals(ERROR, bagObjectFromPost(query).getString(STATUS));
    }

    @Test
    public void testPostOk () throws IOException {
        BagObject query = BagObject.open (EVENT, OK);
        assertQuery(bagObjectFromPost (query), query);

        query.put ("param4", 4);
        assertQuery(bagObjectFromPost (query), query);
    }

    @Test
    public void testPost () throws IOException {
        BagObject testPost = BagObjectFrom.resource (getClass (), "/testPost.json");
        BagObject query = BagObject
                .open (EVENT, "goodbye")
                .put ("param1", 1)
                .put ("param2", 2)
                .put ("testPost", testPost);
        BagObject response = bagObjectFromPost (query);
        assertQuery(response, query);
        assertTrue (response.getBagObject (QUERY).has ("testPost"));
        assertEquals(response.getBagObject(QUERY).getBagObject("testPost"), testPost);

        query.put ("param3", 3);
        response = bagObjectFromPost (query);
        assertQuery(response, query);
        assertTrue (response.getBagObject (QUERY).has ("testPost"));
        assertEquals(response.getBagObject(QUERY).getBagObject("testPost"), testPost);

        query.put ("param4", 4);
        assertEquals(OK, bagObjectFromPost(query).getString(STATUS));
        query.remove ("param4");

        query.put ("param3", 2);
        assertEquals(OK, bagObjectFromPost(query).getString(STATUS));
    }

    @Test
    public void testEmptyRequest () throws IOException {
        BagObject response = bagObjectFromPost (new BagObject ());
        assertEquals(ERROR, response.getString(STATUS));
        assertEquals("Missing '" + EVENT + "'", response.getString(Key.cat(ERROR, 0)));
    }

    @Test
    public void testHelp () throws IOException {
        BagObject query = BagObject.open (EVENT, HELP);
        BagObject response = bagObjectFromPost (query);
        assertEquals(OK, response.getString(STATUS));

        // make sure the response matches the schema
        assertEquals(response.getBagObject(RESPONSE), getSchema());
    }

    @Test
    public void testBadPost () throws IOException {
        BagObject query = BagObject
                .open (EVENT, "halp")
                .put ("param1", 1)
                .put ("param2", 2);
        assertEquals(ERROR, bagObjectFromPost(query).getString(STATUS));
    }

    @Test
    public void testBadParameters () throws IOException {
        BagObject query = BagObject
                .open (EVENT, "hello")
                .put ("param1", 1)
                .put ("param3", 3);
        assertEquals(OK, bagObjectFromPost(query).getString(STATUS));
    }

    @Test
    public void testVersion () throws IOException {
        BagObject query = BagObject.open (EVENT, VERSION);
        BagObject response = bagObjectFromPost (query);
        assertEquals(OK, response.getString(STATUS));
    }

    @Test
    public void testMultiple () throws IOException {
        BagObject query = BagObject
                .open (EVENT, MULTIPLE)
                .put (EVENTS, BagArray
                        .open (BagObject.open (EVENT, VERSION))
                        .add (BagObject.open (EVENT, HELP))
                        .add (BagObject.open (EVENT, OK))
                );
        var response = bagObjectFromPost (query);
        assertQuery (response, query);
    }

    @Test
    public void testDashName () throws IOException {
        BagObject query = BagObject.open (EVENT, "dash-name");
        assertQuery(bagObjectFromPost (query), query);
    }
}
