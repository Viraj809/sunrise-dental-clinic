package com.mycompany.backend.resources;

import model.Notification;
import dao.NotificationDAO;
import service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    private final NotificationDAO dao = new NotificationDAO();

    /** Admin: all system notifications. */
    @GET
    public Response getAll() {
        SecurityUtil.requireAdmin();
        return Response.ok(dao.findAll()).build();
    }

    /** Current user's in-app notifications. */
    @GET
    @Path("/my")
    public Response getMine() {
        SecurityUtil.session();
        int userId = SecurityUtil.currentId();
        List<Notification> list = dao.findByUserId(userId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("notifications", list);
        res.put("unread", dao.countUnread(userId));
        return Response.ok(res).build();
    }

    @PUT
    @Path("/{id}/read")
    public Response markRead(@PathParam("id") int id) {
        SecurityUtil.session();
        // Admin may mark any; users may only mark their own.
        if (!"ADMIN".equals(SecurityUtil.currentRole())) {
            List<Notification> mine = dao.findByUserId(SecurityUtil.currentId());
            boolean owned = mine.stream().anyMatch(n -> n.getNotificationId() == id);
            if (!owned) return Response.status(403).entity(error("Not allowed")).build();
        }
        if (dao.markRead(id)) {
            return Response.ok(success("Marked as read")).build();
        }
        return Response.status(500).entity(error("Failed to update notification")).build();
    }

    /** Admin: push a notification to a user. */
    @POST
    public Response create(Map<String, String> body) {
        SecurityUtil.requireAdmin();
        String recipient = body.get("recipient");
        String message   = body.get("message");
        String title     = body.get("title");
        String type      = body.get("notification_type");
        if (message == null || message.isEmpty()) {
            return Response.status(400).entity(error("message is required")).build();
        }
        Notification n = new Notification();
        n.setUserId(body.get("user_id") != null ? Integer.parseInt(body.get("user_id")) : 0);
        n.setTitle(title != null ? title : "Clinic Notification");
        n.setChannel("IN_APP");
        n.setRecipient(recipient != null ? recipient : "");
        n.setNotificationType(type != null ? type : "GENERAL");
        n.setMessage(message);
        n.setRead(false);
        n.setStatus("SENT");
        if (dao.insert(n)) return Response.ok(success("Notification sent")).build();
        return Response.status(500).entity(error("Failed to send notification")).build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }

    private Map<String, String> success(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }
}
