module us.irdev.bedrock.site {
    requires us.irdev.bedrock.bag;
    requires us.irdev.bedrock.service;
    requires us.irdev.bedrock.secret;
    requires java.servlet;
    requires us.irdev.bedrock.logger;
    opens us.irdev.bedrock.site to us.irdev.bedrock.service;
}
