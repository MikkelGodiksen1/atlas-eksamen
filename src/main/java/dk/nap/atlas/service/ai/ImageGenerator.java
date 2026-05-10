package dk.nap.atlas.service.ai;

import dk.nap.atlas.model.Logo;
import dk.nap.atlas.model.Product;
import dk.nap.atlas.model.ProductSpecification;

import java.util.List;

/**
 * Strategy-mønster: udskifteligt interface for billede-generering.
 * Implementationer:
 *  - {@link OpenAIImageGenerator} til prod
 *  - {@link StubImageGenerator} til lokal udvikling og fallback
 */
public interface ImageGenerator {
    GeneratedImage generate(Logo logo, Product product, List<ProductSpecification> specs);
}
