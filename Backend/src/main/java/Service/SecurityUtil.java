package service;

import service.TokenManager;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;

public final class SecurityUtil {
    private SecurityUtil() {
    }

    public static TokenManager.Session session() {
        TokenManager.Session s = TokenManager.getInstance().current();
        if (s == null) {
            throw new NotAuthorizedException((Object)"Missing or invalid session", new Object[0]);
        }
        return s;
    }

    public static void requireRole(String ... roles) {
        String role = SecurityUtil.session().role;
        for (String r : roles) {
            if (!r.equals(role)) continue;
            return;
        }
        throw new ForbiddenException("You are not authorized to perform this action");
    }

    public static void requireAdmin() {
        SecurityUtil.requireRole("ADMIN", "SYSTEM_ADMIN");
    }

    public static void requireStaff() {
        SecurityUtil.requireRole("ADMIN", "RECEPTIONIST", "DENTIST", "SYSTEM_ADMIN");
    }

    public static void requireReceptionOrAdmin() {
        SecurityUtil.requireRole("ADMIN", "RECEPTIONIST", "SYSTEM_ADMIN");
    }

    public static void requirePatient() {
        SecurityUtil.requireRole("PATIENT");
    }

    public static boolean isSystemAdmin() {
        return "SYSTEM_ADMIN".equals(SecurityUtil.session().role);
    }

    public static boolean isAdmin() {
        String role = SecurityUtil.session().role;
        return "ADMIN".equals(role) || "SYSTEM_ADMIN".equals(role);
    }

    public static int currentId() {
        return SecurityUtil.session().id;
    }

    public static String currentRole() {
        return SecurityUtil.session().role;
    }

    public static boolean isPatient() {
        return "PATIENT".equals(SecurityUtil.session().role);
    }
}

