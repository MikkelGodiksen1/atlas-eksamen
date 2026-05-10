package dk.nap.atlas.service;

import dk.nap.atlas.model.Product;
import dk.nap.atlas.model.ProductSpecification;
import dk.nap.atlas.repository.ProductRepository;
import dk.nap.atlas.repository.ProductSpecificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * White-box unit tests skrevet TDD-style i Sprint 3.
 * Coverage-fokus: forretningsregel BR4 (volume-tier) og fejl-cases.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PriceCalculationServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductSpecificationRepository specRepository;
    @InjectMocks private PriceCalculationService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setBasePrice(new BigDecimal("10.00"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    }

    @Test
    void calculate_underTier_noDiscount() {
        when(specRepository.findAllByIds(List.of())).thenReturn(List.of());
        var result = service.calculate(1L, List.of(), 50);
        assertEquals(new BigDecimal("10.00"), result.getUnitPriceBeforeDiscount());
        assertEquals(new BigDecimal("10.00"), result.getUnitPriceAfterDiscount());
        assertEquals(new BigDecimal("500.00"), result.getTotalPrice());
        assertEquals(new BigDecimal("1"), result.getTierFactor().stripTrailingZeros());
    }

    @Test
    void calculate_atTier100_appliesDiscount() {
        when(specRepository.findAllByIds(List.of())).thenReturn(List.of());
        var result = service.calculate(1L, List.of(), 100);
        assertEquals(new BigDecimal("9.20"), result.getUnitPriceAfterDiscount());
        assertEquals(new BigDecimal("920.00"), result.getTotalPrice());
        assertEquals(new BigDecimal("0.92"), result.getTierFactor());
    }

    @Test
    void calculate_atTier250_appliesDiscount() {
        when(specRepository.findAllByIds(List.of())).thenReturn(List.of());
        var result = service.calculate(1L, List.of(), 250);
        assertEquals(new BigDecimal("8.50"), result.getUnitPriceAfterDiscount());
        assertEquals(new BigDecimal("0.85"), result.getTierFactor());
    }

    @Test
    void calculate_atTier500_appliesDiscount() {
        when(specRepository.findAllByIds(List.of())).thenReturn(List.of());
        var result = service.calculate(1L, List.of(), 500);
        assertEquals(new BigDecimal("0.78"), result.getTierFactor());
    }

    @Test
    void calculate_atTier1000_appliesDeepestDiscount() {
        when(specRepository.findAllByIds(List.of())).thenReturn(List.of());
        var result = service.calculate(1L, List.of(), 1000);
        assertEquals(new BigDecimal("0.70"), result.getTierFactor());
        assertEquals(new BigDecimal("7000.00"), result.getTotalPrice());
    }

    @Test
    void calculate_withMultipleSpecs_multipliesFactors() {
        ProductSpecification s1 = new ProductSpecification();
        s1.setPriceFactor(new BigDecimal("1.300"));
        ProductSpecification s2 = new ProductSpecification();
        s2.setPriceFactor(new BigDecimal("1.150"));
        when(specRepository.findAllByIds(List.of(2L, 5L))).thenReturn(List.of(s1, s2));

        var result = service.calculate(1L, List.of(2L, 5L), 1);
        // 10.00 * 1.300 * 1.150 = 14.95
        assertEquals(new BigDecimal("14.95"), result.getUnitPriceBeforeDiscount());
    }

    @Test
    void calculate_quantityZero_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> service.calculate(1L, List.of(), 0));
    }

    @Test
    void calculate_unknownProduct_throws() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class,
            () -> service.calculate(999L, List.of(), 1));
    }
}
