package dk.nap.atlas.repository;

import dk.nap.atlas.model.Mockup;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;

@Repository
public class MockupRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Mockup> MAPPER = (rs, n) -> {
        Mockup m = new Mockup();
        m.setId(rs.getLong("id"));
        long cid = rs.getLong("customer_id");
        m.setCustomerId(rs.wasNull() ? null : cid);
        m.setProductId(rs.getLong("product_id"));
        m.setLogoId(rs.getLong("logo_id"));
        m.setFilePath(rs.getString("file_path"));
        m.setSelectedSpecifications(rs.getString("selected_specifications"));
        if (rs.getTimestamp("generated_at") != null) m.setGeneratedAt(rs.getTimestamp("generated_at").toLocalDateTime());
        return m;
    };

    public MockupRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Mockup save(Mockup m) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO mockup (customer_id, product_id, logo_id, file_path, selected_specifications) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            if (m.getCustomerId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, m.getCustomerId());
            ps.setLong(2, m.getProductId());
            ps.setLong(3, m.getLogoId());
            ps.setString(4, m.getFilePath());
            ps.setString(5, m.getSelectedSpecifications());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) m.setId(key.longValue());
        return m;
    }

    public Optional<Mockup> findById(long id) {
        return jdbc.query("SELECT * FROM mockup WHERE id = ?", MAPPER, id).stream().findFirst();
    }
}
