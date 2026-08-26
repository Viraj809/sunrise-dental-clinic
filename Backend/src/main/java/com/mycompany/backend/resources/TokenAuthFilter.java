package com.mycompany.backend.resources;

import service.TokenManager;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * TokenAuthFilter (ContainerRequestFilter)
 *
 * Enforces authentication on every protected JAX-RS endpoint. It validates the
 * {@code Authorization: Bearer <token>} header against the server-side
 * {@link TokenManager} registry, attaches the resolved {@link TokenManager.Session}
 * to the request thread (for {@link service.SecurityUtil} RBAC checks), and
 * applies a small set of coarse path-based guards as defence-in-depth. Fine
 * grained, resource-level authorization is still performed inside each resource.
 *
 * The public login endpoint (/auth/login) and CORS preflight (OPTIONS) requests
 * are exempt from authentication.
 */
@Provider
public class TokenAuthFilter implements ContainerRequestFilter {

    private static final String AUTH_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        // Allow login and CORS preflight without a token.
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())
                || path.contains("auth/login")
                || path.contains("auth/patient/register")) {
            return;
        }

        // Allow public health check or static resources if needed
        if (path.startsWith("public") || path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".jpg")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith(AUTH_PREFIX)) {
            abort(requestContext, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(AUTH_PREFIX.length()).trim();
        TokenManager.Session session = TokenManager.getInstance().resolve(token);
        if (session == null) {
            abort(requestContext, "Invalid or expired session. Please log in again.");
            return;
        }

        TokenManager.getInstance().attach(session);

        // Coarse, path-based defence-in-depth (resources also enforce precisely).
        if (!coarseAllowed(path, requestContext.getMethod(), session.role)) {
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\":\"You are not authorized to access this resource\"}")
                            .build());
        }
    }

    private boolean coarseAllowed(String path, String method, String role) {
        // Patient self-service endpoints â€“ only the patient themselves.
        if (path.startsWith("patients/me") && !"PATIENT".equals(role)) return false;
        if (path.startsWith("patient/") && !"PATIENT".equals(role)) return false;

        boolean isAdmin = "ADMIN".equals(role) || "SYSTEM_ADMIN".equals(role);

        // Admin-only management surfaces.
        if (path.startsWith("staff") && !isAdmin) return false;
        if (path.startsWith("audit") && !isAdmin) return false;
        if (path.startsWith("settings") && !isAdmin) return false;

        // Receptionist / admin only operational surfaces.
        if (path.startsWith("queue") && !(isAdmin || "RECEPTIONIST".equals(role))) return false;

        // Treatment catalogue writes are admin-only (reads are allowed to all).
        if (path.startsWith("treatments") && !"GET".equalsIgnoreCase(method) && !isAdmin) {
            return false;
        }

        // Dentist data is only reachable by staff/patient; admins/receptionists may view.
        return true;
    }

    private void abort(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"" + message + "\"}")
                        .build());
    }
}
