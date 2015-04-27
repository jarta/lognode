package com.jarta.ws;

import com.jarta.db.MongoHelper;
import com.jarta.ws.po.CommonMsg;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wei on 2015/4/15.
 */
@Path("daemon/publish")
public class JerseyService {

    static Logger logger = LoggerFactory.getLogger(JerseyService.class);

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
//        msg.setId(name.hashCode());
        msg.setContent(String.format("hello post %s", name));
        return msg;
    }

    @Path("{regionName}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public int publishMsg(@PathParam("regionName") String region, @FormParam("payload") String payload, @FormParam("tstamp") long tStamp) {
        int rtnCode = 200;
        try {
            logger.info("region={}, payload={}, tstamp={}", region, payload, tStamp);
            MongoCollection mc = MongoHelper.getInstance().getCollection(region);
//            BasicDBObject doc = (BasicDBObject) JSON.parse(payload);
            Document doc = Document.parse(payload);
            mc.insertOne(doc);
        }catch(Exception e) {
            logger.error("fail to process message", e);
            rtnCode = 400;
        }
        return rtnCode;
    }
}
