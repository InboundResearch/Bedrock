package us.irdev.bedrock.bag.scanner;

public enum XmlToken {
    ERROR,
    WHITESPACE,
    CONTENT,
    BEGIN_OPEN_ELEMENT,
    OPEN_ELEMENT_NAME,
    END_OPEN_ELEMENT,
    ATTRIBUTE_NAME,
    ATTRIBUTE_EQ,
    ATTRIBUTE_VALUE,
    OPEN_QUOTE,
    CLOSE_QUOTE,
    BEGIN_CLOSE_ELEMENT,
    CLOSE_ELEMENT_NAME,
    CLOSE_ELEMENT_ANONYMOUS,
    END_CLOSE_ELEMENT,
    EMPTY_ELEMENT,
    PROLOG,
    DECL,
    COMMENT
}
