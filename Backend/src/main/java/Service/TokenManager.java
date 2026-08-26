package service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    private static TokenManager instance;
    private final Map<String, Session> tokens = new ConcurrentHashMap<String, Session>();
    private final ThreadLocal<Session> current = new ThreadLocal();

    private TokenManager() {
    }

    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public String createStaff(int staffId, String role) {
        String token = UUID.randomUUID().toString();
        this.tokens.put(token, new Session("STAFF", staffId, role));
        return token;
    }

    public String createPatient(int patientId) {
        String token = UUID.randomUUID().toString();
        this.tokens.put(token, new Session("PATIENT", patientId, "PATIENT"));
        return token;
    }

    public Session resolve(String token) {
        if (token == null) {
            return null;
        }
        return this.tokens.get(token);
    }

    public void remove(String token) {
        if (token != null) {
            this.tokens.remove(token);
        }
    }

    public void attach(Session session) {
        this.current.set(session);
    }

    public Session current() {
        return this.current.get();
    }

    public void detach() {
        this.current.remove();
    }

    public static class Session {
        public final String type;
        public final int id;
        public final String role;

        public Session(String type, int id, String role) {
            this.type = type;
            this.id = id;
            this.role = role;
        }

        public boolean isPatient() {
            return "PATIENT".equals(this.role);
        }
    }
}

