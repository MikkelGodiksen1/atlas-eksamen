package dk.nap.atlas.service;

import dk.nap.atlas.model.Product;
import dk.nap.atlas.model.ProductSpecification;
import dk.nap.atlas.repository.ProductRepository;
import dk.nap.atlas.repository.ProductSpecificationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Beregner pris ud fra produkt, valgte specifikationer, antal og volume-rabat.
 * GRASP: Information Expert — har dataet (Product, ProductSpecification) og ansvaret.
 * Forretningsregel BR4 implementeret som tier-tabel.
 *
 * Skrevet TDD-style i Sprint 3 — se PriceCalculationServiceTest.
 */
@Service
public class PriceCalculationService {

    private final ProductRepository productRepository;
    private final ProductSpecificationRepository specRepository;

    public PriceCalculationService(ProductRepository productRepository,
                                   ProductSpecificationRepository specRepository) {
        this.productRepository = productRepository;
        this.specRepository = specRepository;
    }

    /**
     * Beregner priser. Inputs valideres og hentes fra DB.
     * @param productId   produkt der skal pris-beregnes
     * @param specIds     valgte specifikationers IDs (kan være tom)
     * @param quantity    antal stk (skal være ≥ 1)
     */
    public PriceResult calculate(long productId, List<Long> specIds, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Antal skal være mindst 1");

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("Produkt ikke fundet: " + productId));

        BigDecimal unitPrice = product.getBasePrice();
        List<ProductSpecification> specs = specRepository.findAllByIds(specIds);
        for (ProductSpecification s : specs) {
            unitPrice = unitPrice.multiply(s.getPriceFactor());
        }
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);

        BigDecimal tier = determineTierFactor(quantity);
        BigDecimal unitAfterDiscount = unitPrice.multiply(tier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = unitAfterDiscount.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

        return new PriceResult(unitPrice, unitAfterDiscount, total, tier);
    }

    /**
     * Volume-rabat-tiers fra forretningsregel BR4.
     * 0-99 stk: ingen rabat. 100-249: 8 % rabat. 250-499: 15 %. 500-999: 22 %. 1000+: 30 %.
     */
    BigDecimal determineTierFactor(int quantity) {
        if (quantity >= 1000) return new BigDecimal("0.70");
        if (quantity >= 500)  return new BigDecimal("0.78");
        if (quantity >= 250)  return new BigDecimal("0.85");
        if (quantity >= 100)  return new BigDecimal("0.92");
        return BigDecimal.ONE;
    }

    /** Resultatobjekt fra prisberegning — DTO-stil. */
    public static class PriceResult {
        private final BigDecimal unitPriceBeforeDiscount;
        private final BigDecimal unitPriceAfterDiscount;
        private final BigDecimal totalPrice;
        private final BigDecimal tierFactor;

        public PriceResult(BigDecimal before, BigDecimal after, BigDecimal total, BigDecimal tier) {
            this.unitPriceBeforeDiscount = before;
            this.unitPriceAfterDiscount = after;
            this.totalPrice = total;
            this.tierFactor = tier;
        }

        public BigDecimal getUnitPriceBeforeDiscount() { return unitPriceBeforeDiscount; }
        public BigDecimal getUnitPriceAfterDiscount() { return unitPriceAfterDiscount; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public BigDecimal getTierFactor() { return tierFactor; }
    }
}
