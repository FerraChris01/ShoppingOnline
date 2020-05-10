/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.DatabaseConnector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * REST Web Service
 *
 * @author Chris
 */
@Path("/user")
public class UserResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of UserResource
     */
    public UserResource() {
    }

    /**
     * Retrieves representation of an instance of war.UserResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(@QueryParam("email")String email, @QueryParam("password")String password) {
        try {
            if (email == null || email.isEmpty() || password == null || password.isEmpty())
                throw new WebApplicationException(Response.Status.BAD_REQUEST);  
                
            ResultSet rs = DatabaseConnector.getIstance().query("SELECT * FROM utenti WHERE email='" + email + "' AND password='" + DigestUtils.md5Hex(password) + "'");
            if (!rs.next()) 
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);          
            
            String generatedToken = "sadasdasfw34fa";
            
            Object cookie = new Object() {
                public String token = generatedToken;
                
            };
            
            return Response
                .status(Response.Status.OK)
                .entity(new ObjectMapper().writeValueAsString(cookie))
                .build();
            
       } catch (SQLException | JsonProcessingException ex) {
           throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR); 
        } 
    }
}