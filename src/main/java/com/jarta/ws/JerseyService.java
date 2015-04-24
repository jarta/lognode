package com.jarta.ws;

import com.jarta.ws.po.CommonMsg;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wei on 2015/4/15.
 */
@Path("my_resource")
public class JerseyService {

    @Path("{username}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CommonMsg getIt(@PathParam("username") String name) {
        CommonMsg msg = new CommonMsg();
        msg.setId(7);
        msg.setPath(name);
        msg.setContent("good to go");
        return msg;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CommonMsg getParam(@QueryParam("username") String name) {
        CommonMsg msg = new CommonMsg();
        msg.setId(7);
        msg.setPath(name);
        msg.setContent("getParam good to go");
        return msg;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public CommonMsg hello(@FormParam("test") String name) {
        CommonMsg msg = new CommonMsg();
        msg.setId(name.hashCode());
        msg.setContent(String.format("hello post %s", name));
        return msg;
    }
}
