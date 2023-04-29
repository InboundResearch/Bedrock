#pragma once

#include "File.h"
#include "Log.h"
#include <curl/curl.h>

size_t writeHttpData (void* ptr, size_t size, size_t nmemb, void* stream);

// a web fetch class based on libcurl
// https://curl.haxx.se/libcurl/c/
class Http {
    private:
        static void initCurl () {
            static bool needsInit = true;
            if (needsInit) {
                curl_global_init(CURL_GLOBAL_DEFAULT);
                needsInit = false;
            }
        }

        static bool verbose;
        static bool sslVerifyPeer;
        static bool sslVerifyHost;

    public:
        static PtrToFile getToFile (const Text& url, const PtrToFile& file) {
            initCurl ();

            CURL* curlHandle = curl_easy_init();;
            if(curlHandle) {
                curl_easy_setopt (curlHandle, CURLOPT_URL, url.get ());

                // set up strict SSL handling with identity checks
                curl_easy_setopt (curlHandle, CURLOPT_SSL_VERIFYPEER, sslVerifyPeer);
                curl_easy_setopt (curlHandle, CURLOPT_SSL_VERIFYHOST, sslVerifyHost);

                // Switch on/off full protocol/debug output while testing
                curl_easy_setopt (curlHandle, CURLOPT_VERBOSE, verbose);

                /* open the file */
                FILE* httpDataFile = fopen (file->getPath (), "wb");
                if(httpDataFile) {
                    // write the page body to this file handle
                    curl_easy_setopt (curlHandle, CURLOPT_WRITEFUNCTION, writeHttpData);
                    curl_easy_setopt (curlHandle, CURLOPT_WRITEDATA, httpDataFile);

                    // perform the request, res will get the return code
                    CURLcode res = curl_easy_perform (curlHandle);

                    // close the file
                    fclose (httpDataFile);

                    // check the result
                    if(res != CURLE_OK) {
                        file->remove ();
                        throw RuntimeError (Text ("curl_easy_perform() failed: ") << curl_easy_strerror (res));
                    }
                }

                curl_easy_cleanup (curlHandle);
            }
            return file;
        }

        static PtrToBuffer get (const Text& url, const PtrToFile& file) {
            return getToFile (url, file)->read ();
        }

        static Text getText (const Text& url, const PtrToFile& file) {
            return getToFile (url, file)->readText ();
        }

        static void setStrictHttps (bool strict) {
            sslVerifyPeer = strict ? 1L : 0L;
            sslVerifyHost = strict ? 1L : 0L;
        }

        static void setVerbose (bool _verbose) {
            verbose = _verbose;
        }
};
