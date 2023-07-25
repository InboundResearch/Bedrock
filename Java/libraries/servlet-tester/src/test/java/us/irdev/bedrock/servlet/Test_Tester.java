package us.irdev.bedrock.servlet;

import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.bag.BagObjectFrom;
import us.irdev.bedrock.bag.SourceAdapter;
import us.irdev.bedrock.bag.SourceAdapterReader;
import us.irdev.bedrock.bag.formats.MimeType;
import us.irdev.bedrock.logger.*;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Test_Tester extends HttpServlet {
    private static final Logger log = LogManager.getLogger (Test_Tester.class);

    public static final String OK_KEY = "ok";
    public static final String ERROR_KEY = "error";
    public static final String STATUS_KEY = "status";
    public static final String POST_DATA_KEY = "post-data";
    public static final String IP_KEY = "ip";
    public static final String COMMAND_KEY = "command";
    public static final String TEST_KEY = "test";

    Tester servletTester;

    public Test_Tester () {
        servletTester = new Tester (this);
    }

    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug ("doPost");

        BagObject query = BagObjectFrom.string (request.getQueryString (), MimeType.URL);
        BagObject responseObject = new BagObject (query).put (IP_KEY, request.getRemoteAddr ());
        if (query.getString (COMMAND_KEY).equals (TEST_KEY)) {
            SourceAdapter sourceAdapter = new SourceAdapterReader (request.getInputStream (), MimeType.JSON);
            String postDataString = sourceAdapter.getStringData ();
            BagObject postData = BagObjectFrom.string (postDataString);
            makeResponse (response, responseObject.put (STATUS_KEY, OK_KEY).put (POST_DATA_KEY, postData).toString ());
        } else {
            makeResponse (response, responseObject.put (STATUS_KEY, ERROR_KEY).toString ());
        }
    }

    public void makeResponse (HttpServletResponse response, String responseText) throws IOException {
        // set the response types
        String UTF_8 = StandardCharsets.UTF_8.name ();
        response.setContentType (MimeType.JSON + "; charset=" + UTF_8);
        response.setCharacterEncoding (UTF_8);

        // write out the response
        PrintWriter out = response.getWriter ();
        out.println (responseText);
        //out.flush ();
        out.close ();
    }

    private void doGetAssert (BagObject bagObject) {
        assertEquals(OK_KEY, bagObject.getString(STATUS_KEY));
        assertNotNull(bagObject.getString(IP_KEY));
        log.info (IP_KEY + ": " + bagObject.getString (IP_KEY));
    }

    private void doPostAssert (BagObject bagObject, BagObject postData) {
        doGetAssert (bagObject);
        assertEquals(bagObject.getBagObject(POST_DATA_KEY), postData);
    }

    @Test
    public void testPostByObject () throws IOException {
        BagObject postData = BagObjectFrom.resource (getClass (), "/testPost.json");
        doPostAssert (servletTester.bagObjectFromPost (BagObject.open (COMMAND_KEY, TEST_KEY), postData), postData);
    }

    @Test
    public void testPostByString () throws IOException {
        BagObject postData = BagObjectFrom.resource (getClass (), "/testPost.json");
        doPostAssert (servletTester.bagObjectFromPost (BagObject.open (COMMAND_KEY, TEST_KEY).toString (MimeType.URL), postData), postData);
    }

    @Test
    public void testServletContextPaths () throws IOException {
        String resourcePath = "/WEB-INF/configuration.json";
        String path = getServletContext ().getRealPath (resourcePath);
        log.info ("configuration.json path = " + path);

        InputStream resourceAsStream = getServletContext ().getResourceAsStream (resourcePath);
        BagObject configuration = BagObjectFrom.inputStream (resourceAsStream);
        assertNotNull(configuration);
        assertEquals(OK_KEY, configuration.getString(STATUS_KEY));
    }
}
