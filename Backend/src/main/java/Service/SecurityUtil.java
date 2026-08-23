package Service;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;

/**
 * SecurityUtil – centralises Role-Based Access Control for JAX-RS resources.
 *
 * Resources call these helpers at the top of protected methods. They rely on the
 * {@link TokenManager.Session} that {@code TokenAuthFilter} attached to the
 * current request thread, throwing Jakarta REST exceptions that Jersey maps to
 * 401 (unauthenticated) / 403 (forbidden) responses.
 */
public final class SecurityUtil {

    private SecurityUtil() {}

    /** Return the current request Session or throw 401 if missing. */
    public static TokenManager.Session session() {
        TokenManager.Session s = TokenManager.getInstance().current();
        if (s == null) {
            throw new NotAuthorizedException("Missing or invalid session");
        }
        return s;
    }

    /** Throw 403 unless the current role is one of the allowed roles. */
    public static void requireRole(String... roles) {
        String role = session().role;
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new ForbiddenException("You are not authorized to perform this action");
    }

    public static void requireAdmin() {
        requireRole("ADMIN", "SYSTEM_ADMIN");
    }

    public static void requireStaff() {
        requireRole("ADMIN", "RECEPTIONIST", "DENTIST", "SYSTEM_ADMIN");
    }

    public static void requireReceptionOrAdmin() {
        requireRole("ADMIN", "RECEPTIONIST", "SYSTEM_ADMIN");
    }

    public static void requirePatient() {
        requireRole("PATIENT");
    }

    public static boolean isSystemAdmin() {
        return "SYSTEM_ADMIN".equals(session().role);
    }

    public static boolean isAdmin() {
        String role = session().role;
        return "ADMIN".equals(role) || "SYSTEM_ADMIN".equals(role);
    }

    public static int currentId() {
        return session().id;
    }

    public static String currentRole() {
        return session().role;
    }

    public static boolean isPatient() {
        return "PATIENT".equals(session().role);
    }
}
