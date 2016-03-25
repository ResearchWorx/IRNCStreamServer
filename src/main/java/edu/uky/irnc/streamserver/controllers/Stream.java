package edu.uky.irnc.streamserver.controllers;

import com.espertech.esper.client.EventBean;
import edu.uky.irnc.streamserver.sresource.ESPERNetFlow;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/")
public class Stream {
    private static final Object latestLock = new Object();
    private static List<EventBean> latest = new ArrayList<EventBean>();

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
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
        return Response.ok(sb.toString()).build();
    }

    public static void updateLatest(EventBean[] events) {
        synchronized (latestLock) {
            latest = new ArrayList<EventBean>();
            latest.addAll(Arrays.asList(events));
        }
    }

    @GET
    @Path("/limit")
    @Produces(MediaType.TEXT_PLAIN)
    public Response emptyLimit() {
        return Response.status(500).entity("Please enter a valid integer for new limit").build();
    }

    @GET
    @Path("/limit/{limit}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setLimit(@PathParam("limit") String limit) {
        if (limit.equals(""))
            return Response.status(500).entity("Please enter a valid integer for new limit").build();
        try {
            int limitInt = Integer.parseInt(limit);
            if (limitInt > 1000) {
                return Response.status(500).entity("Currently the hard limit is 1000, please enter a value equal to or less than 1000.").build();
            }
        } catch (NumberFormatException e) {
            return Response.status(500).entity(limit + " is not a number").build();
        }
        ESPERNetFlow.updateQuery(limit);
        return Response.ok(limit).build();
    }
}
