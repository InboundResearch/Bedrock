module us.irdev.bedrock.bag {
    requires jdk.unsupported;

    requires us.irdev.bedrock;
    requires us.irdev.bedrock.logger;

    exports us.irdev.bedrock.bag;
    exports us.irdev.bedrock.bag.entry;
    exports us.irdev.bedrock.bag.formats;
  exports us.irdev.bedrock.bag.scanner;
}
