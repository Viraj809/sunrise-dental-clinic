package com.mycompany.backend.resources;

import Service.TokenManager;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * PostAuthFilter (ContainerResponseFilter)
 *
 * Clears the per-request {@link TokenManager.Session} ThreadLocal after the
 * response is built so it cannot leak into a subsequent request handled by the
 * same pooled worker thread.
 */
@Provider
public class PostAuthFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        TokenManager.getInstance().detach();
    }
}
