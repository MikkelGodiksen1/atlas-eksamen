package dk.nap.atlas.service;

import dk.nap.atlas.model.Logo;
import dk.nap.atlas.model.Mockup;
import dk.nap.atlas.model.Product;
import dk.nap.atlas.model.ProductSpecification;
import dk.nap.atlas.repository.LogoRepository;
import dk.nap.atlas.repository.MockupRepository;
import dk.nap.atlas.repository.ProductRepository;
import dk.nap.atlas.repository.ProductSpecificationRepository;
import dk.nap.atlas.service.ai.GeneratedImage;
import dk.nap.atlas.service.ai.ImageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Facade over hele mockup-genereringsflowet.
 * - Validerer logo via {@link LogoValidationService}
 * - Persisterer logo til filsystem og DB
 * - Kalder {@link ImageGenerator} (Strategy)
 * - Persisterer mockup til filsystem og DB
 *
 * GRASP: Creator (opretter Mockup-objekter), Low Coupling (afhænger af interface ikke konkret AI).
 */
@Service
public class MockupService {

    private static final Logger log = LoggerFactory.getLogger(MockupService.class);

    private final LogoValidationService logoValidator;
    private final ImageGenerator imageGenerator;
    private final LogoRepository logoRepository;
    private final MockupRepository mockupRepository;
    private final ProductRepository productRepository;
    private final ProductSpecificationRepository specRepository;
    private final Path uploadDir;

    public MockupService(LogoValidationService logoValidator,
                         ImageGenerator imageGenerator,
                         LogoRepository logoRepository,
                         MockupRepository mockupRepository,
                         ProductRepository productRepository,
                         ProductSpecificationRepository specRepository,
                         @Value("${atlas.upload-dir:./uploads}") String uploadDir) {
        this.logoValidator = logoValidator;
        this.imageGenerator = imageGenerator;
        this.logoRepository = logoRepository;
        this.mockupRepository = mockupRepository;
        this.productRepository = productRepository;
        this.specRepository = specRepository;
        this.uploadDir = Paths.get(uploadDir);
    }

    public Logo saveLogo(MultipartFile file) throws IOException {
        LogoValidationService.ValidationResult vr = logoValidator.validate(file);
        if (!vr.valid) {
            throw new IllegalArgumentException(vr.message);
        }
        Path logosDir = uploadDir.resolve("logos");
        Files.createDirectories(logosDir);

        String ext = extensionFor(file.getContentType());
        String storedName = UUID.randomUUID() + ext;
        Path target = logosDir.resolve(storedName);
        Files.write(target, file.getBytes(), StandardOpenOption.CREATE_NEW);

        Logo logo = new Logo(vr.sanitizedFilename, target.toAbsolutePath().toString(),
            file.getSize(), file.getContentType());
        return logoRepository.save(logo);
    }

    public Mockup generateMockup(long logoId, long productId, List<Long> specIds) throws IOException {
        Logo logo = logoRepository.findById(logoId)
            .orElseThrow(() -> new NoSuchElementException("Logo ikke fundet"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("Produkt ikke fundet"));
        List<ProductSpecification> specs = specRepository.findAllByIds(specIds);

        log.info("Genererer mockup for product={} logo={} specs={}", productId, logoId, specIds);
        GeneratedImage img = imageGenerator.generate(logo, product, specs);

        Path mockupsDir = uploadDir.resolve("mockups");
        Files.createDirectories(mockupsDir);
        String storedName = UUID.randomUUID() + ".png";
        Path target = mockupsDir.resolve(storedName);
        Files.write(target, img.getBytes(), StandardOpenOption.CREATE_NEW);

        String specJson = specs.stream()
            .map(s -> "\"" + s.getCategory() + "\":\"" + s.getValue() + "\"")
            .collect(Collectors.joining(",", "{", "}"));

        Mockup m = new Mockup();
        m.setLogoId(logoId);
        m.setProductId(productId);
        m.setFilePath(target.toAbsolutePath().toString());
        m.setSelectedSpecifications(specJson);
        return mockupRepository.save(m);
    }

    private String extensionFor(String mime) {
        if (mime == null) return ".bin";
        return switch (mime.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/svg+xml" -> ".svg";
            default -> ".bin";
        };
    }
}
