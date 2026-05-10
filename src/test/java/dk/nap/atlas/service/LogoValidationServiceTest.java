package dk.nap.atlas.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * White-box unit tests for LogoValidationService.
 * Validerer NFR6, NFR7, BR2, BR3.
 */
class LogoValidationServiceTest {

    private final LogoValidationService service = new LogoValidationService();

    private byte[] pngHeader() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private byte[] jpegHeader() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    }

    @Test
    void acceptsValidPng() {
        var file = new MockMultipartFile("logo", "logo.png", "image/png", pngHeader());
        var result = service.validate(file);
        assertTrue(result.valid);
        assertEquals("logo.png", result.sanitizedFilename);
    }

    @Test
    void acceptsValidJpeg() {
        var file = new MockMultipartFile("logo", "photo.jpg", "image/jpeg", jpegHeader());
        var result = service.validate(file);
        assertTrue(result.valid);
    }

    @Test
    void acceptsValidSvg() {
        byte[] svg = "<?xml version=\"1.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".getBytes();
        var file = new MockMultipartFile("logo", "logo.svg", "image/svg+xml", svg);
        var result = service.validate(file);
        assertTrue(result.valid);
    }

    @Test
    void rejectsEmptyFile() {
        var file = new MockMultipartFile("logo", "logo.png", "image/png", new byte[0]);
        var result = service.validate(file);
        assertFalse(result.valid);
    }

    @Test
    void rejectsExeMimeType() {
        var file = new MockMultipartFile("logo", "evil.exe", "application/x-msdownload", pngHeader());
        var result = service.validate(file);
        assertFalse(result.valid);
        assertTrue(result.message.toLowerCase().contains("png"));
    }

    @Test
    void rejectsFakePngWithWrongHeader() {
        // MIME-type siger PNG men bytes er ikke PNG-magic
        byte[] bytes = "not a real png".getBytes();
        var file = new MockMultipartFile("logo", "fake.png", "image/png", bytes);
        var result = service.validate(file);
        assertFalse(result.valid);
        assertTrue(result.message.contains("ikke ud til at være et rigtigt billede"));
    }

    @Test
    void rejectsOversizedFile() {
        byte[] big = new byte[6 * 1024 * 1024];
        // sæt PNG-header så de første bytes ser gyldige ud
        System.arraycopy(pngHeader(), 0, big, 0, 8);
        var file = new MockMultipartFile("logo", "big.png", "image/png", big);
        var result = service.validate(file);
        assertFalse(result.valid);
        assertTrue(result.message.toLowerCase().contains("5 mb"));
    }

    @Test
    void sanitizesPathTraversalFilename() {
        String s = service.sanitizeFilename("../../etc/passwd");
        // Slashes erstattes med underscores; punktum og bindestreg er tilladt i filnavne
        assertEquals(".._.._etc_passwd", s);
        assertFalse(s.contains("/"));
    }

    @Test
    void sanitizesShellMetacharacters() {
        String s = service.sanitizeFilename("logo;rm-rf.png");
        assertEquals("logo_rm-rf.png", s);
    }

    @Test
    void truncatesVeryLongFilename() {
        String name = "a".repeat(200) + ".png";
        String s = service.sanitizeFilename(name);
        assertTrue(s.length() <= 100);
    }
}
