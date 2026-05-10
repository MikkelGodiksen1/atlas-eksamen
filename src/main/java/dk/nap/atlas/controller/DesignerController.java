package dk.nap.atlas.controller;

import dk.nap.atlas.model.Logo;
import dk.nap.atlas.model.Mockup;
import dk.nap.atlas.model.Product;
import dk.nap.atlas.model.ProductSpecification;
import dk.nap.atlas.repository.MockupRepository;
import dk.nap.atlas.repository.ProductRepository;
import dk.nap.atlas.repository.ProductSpecificationRepository;
import dk.nap.atlas.service.MockupService;
import dk.nap.atlas.service.PriceCalculationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Controller for kundens designer-flow (UC2 + UC3).
 * GRASP: Controller-pattern — tynd lag mellem HTTP og forretningslogik.
 */
@Controller
@RequestMapping("/designer")
public class DesignerController {

    private final MockupService mockupService;
    private final PriceCalculationService priceService;
    private final ProductRepository productRepository;
    private final ProductSpecificationRepository specRepository;
    private final MockupRepository mockupRepository;
    private final String uploadDir;

    public DesignerController(MockupService mockupService,
                              PriceCalculationService priceService,
                              ProductRepository productRepository,
                              ProductSpecificationRepository specRepository,
                              MockupRepository mockupRepository,
                              @Value("${atlas.upload-dir:./uploads}") String uploadDir) {
        this.mockupService = mockupService;
        this.priceService = priceService;
        this.productRepository = productRepository;
        this.specRepository = specRepository;
        this.mockupRepository = mockupRepository;
        this.uploadDir = uploadDir;
    }

    @GetMapping
    public String showDesigner(@RequestParam(required = false) Long logoId,
                               @RequestParam(required = false) Long productId,
                               @RequestParam(required = false) Long mockupId,
                               Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        model.addAttribute("logoId", logoId);
        model.addAttribute("selectedProductId", productId == null && !products.isEmpty() ? products.get(0).getId() : productId);
        model.addAttribute("mockupId", mockupId);

        Long pid = (Long) model.getAttribute("selectedProductId");
        if (pid != null) {
            List<ProductSpecification> specs = specRepository.findByProductId(pid);
            Map<String, List<ProductSpecification>> grouped = new HashMap<>();
            for (ProductSpecification s : specs) {
                grouped.computeIfAbsent(s.getCategory(), k -> new java.util.ArrayList<>()).add(s);
            }
            model.addAttribute("specsByCategory", grouped);
        }

        if (mockupId != null) {
            mockupRepository.findById(mockupId).ifPresent(m -> model.addAttribute("mockup", m));
        }
        return "designer";
    }

    @PostMapping("/upload-logo")
    public String uploadLogo(@RequestParam("logo") MultipartFile file,
                             RedirectAttributes redirect) throws IOException {
        try {
            Logo saved = mockupService.saveLogo(file);
            redirect.addAttribute("logoId", saved.getId());
            return "redirect:/designer";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("uploadError", ex.getMessage());
            return "redirect:/designer";
        }
    }

    @PostMapping("/generate-mockup")
    public String generateMockup(@RequestParam Long logoId,
                                 @RequestParam Long productId,
                                 @RequestParam(required = false) List<Long> specIds,
                                 RedirectAttributes redirect) throws IOException {
        Mockup m = mockupService.generateMockup(logoId, productId,
            specIds == null ? List.of() : specIds);
        redirect.addAttribute("logoId", logoId);
        redirect.addAttribute("productId", productId);
        redirect.addAttribute("mockupId", m.getId());
        return "redirect:/designer";
    }

    @GetMapping("/price")
    @ResponseBody
    public Map<String, Object> calculatePrice(@RequestParam Long productId,
                                              @RequestParam(required = false) List<Long> specIds,
                                              @RequestParam(defaultValue = "1") int quantity) {
        PriceCalculationService.PriceResult pr = priceService.calculate(productId,
            specIds == null ? List.of() : specIds, quantity);
        Map<String, Object> out = new HashMap<>();
        out.put("unitPrice", pr.getUnitPriceBeforeDiscount());
        out.put("unitPriceAfterDiscount", pr.getUnitPriceAfterDiscount());
        out.put("totalPrice", pr.getTotalPrice());
        out.put("tierFactor", pr.getTierFactor());
        return out;
    }

    @GetMapping("/mockup/{id}/image")
    public ResponseEntity<Resource> mockupImage(@PathVariable Long id) {
        Mockup m = mockupRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Mockup ikke fundet"));
        Path path = Paths.get(m.getFilePath());
        Resource res = new FileSystemResource(path);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(res);
    }

    @GetMapping("/mockup/{id}/download")
    public ResponseEntity<Resource> downloadMockup(@PathVariable Long id) {
        Mockup m = mockupRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Mockup ikke fundet"));
        Path path = Paths.get(m.getFilePath());
        Resource res = new FileSystemResource(path);
        ContentDisposition cd = ContentDisposition.attachment().filename("nap-mockup-" + id + ".png").build();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
            .contentType(MediaType.IMAGE_PNG)
            .body(res);
    }
}
