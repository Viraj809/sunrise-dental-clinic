package com.mycompany.backend.resources;

import Model.Patient;
import DAO.PatientDAO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * PatientAuthResource – public, self-service patient registration.
 * This endpoint is explicitly exempted from token authentication by
 * TokenAuthFilter (path "auth/patient/register") so new patients can sign up.
 */
@Path("/auth/patient")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientAuthResource {

    private final PatientDAO dao = new PatientDAO();

    @POST
    @Path("/register")
    public Response register(Map<String, String> body) {
        String name    = body.get("name");
        String email   = body.get("email");
        String contact = body.get("contact");
        String nic     = body.get("nic");
        String password = body.get("password");
        String gender  = body.get("gender");
        String address = body.get("address");
        String dob     = body.get("date_of_birth");

        if (name == null || email == null || contact == null || nic == null || password == null) {
            return Response.status(400).entity(error("name, email, contact, NIC and password are required")).build();
        }
        if (!contact.matches("^0\\d{9}$")) {
            return Response.status(400).entity(error("Contact number must be 10 digits and start with 0")).build();
        }
        if (dao.findByEmail(email) != null) {
            return Response.status(400).entity(error("A patient with this email already exists")).build();
        }
        if (dao.findByNic(nic) != null) {
            return Response.status(400).entity(error("A patient with this NIC already exists")).build();
        }

        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());

        Patient p = new Patient();
        p.setName(name);
        p.setEmail(email);
        p.setContact(contact);
        p.setNic(nic);
        p.setGender(gender != null ? gender : "");
        p.setAddress(address != null ? address : "");
        p.setDateOfBirth(dob != null ? dob : "");
        p.setPasswordHash(hash);
        p.setActive(true);

        if (dao.insert(p)) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Registration successful. You can now log in.");
            res.put("email", email);
            return Response.ok(res).build();
        }
        return Response.status(500).entity(error("Failed to register patient")).build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }
}
