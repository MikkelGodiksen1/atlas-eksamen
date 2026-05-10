package dk.nap.atlas.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * Validerer uploadede logo-filer.
 * Implementerer NFR6, NFR7, BR2, BR3, US23.
 * Whitelist + magic-bytes + filnavn-sanitering = forsvar mod scripted upload-angreb.
 */
@Service
public class LogoValidationService {

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_MIME = Set.of("image/png", "image/jpeg", "image/svg+xml");

    public ValidationResult validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.fail("Ingen fil modtaget.");
        }
        if (file.getSize() > MAX_BYTES) {
            return ValidationResult.fail("Logo skal være under 5 MB. Modtaget: " + (file.getSize() / 1024) + " KB.");
        }
        String mime = file.getContentType();
        if (mime == null || !ALLOWED_MIME.contains(mime.toLowerCase())) {
            return ValidationResult.fail("Logo skal være PNG, JPG eller SVG.");
        }
        try {
            byte[] head = file.getBytes();
            if (head.length < 4) {
                return ValidationResult.fail("Filen er for lille til at være et gyldigt billede.");
            }
            if (!matchesMagicBytes(head, mime.toLowerCase())) {
                return ValidationResult.fail("Filen ser ikke ud til at være et rigtigt billede. Forsøg ikke at omdøbe en anden filtype.");
            }
        } catch (IOException ex) {
            return ValidationResult.fail("Kunne ikke læse filen.");
        }
        return ValidationResult.ok(sanitizeFilename(file.getOriginalFilename()));
    }

    private boolean matchesMagicBytes(byte[] head, String mime) {
        if ("image/png".equals(mime)) {
            return head[0] == (byte) 0x89 && head[1] == 0x50 && head[2] == 0x4E && head[3] == 0x47;
        }
        if ("image/jpeg".equals(mime)) {
            return head[0] == (byte) 0xFF && head[1] == (byte) 0xD8 && head[2] == (byte) 0xFF;
        }
        if ("image/svg+xml".equals(mime)) {
            String s = new String(head, 0, Math.min(head.length, 200)).trim().toLowerCase();
            return s.startsWith("<?xml") || s.startsWith("<svg");
        }
        return false;
    }

    String sanitizeFilename(String original) {
        if (original == null) return "logo";
        String base = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (base.length() > 100) base = base.substring(0, 100);
        if (base.isBlank()) return "logo";
        return base;
    }

    public static class ValidationResult {
        public final boolean valid;
        public final String message;
        public final String sanitizedFilename;

        private ValidationResult(boolean valid, String message, String sanitizedFilename) {
            this.valid = valid;
            this.message = message;
            this.sanitizedFilename = sanitizedFilename;
        }

        public static ValidationResult ok(String sanitizedFilename) {
            return new ValidationResult(true, null, sanitizedFilename);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message, null);
        }
    }
}
