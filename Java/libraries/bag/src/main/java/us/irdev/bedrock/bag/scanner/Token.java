package us.irdev.bedrock.bag.scanner;

public record Token(String currentStateName, String action, String value, String nextStateName) {}
