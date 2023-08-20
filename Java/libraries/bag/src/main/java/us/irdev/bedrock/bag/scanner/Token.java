package us.irdev.bedrock.bag.scanner;

public record Token<EmitTokenType>(EmitTokenType emitToken, String value) {}
