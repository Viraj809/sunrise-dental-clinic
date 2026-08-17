package com.mycompany.backend.resources;

import Service.TokenManager;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * TokenAuthFilter (ContainerRequestFilter)
 *
 * Enforces authentication on every protected JAX-RS endpoint. It validates the
 * {@code Authorization: Bearer <token>} header against the server-side
 * {@link TokenManager} registry. The public login endpoint (/auth/login) and
 * CORS preflight (OPTIONS) requests are exempt.
 *
 * On success the resolved staff id is attached to the request properties so
 * downstream resources could use it if needed.
 */
@Provider
public class TokenAuthFilter implements ContainerRequestFilter {

    private static final String AUTH_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();

        // Allow login and CORS preflight without a token.
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())
                || path.contains("auth/login")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith(AUTH_PREFIX)) {
            abort(requestContext, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(AUTH_PREFIX.length()).trim();
        int staffId = TokenManager.getInstance().validate(token);
        if (staffId < 0) {
            abort(requestContext, "Invalid or expired session. Please log in again.");
            return;
        }

        requestContext.setProperty("staffId", staffId);
    }

    private void abort(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"" + message + "\"}")
                        .build());
    }
}
