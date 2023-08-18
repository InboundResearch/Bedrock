package us.irdev.bedrock.bag.scanner;

public class XmlScanner extends Scanner {
  private static final boolean CAPTURE = true;
  private static final boolean DONT_CAPTURE = false;

  private static final String DEFAULT_STATE = DEFAULT_START_STATE_NAME;
  private static final String BODY_STATE = "body";
  private static final String BEGIN_OPEN_ELEMENT_STATE = "begin-open-element";
  private static final String OPEN_ELEMENT_STATE = "open-element";
  private static final String END_OPEN_ELEMENT_STATE = "end-open-element";
  private static final String CLOSE_ELEMENT_STATE = "close-element";
  private static final String END_CLOSE_ELEMENT_STATE = "end-close-element";
  private static final String EMPTY_ELEMENT_STATE = "empty-element";
  private static final String ATTRIBUTE_NAME_STATE = "attribute-name";
  private static final String ATTRIBUTE_EQ_STATE = "attribute-eq";
  private static final String ATTRIBUTE_OPEN_SQUOTE_STATE = "attribute-open-single-quote";
  private static final String ATTRIBUTE_SQUOTE_BODY = "attribute-single-quote-body";
  private static final String ATTRIBUTE_CLOSE_SQUOTE_STATE = "attribute-close-single-quote";
  private static final String ATTRIBUTE_OPEN_DQUOTE_STATE = "attribute-open-double-quote";
  private static final String ATTRIBUTE_DQUOTE_BODY = "attribute-double-quote-body";
  private static final String ATTRIBUTE_CLOSE_DQUOTE_STATE = "attribute-close-double-quote";

  // special elements take the form <? ... ?> - do we care?
  private static final String PROLOG_STATE = "prolog";
  private static final String END_PROLOG_STATE = "end-prolog";

  // from DECL state (<!), we recognize -- as the beginning of a comment and
  // --> as the end. this is important as comments may be used to hide other xml
  // from the parser. The remainder of decl types are simply scanned for the
  // close bracket
  private static final String DECL_START_STATE = "decl-start";
  private static final String DECL_BODY_STATE = "decl-body";
  private static final String COMMENT_STATE_2 = "comment-2";
  private static final String COMMENT_BODY_STATE = "comment-body";
  private static final String COMMENT_STATE_4 = "comment-4";
  private static final String COMMENT_STATE_5 = "comment-5";

  private static final String NO_EMIT = null;
  private static final String EMIT_WHITESPACE = "whitespace";
  private static final String EMIT_BODY = "body";
  private static final String EMIT_BEGIN_OPEN_ELEMENT = "begin-open-element";
  private static final String EMIT_OPEN_ELEMENT = "open-element";
  private static final String EMIT_END_OPEN_ELEMENT = "end-open-element";
  private static final String EMIT_BEGIN_CLOSE_ELEMENT = "begin-close-element";
  private static final String EMIT_CLOSE_ELEMENT = "close-element";
  private static final String EMIT_END_CLOSE_ELEMENT = "end-close-element";
  private static final String EMIT_PROLOG = "prolog";
  private static final String EMIT_DECL = "decl";
  private static final String EMIT_COMMENT = "comment";
  private static final String EMIT_EMPTY_ELEMENT = "empty-element";
  private static final String EMIT_ATTRIBUTE_NAME = "attribute-name";
  private static final String EMIT_ATTRIBUTE_EQ = "attribute-eq";
  private static final String EMIT_ATTRIBUTE_VALUE = "attribute-value";
  private static final String EMIT_OPEN_QUOTE = "open-quote";
  private static final String EMIT_CLOSE_QUOTE = "close-quote";

  private static final String WHITESPACE = " \t\n\r";

  public XmlScanner() {
    super();

    try {
      addState (ERROR_STATE)
              .onAnyInput(ERROR_STATE, CAPTURE, NO_EMIT);

      addState (DEFAULT_STATE)
              .onAnyInput(BODY_STATE, CAPTURE, NO_EMIT)
              .onInput('<', BEGIN_OPEN_ELEMENT_STATE, CAPTURE, NO_EMIT);

      addState (BODY_STATE)
              .onAnyInput(BODY_STATE, CAPTURE, NO_EMIT)
              .onInput('<', DEFAULT_STATE, DONT_CAPTURE, EMIT_BODY);

      addState (BEGIN_OPEN_ELEMENT_STATE)
              .onAnyInput(OPEN_ELEMENT_STATE, DONT_CAPTURE, EMIT_BEGIN_OPEN_ELEMENT)
              .onInput('/', CLOSE_ELEMENT_STATE, CAPTURE, EMIT_BEGIN_CLOSE_ELEMENT)
              .onInput('?', PROLOG_STATE, CAPTURE, NO_EMIT)
              .onInput('!', DECL_START_STATE, CAPTURE, NO_EMIT);

      addState (OPEN_ELEMENT_STATE)
              .onAnyInput(OPEN_ELEMENT_STATE, CAPTURE, NO_EMIT)
              .onInput("/>", END_OPEN_ELEMENT_STATE, DONT_CAPTURE, EMIT_OPEN_ELEMENT)
              .onInput(WHITESPACE, END_OPEN_ELEMENT_STATE, DONT_CAPTURE, EMIT_OPEN_ELEMENT);

      addState (END_OPEN_ELEMENT_STATE)
              .onAnyInput(ATTRIBUTE_NAME_STATE, CAPTURE, NO_EMIT)
              .onInput('/', EMPTY_ELEMENT_STATE, CAPTURE, NO_EMIT)
              .onInput('>', DEFAULT_STATE, CAPTURE, EMIT_END_OPEN_ELEMENT)
              .onInput(WHITESPACE, END_OPEN_ELEMENT_STATE, CAPTURE, EMIT_WHITESPACE);

      addState (CLOSE_ELEMENT_STATE)
              .onAnyInput(CLOSE_ELEMENT_STATE, CAPTURE, NO_EMIT)
              .onInput('>', END_CLOSE_ELEMENT_STATE, DONT_CAPTURE, EMIT_CLOSE_ELEMENT);

      addState (END_CLOSE_ELEMENT_STATE)
              .onAnyInput(ERROR_STATE, CAPTURE, EMIT_ERROR)
              .onInput('>', DEFAULT_STATE, CAPTURE, EMIT_END_CLOSE_ELEMENT);

      addState (DECL_START_STATE)
              .onAnyInput(DECL_BODY_STATE, DONT_CAPTURE, NO_EMIT)
              .onInput('-', COMMENT_STATE_2, CAPTURE, NO_EMIT);

      addState (DECL_BODY_STATE)
              .onAnyInput(DECL_BODY_STATE, CAPTURE, NO_EMIT)
              .onInput('>', DEFAULT_STATE, CAPTURE, EMIT_DECL);

      addState (COMMENT_STATE_2)
              .onAnyInput(ERROR_STATE, CAPTURE, EMIT_ERROR)
              .onInput('-', COMMENT_BODY_STATE, CAPTURE, NO_EMIT);

      addState (COMMENT_BODY_STATE)
              .onAnyInput(COMMENT_BODY_STATE, CAPTURE, NO_EMIT)
              .onInput('-', COMMENT_STATE_4, CAPTURE, NO_EMIT);

      addState (COMMENT_STATE_4)
              .onAnyInput(COMMENT_BODY_STATE, CAPTURE, NO_EMIT)
              .onInput('-', COMMENT_STATE_5, CAPTURE, NO_EMIT);

      addState (COMMENT_STATE_5)
              .onAnyInput(COMMENT_BODY_STATE, CAPTURE, NO_EMIT)
              .onInput('>', DEFAULT_STATE, CAPTURE, EMIT_COMMENT);

      addState (PROLOG_STATE)
              .onAnyInput(PROLOG_STATE, CAPTURE, NO_EMIT)
              .onInput('?', END_PROLOG_STATE, CAPTURE, NO_EMIT);

      addState (END_PROLOG_STATE)
              .onAnyInput(PROLOG_STATE, CAPTURE, NO_EMIT)
              .onInput('>', DEFAULT_STATE, CAPTURE, EMIT_PROLOG);

      addState (EMPTY_ELEMENT_STATE)
              .onAnyInput(ERROR_STATE, CAPTURE, EMIT_ERROR)
              .onInput('>', DEFAULT_STATE, CAPTURE, EMIT_EMPTY_ELEMENT);

      addState (ATTRIBUTE_NAME_STATE)
              .onAnyInput(ATTRIBUTE_NAME_STATE, CAPTURE, NO_EMIT)
              .onInput('>', END_OPEN_ELEMENT_STATE, DONT_CAPTURE, EMIT_ATTRIBUTE_NAME)
              .onInput(WHITESPACE + "=", ATTRIBUTE_EQ_STATE, DONT_CAPTURE, EMIT_ATTRIBUTE_NAME);

      addState (ATTRIBUTE_EQ_STATE)
              .onAnyInput(ERROR_STATE, CAPTURE, EMIT_ERROR)
              .onInput('=', ATTRIBUTE_EQ_STATE, CAPTURE, NO_EMIT)
              .onInput('\'', ATTRIBUTE_OPEN_SQUOTE_STATE, DONT_CAPTURE, EMIT_ATTRIBUTE_EQ)
              .onInput('"', ATTRIBUTE_OPEN_DQUOTE_STATE, DONT_CAPTURE, EMIT_ATTRIBUTE_EQ)
              .onInput(WHITESPACE, ATTRIBUTE_EQ_STATE, CAPTURE, NO_EMIT);

      addState (ATTRIBUTE_OPEN_SQUOTE_STATE)
              .onAnyInput(ATTRIBUTE_SQUOTE_BODY, CAPTURE, EMIT_OPEN_QUOTE);

      addState (ATTRIBUTE_SQUOTE_BODY)
              .onAnyInput(ATTRIBUTE_SQUOTE_BODY, CAPTURE, NO_EMIT)
              .onInput('\'', ATTRIBUTE_CLOSE_SQUOTE_STATE, DONT_CAPTURE, EMIT_ATTRIBUTE_VALUE);

      addState (ATTRIBUTE_CLOSE_SQUOTE_STATE)
              .onAnyInput(END_OPEN_ELEMENT_STATE, CAPTURE, EMIT_CLOSE_QUOTE);

      addState (ATTRIBUTE_OPEN_DQUOTE_STATE)
              .onAnyInput(ATTRIBUTE_DQUOTE_BODY, CAPTURE, EMIT_OPEN_QUOTE);

      addState (ATTRIBUTE_DQUOTE_BODY)
              .onAnyInput(ATTRIBUTE_DQUOTE_BODY, CAPTURE, NO_EMIT)
              .onInput('"', ATTRIBUTE_CLOSE_DQUOTE_STATE, DONT_CAPTURE, EMIT_ATTRIBUTE_VALUE);

      addState (ATTRIBUTE_CLOSE_DQUOTE_STATE)
              .onAnyInput(END_OPEN_ELEMENT_STATE, CAPTURE, EMIT_CLOSE_QUOTE);

    } catch (Exception exc) {
      // XXX need to handle duplicate input and duplicate state exceptions - though handling these
      // XXX would be most important when designing the language parser and not during normal use
    }
  }
}
