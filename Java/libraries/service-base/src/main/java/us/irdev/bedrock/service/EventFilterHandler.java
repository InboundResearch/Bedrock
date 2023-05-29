package us.irdev.bedrock.service;

import us.irdev.bedrock.bag.BagObject;

public interface EventFilterHandler {
    boolean isAllowedEvent (Event event, BagObject filterConfiguration);
}
