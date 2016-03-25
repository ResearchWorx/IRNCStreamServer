package edu.uky.irnc.streamserver.controllers;

import com.espertech.esper.client.EventBean;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/")
public class Stream {
    private static final Object latestLock = new Object();
    private static List<EventBean> latest = new ArrayList<EventBean>();

    @GET
    @Produces("application/json")
    public Response getLatest() {
        StringBuilder sb = new StringBuilder();
        synchronized (latestLock) {
            sb.append("[");
            for (int i = 0; i < latest.size() - 1; i++) {
                sb.append(latest.get(i).getUnderlying().toString());
                sb.append(",");
            }
            if (latest.size() > 1) {
                sb.append(latest.get(latest.size() - 1).getUnderlying().toString());
            }
            sb.append("]");
        }
        return Response.status(200).entity(sb.toString()).build();
    }

    public static void updateLatest(EventBean[] events) {
        synchronized (latestLock) {
            latest = new ArrayList<EventBean>();
            latest.addAll(Arrays.asList(events));
        }
    }
}
