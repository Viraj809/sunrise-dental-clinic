package Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TokenManager (Singleton)
 *
 * Acts as the server-side session registry. When a user (staff or patient) logs
 * in the backend issues a random bearer token which is stored here together with
 * the principal's type, id and role. Every protected REST call is validated by
 * {@code TokenAuthFilter} against this registry, giving the system real
 * role-aware session authentication (not a decorative token).
 *
 * The resolved {@link Session} for the current request is also exposed through a
 * ThreadLocal so business code (resources) can enforce Role-Based Access Control
 * via {@link SecurityUtil}.
 */
public class TokenManager {
    private static TokenManager instance;

    // token -> Session
    private final Map<String, Session> tokens = new ConcurrentHashMap<>();
    // current request session (set by the auth filter, cleared by the response filter)
    private final ThreadLocal<Session> current = new ThreadLocal<>();

    private TokenManager() {}

    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    /** Authenticated principal attached to a token. */
    public static class Session {
        public final String type; // "STAFF" or "PATIENT"
        public final int id;       // staffId or patientId
        public final String role;  // ADMIN | RECEPTIONIST | DENTIST | PATIENT

        public Session(String type, int id, String role) {
            this.type = type;
            this.id = id;
            this.role = role;
        }

        public boolean isPatient() {
            return "PATIENT".equals(role);
        }
    }

    /** Issue a token for a staff member (admin / receptionist / dentist). */
    public String createStaff(int staffId, String role) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new Session("STAFF", staffId, role));
        return token;
    }

    /** Issue a token for a patient (self-service portal). */
    public String createPatient(int patientId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new Session("PATIENT", patientId, "PATIENT"));
        return token;
    }

    /** Validate a token and return its session, or null if unknown/expired. */
    public Session resolve(String token) {
        if (token == null) return null;
        return tokens.get(token);
    }

    /** Invalidate a token (used on logout). */
    public void remove(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    // ── ThreadLocal request session helpers (used by SecurityUtil) ──────────
    public void attach(Session session) {
        current.set(session);
    }

    public Session current() {
        return current.get();
    }

    public void detach() {
        current.remove();
    }
}
