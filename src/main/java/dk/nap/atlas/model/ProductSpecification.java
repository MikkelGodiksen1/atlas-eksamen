package dk.nap.atlas.model;

import java.math.BigDecimal;

public class ProductSpecification {
    private Long id;
    private Long productId;
    private String category;
    private String value;
    private BigDecimal priceFactor;

    public ProductSpecification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public BigDecimal getPriceFactor() { return priceFactor; }
    public void setPriceFactor(BigDecimal priceFactor) { this.priceFactor = priceFactor; }
}
