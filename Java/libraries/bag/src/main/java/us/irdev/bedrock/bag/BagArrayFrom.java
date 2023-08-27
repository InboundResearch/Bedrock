package us.irdev.bedrock.bag;

import us.irdev.bedrock.bag.formats.FormatReader;
import us.irdev.bedrock.bag.formats.MimeType;
import us.irdev.bedrock.logger.*;


import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Supplier;

public class BagArrayFrom {
    private static final Logger log = LogManager.getLogger (BagArrayFrom.class);

    // from a string, with the mime type specified
    static public BagArray string (String string) {
        return string (string, MimeType.DEFAULT);
    }

    static public BagArray string (String string, Supplier<BagArray> fail) {
        return string (string, MimeType.DEFAULT, fail);
    }

    static public BagArray string (String string, String mimeType) {
        return string (string, mimeType, () -> null);
    }

    static public BagArray string (String string, String mimeType, Supplier<BagArray> fail) {
        try {
            var sourceAdapter = new SourceAdapterReader(string, mimeType);
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    // from a file, with the mime type specified
    static public BagArray file (File file) {
        return file (file, () -> null);
    }

    static public BagArray file (File file, Supplier<BagArray> fail) {
        return file (file, MimeType.DEFAULT, fail);
    }

    static public BagArray file (File file, String mimeType) {
        return file (file, mimeType, () -> null);
    }

    static public BagArray file (File file, String mimeType, Supplier<BagArray> fail) {
        try {
            var sourceAdapter = new SourceAdapterReader(file, mimeType);
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    // from a resource, with the mime type specified
    static public BagArray resource (Class context, String name) {
        return resource (context, name, () -> null);
    }

    static public BagArray resource (Class context, String name, Supplier<BagArray> fail) {
        return resource (context, name, MimeType.DEFAULT, fail);
    }

    static public BagArray resource (Class context, String name, String mimeType) {
        return resource (context, name, mimeType, () -> null);
    }

    static public BagArray resource (Class context, String name, String mimeType, Supplier<BagArray> fail) {
        try {
            var sourceAdapter = new SourceAdapterReader (context, name, mimeType);
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    // from a stream, with the mime type specified
    static public BagArray inputStream (InputStream inputStream) {
        return inputStream (inputStream, MimeType.DEFAULT);
    }

    static public BagArray inputStream (InputStream inputStream, Supplier<BagArray> fail) {
        return inputStream (inputStream, MimeType.DEFAULT, fail);
    }

    static public BagArray inputStream (InputStream inputStream, String mimeType) {
        return inputStream (inputStream, mimeType, () -> null);
    }

    static public BagArray inputStream (InputStream inputStream, String mimeType, Supplier<BagArray> fail) {
        try {
            var sourceAdapter = new SourceAdapterReader(inputStream, mimeType);
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    // from a HTTP connection (get)
    static public BagArray url (String urlString) {
        return url (urlString, null, () -> null);
    }

    static public BagArray url (String urlString, String overrideMimeType) {
        return url (urlString, overrideMimeType, () -> null);
    }

    static public BagArray url (String urlString, Supplier<BagArray> fail) {
        return url (urlString, null, fail);
    }

    static public BagArray url (String urlString, String overrideMimeType, Supplier<BagArray> fail) {
        try {
            var url = URI.create (urlString).toURL();
            return url (url, overrideMimeType, fail);
        } catch (MalformedURLException exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    static public BagArray url (URL url) {
        return url (url, null, () -> null);
    }

    static public BagArray url (URL url, String overrideMimeType) {
        return url (url, overrideMimeType, () -> null);
    }

    static public BagArray url (URL url, Supplier<BagArray> fail) {
        return url (url, null, fail);
    }

    static public BagArray url (URL url, String overrideMimeType, Supplier<BagArray> fail) {
        try {
            var sourceAdapter = new SourceAdapterHttp(url);
            if (overrideMimeType != null) {
                sourceAdapter.setMimeType (overrideMimeType);
            }
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    // from a HTTP connection (post)
    static public BagArray url (String urlString, Bag postData, String postDataMimeType) {
        return url (urlString, postData, postDataMimeType, () -> null);
    }

    static public BagArray url (String urlString, Bag postData, String postDataMimeType, Supplier<BagArray> fail) {
        try {
            var url = new URL (urlString);
            return url (url, postData, postDataMimeType, fail);
        } catch (MalformedURLException exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    static public BagArray url (URL url, Bag postData, String postDataMimeType) {
        return url (url, postData, postDataMimeType, () -> null);
    }

    static public BagArray url (URL url, Bag postData, String postDataMimeType, Supplier<BagArray> fail) {
        try {
            var sourceAdapter = new SourceAdapterHttp(url, postData, postDataMimeType);
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    static public BagArray sourceAdapter (SourceAdapter sourceAdapter) {
        return sourceAdapter (sourceAdapter, () -> null);
    }

    static public BagArray sourceAdapter (SourceAdapter sourceAdapter, Supplier<BagArray> fail) {
        try {
            return FormatReader.readBagArray (sourceAdapter);
        } catch (Exception exception) {
            log.error (exception);
        }
        return fail.get ();
    }

    @SafeVarargs
    static public <ArrayType> BagArray array (ArrayType... array) {
        var bagArray = new BagArray (array.length);
        for (var element : array) {
            bagArray.add (element);
        }
        return bagArray;
    }
}
