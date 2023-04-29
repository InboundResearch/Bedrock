#include "Test.h"
#include "XmlNode.h"

TEST_CASE(TestXmlNode) {
    PtrToXmlNode xmlNode = (new XmlNode ("xml"))
                    ->setAttribute("src", "https://bedrock.brettonw.com")
                    ->setAttribute("name", "test-xml")
                    ->addChild((new XmlNode ("abc"))
                                    ->setAttribute ("test", "true")
                                    )
                    ->addChild (new XmlNode ("abc", "content"))
                    ->addChild ((new XmlNode ("abc"))
                                    ->setContent("content2")
                                    ->addChild (new XmlNode ("def"))
                                    );
    TEST_EQUALS(xmlNode->toXml (), "<xml name=\"test-xml\" src=\"https://bedrock.brettonw.com\"><abc test=\"true\"/><abc>content</abc><abc><def/>content2</abc></xml>");
}
