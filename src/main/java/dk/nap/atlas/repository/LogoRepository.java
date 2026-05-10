package dk.nap.atlas.repository;

import dk.nap.atlas.model.Logo;
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
public class LogoRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Logo> MAPPER = (rs, n) -> {
        Logo l = new Logo();
        l.setId(rs.getLong("id"));
        long cid = rs.getLong("customer_id");
        l.setCustomerId(rs.wasNull() ? null : cid);
        l.setFilename(rs.getString("filename"));
        l.setFilePath(rs.getString("file_path"));
        l.setFileSizeBytes(rs.getLong("file_size_bytes"));
        l.setMimeType(rs.getString("mime_type"));
        if (rs.getTimestamp("uploaded_at") != null) l.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
        return l;
    };

    public LogoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Logo save(Logo l) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO logo (customer_id, filename, file_path, file_size_bytes, mime_type) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            if (l.getCustomerId() == null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, l.getCustomerId());
            ps.setString(2, l.getFilename());
            ps.setString(3, l.getFilePath());
            ps.setLong(4, l.getFileSizeBytes());
            ps.setString(5, l.getMimeType());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) l.setId(key.longValue());
        return l;
    }

    public Optional<Logo> findById(long id) {
        return jdbc.query("SELECT * FROM logo WHERE id = ?", MAPPER, id).stream().findFirst();
    }
}
