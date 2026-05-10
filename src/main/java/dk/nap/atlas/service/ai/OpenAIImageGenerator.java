package dk.nap.atlas.service.ai;

import dk.nap.atlas.model.Logo;
import dk.nap.atlas.model.Product;
import dk.nap.atlas.model.ProductSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Production-implementation: kalder OpenAI image-edit API med logoet
 * og en prompt baseret på produktet og specifikationerne.
 */
@Component
@Profile("prod")
@Primary
public class OpenAIImageGenerator implements ImageGenerator {

    private static final Logger log = LoggerFactory.getLogger(OpenAIImageGenerator.class);

    private final RestClient client;
    private final String apiKey;
    private final String model;
    private final StubImageGenerator fallback;

    public OpenAIImageGenerator(@Value("${atlas.openai.api-key:}") String apiKey,
                                @Value("${atlas.openai.model:gpt-image-1}") String model,
                                StubImageGenerator fallback) {
        this.apiKey = apiKey;
        this.model = model;
        this.fallback = fallback;
        this.client = RestClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();
    }

    @Override
    public GeneratedImage generate(Logo logo, Product product, List<ProductSpecification> specs) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key mangler — falder tilbage til StubImageGenerator");
            return fallback.generate(logo, product, specs);
        }
        try {
            String prompt = buildPrompt(product, specs);
            byte[] logoBytes = Files.readAllBytes(Paths.get(logo.getFilePath()));

            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("model", model);
            form.add("prompt", prompt);
            form.add("size", "1024x1024");
            form.add("image", new ByteArrayResource(logoBytes) {
                @Override public String getFilename() { return logo.getFilename(); }
            });

            Map response = client.post()
                .uri("/images/edits")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve()
                .body(Map.class);

            if (response == null) throw new RuntimeException("Tom response fra OpenAI");
            List data = (List) response.get("data");
            if (data == null || data.isEmpty()) throw new RuntimeException("Ingen billeder i response");
            Map first = (Map) data.get(0);
            String b64 = (String) first.get("b64_json");
            if (b64 == null) throw new RuntimeException("Mangler b64_json i response");

            return new GeneratedImage(Base64.getDecoder().decode(b64), "image/png", "openai");
        } catch (Exception ex) {
            log.error("OpenAI-kald fejlede, falder tilbage til stub: {}", ex.getMessage());
            return fallback.generate(logo, product, specs);
        }
    }

    private String buildPrompt(Product product, List<ProductSpecification> specs) {
        String specStr = specs.stream()
            .map(s -> s.getCategory() + ":" + s.getValue())
            .collect(Collectors.joining(", "));
        return "Et professionelt produktfoto af en " + product.getName().toLowerCase()
            + " (" + product.getDescription() + ") med følgende specifikationer: "
            + specStr + ". Logoet skal placeres centreret på posen som tryk. "
            + "Ren hvid baggrund, naturligt lys, høj kvalitet, e-commerce stil.";
    }
}
