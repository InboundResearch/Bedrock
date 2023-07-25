package us.irdev.bedrock.site;

import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.service.Base;
import us.irdev.bedrock.service.Event;
import us.irdev.bedrock.logger.*;

import java.util.regex.Pattern;

public class Service extends Base {
    private static final Logger log = LogManager.getLogger (Service.class);

    public static final String IP_ADDRESS = "ip-address";

    public Service () { }

    public void handleEventIpAddress (Event event) {
        event.ok (BagObject.open (IP_ADDRESS, event.getIpAddress ()));
    }

    public void handleEventHeaders (Event event) {
        var request = event.getRequest ();
        var responseBagObject = new BagObject ();
        var headerNames = request.getHeaderNames ();
        while (headerNames.hasMoreElements ()) {
            var headerName = (String) headerNames.nextElement ();
            var headerValue = escapeLine(request.getHeader (headerName));
            responseBagObject.put (headerName, headerValue);
        }
        event.ok (responseBagObject);
    }
}
