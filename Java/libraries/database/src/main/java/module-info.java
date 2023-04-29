module us.irdev.bedrock.database {
    requires us.irdev.bedrock.bag;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires us.irdev.bedrock.logger;
    exports us.irdev.bedrock.database;
}
