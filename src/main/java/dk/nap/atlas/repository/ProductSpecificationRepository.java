package dk.nap.atlas.repository;

import dk.nap.atlas.model.ProductSpecification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductSpecificationRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<ProductSpecification> MAPPER = (rs, n) -> {
        ProductSpecification s = new ProductSpecification();
        s.setId(rs.getLong("id"));
        s.setProductId(rs.getLong("product_id"));
        s.setCategory(rs.getString("category"));
        s.setValue(rs.getString("spec_value"));
        s.setPriceFactor(rs.getBigDecimal("price_factor"));
        return s;
    };

    public ProductSpecificationRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<ProductSpecification> findByProductId(long productId) {
        return jdbc.query("SELECT * FROM product_specification WHERE product_id = ? ORDER BY category, id", MAPPER, productId);
    }

    public List<ProductSpecification> findAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        String placeholders = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        return jdbc.query("SELECT * FROM product_specification WHERE id IN (" + placeholders + ")",
            MAPPER, ids.toArray());
    }
}
