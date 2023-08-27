package us.irdev.bedrock.bag.scanner;

public enum XmlState {
    START,
    ERROR,
    CONTENT,
    BEGIN_OPEN_ELEMENT,
    OPEN_ELEMENT,
    OPEN_ELEMENT_NAME,
    END_OPEN_ELEMENT,
    CLOSE_ELEMENT,
    CLOSE_ELEMENT_NAME,
    END_CLOSE_ELEMENT,
    EMPTY_ELEMENT,
    ATTRIBUTE_NAME,
    ATTRIBUTE_EQ,
    END_ATTRIBUTE_EQ,
    ATTRIBUTE_SINGLE_QUOTE_BODY,
    ATTRIBUTE_CLOSE_SINGLE_QUOTE,
    ATTRIBUTE_DOUBLE_QUOTE_BODY,
    ATTRIBUTE_CLOSE_DOUBLE_QUOTE,

    // prolog elements take the form <? ... ?> - do we care?
    PROLOG,
    END_PROLOG,

    // from DECL state (<!), we recognize -- as the beginning of a comment and
    // --> as the end. this is important as comments may be used to hide other xml
    // from the parser. The remainder of decl types are simply scanned for the
    // close bracket
    DECL_START,
    DECL_BODY,
    COMMENT_2,
    COMMENT_BODY,
    COMMENT_4,
    COMMENT_5
}
