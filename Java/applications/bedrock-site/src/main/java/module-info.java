module us.irdev.bedrock.site {
    requires jakarta.servlet;

    requires us.irdev.bedrock.bag;
    requires us.irdev.bedrock.logger;
    requires us.irdev.bedrock.secret;
    requires us.irdev.bedrock.service;

    opens us.irdev.bedrock.site to us.irdev.bedrock.service;
}
