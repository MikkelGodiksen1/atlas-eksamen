package dk.nap.atlas.service.ai;

import dk.nap.atlas.model.Logo;
import dk.nap.atlas.model.Product;
import dk.nap.atlas.model.ProductSpecification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * Fallback / lokal implementation der ikke kræver eksternt API.
 * Tager et eksempel-tote-bag-billede og kompositter logoet på det med Java AWT.
 * Aktiv når {@code spring.profiles.active=local}, eller som fallback hvis OpenAI fejler.
 */
@Component
public class StubImageGenerator implements ImageGenerator {

    private static final List<String> SAMPLE_BAGS = List.of(
        "/sample-bags/bag-white.png",
        "/sample-bags/bag-black.png",
        "/sample-bags/bag-navy.png"
    );

    private final Random random = new Random();
    private final String externalSamplesDir;

    public StubImageGenerator(@Value("${atlas.stub.samples-dir:}") String externalSamplesDir) {
        this.externalSamplesDir = externalSamplesDir;
    }

    @Override
    public GeneratedImage generate(Logo logo, Product product, List<ProductSpecification> specs) {
        try {
            BufferedImage bag = loadSampleBag(specs);
            BufferedImage logoImg = loadLogoImage(logo);
            BufferedImage result = composite(bag, logoImg);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(result, "png", baos);
            return new GeneratedImage(baos.toByteArray(), "image/png", "stub");
        } catch (IOException ex) {
            throw new RuntimeException("Kunne ikke generere stub-mockup: " + ex.getMessage(), ex);
        }
    }

    private BufferedImage loadSampleBag(List<ProductSpecification> specs) throws IOException {
        if (externalSamplesDir != null && !externalSamplesDir.isBlank()) {
            Path dir = Paths.get(externalSamplesDir);
            if (Files.isDirectory(dir)) {
                List<Path> files = Files.list(dir)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase();
                        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png");
                    })
                    .toList();
                if (!files.isEmpty()) {
                    Path pick = files.get(random.nextInt(files.size()));
                    return ImageIO.read(pick.toFile());
                }
            }
        }
        String pick = SAMPLE_BAGS.get(random.nextInt(SAMPLE_BAGS.size()));
        try (InputStream is = new ClassPathResource(pick).getInputStream()) {
            return ImageIO.read(is);
        } catch (IOException ignore) {
            return placeholderBag();
        }
    }

    private BufferedImage placeholderBag() {
        BufferedImage img = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, 800, 800);
        g.setColor(new Color(50, 50, 50));
        g.fillRect(200, 250, 400, 450);
        g.setColor(new Color(80, 80, 80));
        g.fillRect(280, 200, 60, 80);
        g.fillRect(460, 200, 60, 80);
        g.dispose();
        return img;
    }

    private BufferedImage loadLogoImage(Logo logo) throws IOException {
        if (logo == null || logo.getFilePath() == null) return null;
        Path p = Paths.get(logo.getFilePath());
        if (!Files.exists(p)) return null;
        if ("image/svg+xml".equalsIgnoreCase(logo.getMimeType())) {
            return null;
        }
        return ImageIO.read(p.toFile());
    }

    private BufferedImage composite(BufferedImage bag, BufferedImage logo) {
        BufferedImage out = new BufferedImage(bag.getWidth(), bag.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(bag, 0, 0, null);

        if (logo != null) {
            int targetWidth = bag.getWidth() / 3;
            double ratio = (double) logo.getHeight() / logo.getWidth();
            int targetHeight = (int) (targetWidth * ratio);
            int x = (bag.getWidth() - targetWidth) / 2;
            int y = (bag.getHeight() - targetHeight) / 2;
            g.drawImage(logo, x, y, targetWidth, targetHeight, null);
        }
        g.dispose();
        return out;
    }
}
