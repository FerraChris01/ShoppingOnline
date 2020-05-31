/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resources;

import authentication.JwtAuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.PreparedStatement;
import utils.DatabaseConnector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.*;
import javax.ws.rs.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.*;
import resources.entities.Address;


/**
 * REST Web Service
 *
 * @author Chris
 */
@Path("user")
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
    public Response login(@QueryParam("email")String email, @QueryParam("password")String password) {
        try {
            if (email == null || email.isEmpty() || password == null || password.isEmpty())
                throw new WebApplicationException(Response.Status.BAD_REQUEST);  
            
            if (!DatabaseConnector.getIstance().isConnected())
                throw new WebApplicationException("failed to connect to db", 500);
                
            Statement st = DatabaseConnector.getIstance().getConnection(true).createStatement();
            String sql = "SELECT * FROM users WHERE email='" + email + "' AND password='" + DigestUtils.md5Hex(password) + "'";
            
            ResultSet rs = st.executeQuery(sql);
            if (!rs.next()) 
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);          
            
            return authenticate(email, rs.getInt("ID"), rs.getBoolean("vendor"));
            
       } catch (SQLException ex) {
           throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR); 
        } 
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response signUp(String jsonBody) {
        if (!DatabaseConnector.getIstance().isConnected()) 
            throw new WebApplicationException("failed to connect to db", 500);
        
        try {
            
            JSONObject obj = new JSONObject(jsonBody);
            
            String name = obj.getString("name");
            String surname = obj.getString("surname");
            String birthDate = obj.getString("birthDate");
            String email = obj.getString("email");
            String password = obj.getString("password");
            Integer IdMainAddress = null;
            
            JSONObject mainAddressJson = obj.has("mainAddress") ? obj.getJSONObject("mainAddress") : null;
            JSONArray secondaryAddressesJson = obj.has("secondaryAddresses") ? obj.getJSONArray("secondaryAddresses") : null;      
            
            Statement st = DatabaseConnector.getIstance().getConnection(false).createStatement();
            
            if (mainAddressJson != null || secondaryAddressesJson != null) {
                try {
                    st.executeQuery("INSERT INTO addresses (addressee, phone, country, province, city, street, number, zipCode) VALUES (" +                        
                        new Address(mainAddressJson.getString("addressee"), mainAddressJson.getString("phone"), mainAddressJson.getString("country"), 
                    mainAddressJson.getString("province"), mainAddressJson.getString("city"), mainAddressJson.getString("street"), mainAddressJson.getString("number"),
                    mainAddressJson.getString("zipCode")).toSQL() + ")");
                    IdMainAddress = st.getGeneratedKeys().getInt("ID");
                    
                    for (Object o : secondaryAddressesJson) {
                        JSONObject address = (JSONObject)o;
                        st.executeQuery(new Address(address.getString("addressee"), address.getString("phone"), address.getString("country"), 
                            address.getString("province"), address.getString("city"), address.getString("street"), address.getString("number"),
                            address.getString("zipCode")).toSQL() + ")");
                    }
                } catch (JSONException ex) {
                    throw new WebApplicationException(Response.Status.BAD_REQUEST); 
                }            
            }
            
            if (name == null || name.isEmpty() || surname == null || surname.isEmpty() || birthDate == null || birthDate.isEmpty() ||
                    email == null || email.isEmpty() || password == null || password.isEmpty())
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            
            String sql = "INSERT INTO users (name, surname, birthDate, email, password) "
                    + "VALUES ('" + name + "', '" + surname + "', '" + birthDate + "', '" + email + "', '" + password + "')";
            
            st.executeQuery(sql);
            ResultSet rs = st.getGeneratedKeys();
            
            int userID = 0;
            if (rs.next()) 
                userID = rs.getInt("ID");
                    
            if (!IdProfilePic.equals(""))
                stmt.executeQuery("UPDATE users SET IdProfilePic = " + IdProfilePic + " WHERE ID = " + userID);
            
            if (IdMainAddress != null)
                st.executeQuery("UPDATE users SET IdMainAddress = " + IdMainAddress + " WHERE ID = " + userID);         
            
            return authenticate(email, userID, false);           
            
        } catch (SQLException | JSONException ex) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);  
        } 
    }
    private Response authenticate(String email, int id, boolean vendor) {
        return Response
                .status(Response.Status.OK)
                .cookie(new NewCookie(
                        "panDiStelle",
                        JwtAuthenticationService.getInstance().generateToken(email, id, vendor, context),
                        null, // the URI path for which the cookie is valid
                        null, // the host domain for which the cookie is valid. TODO: should probably set this
                        NewCookie.DEFAULT_VERSION, // the version of the specification to which the cookie complies
                        null, // the comment
                        // No max-age and expiry set, cookie expires when the browser gets closed
                        NewCookie.DEFAULT_MAX_AGE, // the maximum age of the cookie in seconds
                        null, // the cookie expiry date
                        false, // specifies whether the cookie will only be sent over a secure connection
                        true // if {@code true} make the cookie HTTP only
                
                ))
                .build();
    }
    
}
