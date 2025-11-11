package sigmacine.dominio.valueobject;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHash {
    private final String hash;

    public PasswordHash(String hash) {
        if (hash == null || hash.isBlank()) throw new IllegalArgumentException("hash vacío");
        this.hash = hash;
    }

    public String value(){ return hash; }

    public boolean matches(String plain) {
        if (plain == null) return false;
        boolean isBcrypt = hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
        if (isBcrypt) {
            try { return BCrypt.checkpw(plain, hash); }
            catch (IllegalArgumentException ex) { return false; }
        }
        return plain.equals(hash);
    }
}
