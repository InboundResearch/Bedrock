package us.irdev.bedrock.servlet;

import us.irdev.bedrock.bag.*;
import us.irdev.bedrock.bag.formats.MimeType;
import us.irdev.bedrock.servlet.test.TestRequest;
import us.irdev.bedrock.servlet.test.TestResponse;
import us.irdev.bedrock.servlet.test.TestServletConfig;
import us.irdev.bedrock.logger.*;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Tester extends HttpServlet {
    private static final Logger log = LogManager.getLogger (Tester.class);

    public static final String TARGET_DIR = "target";
    public static final String TEST_DIR = "servlet-files";
    public static final String DO_POST = "doPost";

    private File targetTestDir;
    private final HttpServlet httpServlet;
    private final Method doPostMethod;

    public Tester (HttpServlet httpServlet) {
        this.httpServlet = httpServlet;
        try {
            httpServlet.init (new TestServletConfig (getClass ().getName ()));
            targetTestDir = new File (TARGET_DIR, TEST_DIR);
            targetTestDir.mkdirs ();
        } catch (ServletException exception) {
            log.error (exception);
        }

        // find the doPost method for testing
        // httpServlet.doPost (request, response);
        var method = (Method) null;
        try {
            method = httpServlet.getClass ().getMethod (DO_POST, HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException ignored) {}
        var declaredMethod = (Method) null;
        try {
            declaredMethod = httpServlet.getClass().getDeclaredMethod(DO_POST, HttpServletRequest.class, HttpServletResponse.class);
        } catch (NoSuchMethodException ignored) {}
        doPostMethod = (method != null) ? method : declaredMethod;
        assert (doPostMethod != null);
        doPostMethod.setAccessible(true);
    }

    public File fileFromPost (BagObject query, Bag postData) throws IOException {
        return fileFromPost (query.toString (MimeType.URL), postData);
    }

    /**
     * a mock routine to get a file from the hosted servlet as if we'd called it
     * through the web interface
     * @param queryString - the path to the query interface n the webapp
     * @param postData - the actual data to be passed in the body
     * @return the file with the fetch results
     * @throws IOException
     */
    public File fileFromPost (String queryString, Bag postData) throws IOException {
        var request = new TestRequest (queryString, postData);
        var outputFile = new File (targetTestDir, java.util.UUID.randomUUID().toString ());
        var response = new TestResponse (outputFile);

        // invoke the fetch method it as if it were actually being called in a
        // web fetch
        try {
            doPostMethod.invoke (httpServlet, request, response);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            log.error (exception);
        }
        return outputFile;
    }

    private BagObject bagObjectFromFile (File outputFile) {
        var bagObject = BagObjectFrom.file (outputFile);
        outputFile.delete ();
        return bagObject;
    }

    public BagObject bagObjectFromPost (BagObject query, Bag postData) throws IOException {
        return bagObjectFromFile (fileFromPost (query, postData));
    }

    public BagObject bagObjectFromPost (String queryString, Bag postData) throws IOException {
        return bagObjectFromFile (fileFromPost (queryString, postData));
    }

    private BagArray bagArrayFromFile (File outputFile) {
        var bagArray = BagArrayFrom.file (outputFile);
        outputFile.delete();
        return bagArray;
    }

    public BagArray bagArrayFromPost (BagObject query, Bag postData) throws IOException {
        return bagArrayFromFile (fileFromPost (query, postData));
    }

}
