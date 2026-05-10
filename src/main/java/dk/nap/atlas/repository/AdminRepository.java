package dk.nap.atlas.repository;

import dk.nap.atlas.model.Admin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Admin> MAPPER = (rs, n) -> {
        Admin a = new Admin();
        a.setId(rs.getLong("id"));
        a.setName(rs.getString("name"));
        a.setEmail(rs.getString("email"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setRole(rs.getString("role"));
        if (rs.getTimestamp("created_at") != null) a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return a;
    };

    public AdminRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Admin> findByEmail(String email) {
        return jdbc.query("SELECT * FROM admin WHERE email = ?", MAPPER, email).stream().findFirst();
    }

    public Optional<Admin> findById(long id) {
        return jdbc.query("SELECT * FROM admin WHERE id = ?", MAPPER, id).stream().findFirst();
    }
}
