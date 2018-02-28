package com.kumuluz.ee.testing.arquillian.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("test")
public class SimpleResource {

    @GET
    public Response test() {
        return Response.ok("hello").build();
    }
}
