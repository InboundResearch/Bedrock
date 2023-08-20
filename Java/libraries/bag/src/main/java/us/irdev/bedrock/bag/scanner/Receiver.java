package us.irdev.bedrock.bag.scanner;

public interface Receiver<EmitTokenType> {
    void handleToken (Token<EmitTokenType> token);
}
