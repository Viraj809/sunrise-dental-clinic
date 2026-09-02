package com.mycompany.backend.resources;

import model.Notice;
import dao.NoticeDAO;
import service.SecurityUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

@Path("/notices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoticeResource {
    private NoticeDAO dao = new NoticeDAO();

    @GET
    public Response getAll() {
        SecurityUtil.requireStaff();
        List<Notice> list = dao.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/my")
    public Response getMyNotices() {
        SecurityUtil.requireStaff();
        String role = SecurityUtil.currentRole();
        int staffId = SecurityUtil.currentId();
        List<Notice> list = dao.findPublishedForRole(role, staffId);
        return Response.ok(list).build();
    }

    @GET
    @Path("/unread-count")
    public Response getUnreadCount() {
        SecurityUtil.requireStaff();
        String role = SecurityUtil.currentRole();
        int staffId = SecurityUtil.currentId();
        List<Notice> notices = dao.findPublishedForRole(role, staffId);
        Map<String, Object> res = new HashMap<>();
        res.put("count", notices.size());
        return Response.ok(res).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") int id) {
        SecurityUtil.requireStaff();
        Notice notice = dao.findById(id);
        if (notice == null) return Response.status(404).entity(error("Notice not found")).build();
        return Response.ok(notice).build();
    }

    @POST
    public Response create(Notice notice) {
        SecurityUtil.requireAdmin();
        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            return Response.status(400).entity(error("Title is required")).build();
        }
        notice.setCreatedBy(SecurityUtil.currentId());
        if (dao.insert(notice)) {
            return Response.ok(notice).build();
        }
        return Response.status(500).entity(error("Failed to create notice")).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") int id, Notice notice) {
        SecurityUtil.requireAdmin();
        Notice existing = dao.findById(id);
        if (existing == null) return Response.status(404).entity(error("Notice not found")).build();
        notice.setNoticeId(id);
        if (dao.update(notice)) {
            return Response.ok(notice).build();
        }
        return Response.status(500).entity(error("Failed to update notice")).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        SecurityUtil.requireAdmin();
        Notice existing = dao.findById(id);
        if (existing == null) return Response.status(404).entity(error("Notice not found")).build();
        if (dao.delete(id)) {
            return Response.ok(success("Notice deleted")).build();
        }
        return Response.status(500).entity(error("Failed to delete notice")).build();
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
