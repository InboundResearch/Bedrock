package us.irdev.bedrock.bag;

import us.irdev.bedrock.bag.formats.MimeType;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SourceAdapterBedrock extends us.irdev.bedrock.bag.SourceAdapter {
    private static final Logger log = LogManager.getLogger (SourceAdapterBedrock.class);

    private static final String UTF_8 = StandardCharsets.UTF_8.name ();
    private static final String API = "API";

    /**
     * Read string data from a remote api source given as a URL string
     * @param urlString
     * @param apiData
     * @throws IOException
     */
    public SourceAdapterBedrock (String urlString, Bag apiData) throws IOException {
        this (new URL (urlString), apiData);
    }

    /**
     * Read string data from a remote api source given as a URL
     * @param url
     * @param apiData
     * @throws IOException
     */
    public SourceAdapterBedrock (URL url, Bag apiData) throws IOException {
        // ensure the call has an actual api call
        if (apiData != null) {
            // create the connection, see if it was successful
            var connection = (HttpURLConnection) url.openConnection ();
            if (connection != null) {
                // don't use the caches
                connection.setUseCaches (false);

                // prepare the api data
                var apiDataString = apiData.toString (MimeType.JSON);
                var apiDataBytes = apiDataString.getBytes (UTF_8);

                // setup the headers
                connection.setRequestMethod(API);
                connection.setRequestProperty("Content-Type", MimeType.JSON + ";charset=" + UTF_8);
                connection.setRequestProperty("Content-Length", Integer.toString(apiDataBytes.length));

                // write out the request data
                connection.setDoOutput (true);
                var outputStream = connection.getOutputStream();
                var dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.write(apiDataBytes);
                dataOutputStream.close();

                // get the response type (this will trigger the actual fetch), then tease out the
                // response type (use a default if it's not present) and the charset (if given,
                // otherwise default to UTF-8, because that's what it will be in Java)
                var contentTypeHeader = connection.getHeaderField("Content-Type");
                var charset = UTF_8;
                mimeType = MimeType.DEFAULT;
                if (contentTypeHeader != null) {
                    var contentType = contentTypeHeader.replace (" ", "").split (";");
                    mimeType = contentType[0];
                    if (contentType.length > 1) {
                        charset = contentType[1].split ("=", 2)[1];
                    }
                    log.debug ("'Content-Type' is " + mimeType + " (charset: " + charset + ")");
                } else {
                    log.warn ("'Content-Type' is not set at the host (" + url.toString () + ")");
                }

                // get the response data
                var inputStream = connection.getInputStream();
                var inputStreamReader = new InputStreamReader (inputStream, charset);
                stringData = readString (inputStreamReader);
                connection.disconnect();
            }
        } else {
            log.warn ("invalid call with empty api data");
            // XXX what else should happen here?
        }
    }
}
