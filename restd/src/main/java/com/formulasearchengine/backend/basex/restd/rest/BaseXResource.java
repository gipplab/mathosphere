package com.formulasearchengine.backend.basex.restd.rest;

import com.formulasearchengine.backend.basex.Client;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

@Component @RestxResource
public class BaseXResource {

    /**
     * Say hello to anybody.
     *
     * Does not require authentication.
     *
     * @return a Message to say hello
     */
    @GET("/startup")
    @PermitAll
    public String query(String query) {
        Client client = new Client();
        return client.execute( query ) ;
    }
}
