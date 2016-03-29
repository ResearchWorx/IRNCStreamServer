package edu.uky.irnc.streamserver.controllers;

import com.espertech.esper.client.EventBean;
import edu.uky.irnc.streamserver.sresource.ESPERNetFlow;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Path("/")
public class Stream {
    private static HashMap<String, List<EventBean> > latest = new HashMap<String, List<EventBean>>();

    @GET
    @Path("/results/{exchange}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getLatest(@PathParam("exchange") String exchange) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        synchronized (latest) {
            List<EventBean> currLatest = latest.get(exchange);
            if (currLatest != null) {
                for (int i = 0; i < currLatest.size() - 1; i++) {
                    sb.append(currLatest.get(i).getUnderlying().toString());
                    sb.append(",");
                }
                if (currLatest.size() > 1) {
                    sb.append(currLatest.get(currLatest.size() - 1).getUnderlying().toString());
                }
            }
        }
        sb.append("]");
        return Response.ok(sb.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response exchangeList() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int currIndex = 0;
        for (String key : latest.keySet()) {
            sb.append("\"");
            sb.append(key);
            sb.append("\"");
            if (++currIndex < latest.keySet().size()) {
                sb.append(",");
            }
        }
        sb.append("]");
        return Response.ok(sb.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/limit")
    @Produces(MediaType.TEXT_PLAIN)
    public Response emptyLimit() {
        return Response.status(500).entity("Please enter a valid integer for new limit").header("Access-Control-Allow-Origin", "*").build();
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
        ESPERNetFlow.updateAllQuery(limit);
        return Response.ok(limit).header("Access-Control-Allow-Origin", "*").build();
    }

    public static void updateLatest(String  exchange, EventBean[] events) {
        synchronized (latest) {
            latest.put(exchange, new ArrayList<EventBean>());
            latest.get(exchange).addAll(Arrays.asList(events));
        }
    }
}
