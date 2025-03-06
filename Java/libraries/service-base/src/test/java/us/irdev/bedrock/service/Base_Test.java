package us.irdev.bedrock.service;

import us.irdev.bedrock.bag.*;
import us.irdev.bedrock.servlet.Tester;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class Base_Test extends Base {
    Tester tester;

    public Base_Test () {
        tester = new Tester (this);
    }

    public void handleEventHello (Event event) {
        event.ok (BagObject.open  ("testing", "123"));
    }

    public void handleEventGoodbye (Event event) {
        assertNotNull(event.getQuery());
        assertNotNull(event.getRequest());
        if (event.getQuery ().has ("param3") && (event.getQuery ().getInteger ("param3") == 2)) {
            // deliberately invoke a failure to test the failure handling
            fail();
        }
        event.ok (BagObject.open ("testing", "456"));
    }

    public void handleEventDashName (Event event) {
        event.ok ();
    }

    private void handleEventNope (Event event) {
        event.ok ();
    }

    public void handleEventWithException (Event event) {
        BagObject bagObject = null;
        var err = bagObject.getDouble ("param3");
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
        assertSame(this, getAttribute(SERVLET));
    }

    @Test
    public void testBadInstall () {
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
    public void testGetOk () throws IOException {
        BagObject query = BagObject.open (EVENT, OK);
        assertQuery (bagObjectFromPost (query), query);

        query.put ("param4", 4);
        assertQuery (bagObjectFromPost (query), query);
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
        assertQuery (response, query);
        assertTrue (response.getBagObject (QUERY).has ("testPost"));
        assertEquals(response.getBagObject(QUERY).getBagObject("testPost"), testPost);

        query.put ("param3", 3);
        response = bagObjectFromPost (query);
        assertQuery (response, query);
        assertTrue (response.getBagObject (QUERY).has ("testPost"));
        assertEquals(response.getBagObject(QUERY).getBagObject("testPost"), testPost);

        query.put ("param4", 4);
        assertEquals(ERROR, bagObjectFromPost(query).getString(STATUS));
        query.remove ("param4");

        query.put ("param3", 2);
        assertEquals(ERROR, bagObjectFromPost(query).getString(STATUS));
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

        // the response should be a new object that matches the schema
        assertEquals(response.getBagObject(RESPONSE), getSchema());
    }

    @Test
    public void testBadGet () throws IOException {
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
        assertEquals(ERROR, bagObjectFromPost(query).getString(STATUS));
    }

    @Test
    public void testVersion () throws IOException {
        BagObject query = BagObject.open (EVENT, VERSION);
        BagObject response = bagObjectFromPost (query);
        assertQuery (response, query);
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
        // the test schema actually uses "-dash-name" as the event name (including the
        // leading dash - so that's important
        BagObject query = BagObject.open (EVENT, "-dash-name");
        assertQuery (bagObjectFromPost (query), query);
    }

    @Test
    public void testNope () throws IOException {
        BagObject query = BagObject.open (EVENT, "nope");
        assertEquals(ERROR, bagObjectFromPost(query).getString(STATUS));
    }

    @Test
    public void testWithException () throws IOException {
        BagObject query = BagObject.open (EVENT, "with-exception");
        var response = bagObjectFromPost(query);
        assertNotNull(response);
        assertEquals(ERROR, response.getString(STATUS));
        var errors = response.getBagArray (ERROR);
        assertNotNull(errors);
        assertEquals(1, errors.getCount ());
        var errorString = errors.getString (0);
        assertTrue (errorString.startsWith ("java.lang.NullPointerException"));
    }
}
