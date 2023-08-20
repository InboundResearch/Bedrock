package us.irdev.bedrock.bag.scanner;

public class XmlScanner extends Scanner<XmlState, XmlToken> {
  private static final XmlToken NO_EMIT = null;
  private static final String WHITESPACE = " \t\n\r";

  public XmlScanner() {
    super(XmlState.START);

    try {
      addState (XmlState.ERROR)
              .onAnyInput(XmlState.ERROR, CAPTURE, NO_EMIT);

      addState (XmlState.START)
              .onAnyInput(XmlState.BODY, CAPTURE, NO_EMIT)
              .onInput('<', XmlState.BEGIN_OPEN_ELEMENT, CAPTURE, NO_EMIT);

      addState (XmlState.BODY)
              .onAnyInput(XmlState.BODY, CAPTURE, NO_EMIT)
              .onInput('<', XmlState.START, DONT_CAPTURE, XmlToken.BODY);

      addState (XmlState.BEGIN_OPEN_ELEMENT)
              .onAnyInput(XmlState.OPEN_ELEMENT, DONT_CAPTURE, XmlToken.BEGIN_OPEN_ELEMENT)
              .onInput('/', XmlState.CLOSE_ELEMENT, CAPTURE, XmlToken.BEGIN_CLOSE_ELEMENT)
              .onInput('?', XmlState.PROLOG, CAPTURE, NO_EMIT)
              .onInput('!', XmlState.DECL_START, CAPTURE, NO_EMIT);

      addState (XmlState.OPEN_ELEMENT)
              .onAnyInput(XmlState.OPEN_ELEMENT, CAPTURE, NO_EMIT)
              .onInput("/>", XmlState.END_OPEN_ELEMENT, DONT_CAPTURE, XmlToken.OPEN_ELEMENT_NAME)
              .onInput(WHITESPACE, XmlState.END_OPEN_ELEMENT, DONT_CAPTURE, XmlToken.OPEN_ELEMENT_NAME);

      addState (XmlState.END_OPEN_ELEMENT)
              .onAnyInput(XmlState.ATTRIBUTE_NAME, CAPTURE, NO_EMIT)
              .onInput('/', XmlState.EMPTY_ELEMENT, CAPTURE, NO_EMIT)
              .onInput('>', XmlState.START, CAPTURE, XmlToken.END_OPEN_ELEMENT)
              .onInput(WHITESPACE, XmlState.END_OPEN_ELEMENT, CAPTURE, XmlToken.WHITESPACE);

      addState (XmlState.CLOSE_ELEMENT)
              .onAnyInput(XmlState.CLOSE_ELEMENT, CAPTURE, NO_EMIT)
              .onInput('>', XmlState.END_CLOSE_ELEMENT, DONT_CAPTURE, XmlToken.CLOSE_ELEMENT_NAME);

      addState (XmlState.END_CLOSE_ELEMENT)
              .onAnyInput(XmlState.ERROR, CAPTURE, XmlToken.ERROR)
              .onInput('>', XmlState.START, CAPTURE, XmlToken.END_CLOSE_ELEMENT);

      addState (XmlState.DECL_START)
              .onAnyInput(XmlState.DECL_BODY, DONT_CAPTURE, NO_EMIT)
              .onInput('-', XmlState.COMMENT_2, CAPTURE, NO_EMIT);

      addState (XmlState.DECL_BODY)
              .onAnyInput(XmlState.DECL_BODY, CAPTURE, NO_EMIT)
              .onInput('>', XmlState.START, CAPTURE, XmlToken.DECL);

      addState (XmlState.COMMENT_2)
              .onAnyInput(XmlState.ERROR, CAPTURE, XmlToken.ERROR)
              .onInput('-', XmlState.COMMENT_BODY, CAPTURE, NO_EMIT);

      addState (XmlState.COMMENT_BODY)
              .onAnyInput(XmlState.COMMENT_BODY, CAPTURE, NO_EMIT)
              .onInput('-', XmlState.COMMENT_4, CAPTURE, NO_EMIT);

      addState (XmlState.COMMENT_4)
              .onAnyInput(XmlState.COMMENT_BODY, CAPTURE, NO_EMIT)
              .onInput('-', XmlState.COMMENT_5, CAPTURE, NO_EMIT);

      addState (XmlState.COMMENT_5)
              .onAnyInput(XmlState.COMMENT_BODY, CAPTURE, NO_EMIT)
              .onInput('>', XmlState.START, CAPTURE, XmlToken.COMMENT);

      addState (XmlState.PROLOG)
              .onAnyInput(XmlState.PROLOG, CAPTURE, NO_EMIT)
              .onInput('?', XmlState.END_PROLOG, CAPTURE, NO_EMIT);

      addState (XmlState.END_PROLOG)
              .onAnyInput(XmlState.PROLOG, CAPTURE, NO_EMIT)
              .onInput('>', XmlState.START, CAPTURE, XmlToken.PROLOG);

      addState (XmlState.EMPTY_ELEMENT)
              .onAnyInput(XmlState.ERROR, CAPTURE, XmlToken.ERROR)
              .onInput('>', XmlState.START, CAPTURE, XmlToken.EMPTY_ELEMENT);

      addState (XmlState.ATTRIBUTE_NAME)
              .onAnyInput(XmlState.ATTRIBUTE_NAME, CAPTURE, NO_EMIT)
              .onInput('>', XmlState.END_OPEN_ELEMENT, DONT_CAPTURE, XmlToken.ATTRIBUTE_NAME)
              .onInput(WHITESPACE + "=", XmlState.ATTRIBUTE_EQ, DONT_CAPTURE, XmlToken.ATTRIBUTE_NAME);

      addState (XmlState.ATTRIBUTE_EQ)
              .onAnyInput(XmlState.ERROR, CAPTURE, XmlToken.ERROR)
              .onInput('=', XmlState.ATTRIBUTE_EQ, CAPTURE, NO_EMIT)
              .onInput('\'', XmlState.ATTRIBUTE_OPEN_SINGLE_QUOTE, DONT_CAPTURE, XmlToken.ATTRIBUTE_EQ)
              .onInput('"', XmlState.ATTRIBUTE_OPEN_DOUBLE_QUOTE, DONT_CAPTURE, XmlToken.ATTRIBUTE_EQ)
              .onInput(WHITESPACE, XmlState.ATTRIBUTE_EQ, CAPTURE, NO_EMIT);

      addState (XmlState.ATTRIBUTE_OPEN_SINGLE_QUOTE)
              .onAnyInput(XmlState.ATTRIBUTE_SINGLE_QUOTE_BODY, CAPTURE, XmlToken.OPEN_QUOTE);

      addState (XmlState.ATTRIBUTE_SINGLE_QUOTE_BODY)
              .onAnyInput(XmlState.ATTRIBUTE_SINGLE_QUOTE_BODY, CAPTURE, NO_EMIT)
              .onInput('\'', XmlState.ATTRIBUTE_CLOSE_SINGLE_QUOTE, DONT_CAPTURE, XmlToken.ATTRIBUTE_VALUE);

      addState (XmlState.ATTRIBUTE_CLOSE_SINGLE_QUOTE)
              .onAnyInput(XmlState.END_OPEN_ELEMENT, CAPTURE, XmlToken.CLOSE_QUOTE);

      addState (XmlState.ATTRIBUTE_OPEN_DOUBLE_QUOTE)
              .onAnyInput(XmlState.ATTRIBUTE_DOUBLE_QUOTE_BODY, CAPTURE, XmlToken.OPEN_QUOTE);

      addState (XmlState.ATTRIBUTE_DOUBLE_QUOTE_BODY)
              .onAnyInput(XmlState.ATTRIBUTE_DOUBLE_QUOTE_BODY, CAPTURE, NO_EMIT)
              .onInput('"', XmlState.ATTRIBUTE_CLOSE_DOUBLE_QUOTE, DONT_CAPTURE, XmlToken.ATTRIBUTE_VALUE);

      addState (XmlState.ATTRIBUTE_CLOSE_DOUBLE_QUOTE)
              .onAnyInput(XmlState.END_OPEN_ELEMENT, CAPTURE, XmlToken.CLOSE_QUOTE);

    } catch (Exception exc) {
      // XXX need to handle duplicate input and duplicate state exceptions - though handling these
      // XXX would be most important when designing the language parser and not during normal use
    }
  }
}
