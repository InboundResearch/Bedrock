#include "Http.h"

size_t writeHttpData (void* ptr, size_t size, size_t nmemb, void* stream) {
  size_t written = fwrite (ptr, size, nmemb, (FILE*) stream);
  return written;
}

bool Http::verbose = 0L;
bool Http::sslVerifyPeer = 1L;
bool Http::sslVerifyHost = 1L;
