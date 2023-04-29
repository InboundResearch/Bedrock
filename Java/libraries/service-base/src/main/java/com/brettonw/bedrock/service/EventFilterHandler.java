package us.irdev.bedrock.service;

import us.irdev.bedrock.bag.BagObject;

public interface EventFilterHandler {
    public boolean isAllowedEvent (Event event, BagObject filterConfiguration);
}
