package Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TokenManager (Singleton)
 *
 * Acts as the server-side session registry. When a staff member logs in the
 * backend issues a random bearer token which is stored here keyed by staff id.
 * Every protected REST call is validated by {@code TokenAuthFilter} against this
 * registry, giving the system real session/cookie-style authentication instead of
 * a purely decorative token.
 */
public class TokenManager {
    private static TokenManager instance;

    // token -> staffId
    private final Map<String, Integer> tokens = new ConcurrentHashMap<>();

    private TokenManager() {}

    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    /** Issue a new token for the given staff member. */
    public String create(int staffId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, staffId);
        return token;
    }

    /** Validate a token. Returns the staff id or -1 if unknown/expired. */
    public int validate(String token) {
        if (token == null) return -1;
        Integer id = tokens.get(token);
        return id == null ? -1 : id;
    }

    /** Invalidate a token (used on logout). */
    public void remove(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
