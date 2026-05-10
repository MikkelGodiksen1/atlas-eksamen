package dk.nap.atlas.repository;

import dk.nap.atlas.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Product> MAPPER = (rs, n) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setType(rs.getString("type"));
        p.setBasePrice(rs.getBigDecimal("base_price"));
        p.setDescription(rs.getString("description"));
        return p;
    };

    public ProductRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Product> findAll() {
        return jdbc.query("SELECT * FROM product ORDER BY id", MAPPER);
    }

    public Optional<Product> findById(long id) {
        return jdbc.query("SELECT * FROM product WHERE id = ?", MAPPER, id).stream().findFirst();
    }
}
