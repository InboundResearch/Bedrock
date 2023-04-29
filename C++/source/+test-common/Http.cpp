#include "Test.h"
#include "Http.h"

TEST_MODULE_DEPENDENCIES (Http, "File")

TEST_CASE(HttpGet) {
    Text result = Http::getText ("https://bedrock.brettonw.com/api?event=ok", new File ("result.json"));
    TEST_NOT_EQUALS(result.find ("\"query\":{\"event\":\"ok\"}"), -1);
}

