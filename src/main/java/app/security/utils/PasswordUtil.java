package app.security.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {}

    /** Hasher et password med BCrypt. Salt genereres automatisk. */
    public static String hash(String raw) {
        int cost = getCost();
        return BCrypt.hashpw(raw, BCrypt.gensalt(cost));
    }

    /** Verificerer om et råt password matcher en BCrypt-hash. */
    public static boolean verify(String raw, String bcryptHash) {
        if (raw == null || bcryptHash == null) return false;
        return BCrypt.checkpw(raw, bcryptHash);
    }

    /** Giver mulighed for at justere cost via miljøvariabel (default 12). */
    private static int getCost() {
        try {
            return Integer.parseInt(System.getenv().getOrDefault("BCRYPT_COST", "12"));
        } catch (Exception e) {
            return 12;
        }
    }
}
