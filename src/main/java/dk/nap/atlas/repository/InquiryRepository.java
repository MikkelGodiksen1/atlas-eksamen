package dk.nap.atlas.repository;

import dk.nap.atlas.model.Inquiry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class InquiryRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Inquiry> MAPPER = (rs, n) -> {
        Inquiry i = new Inquiry();
        i.setId(rs.getLong("id"));
        i.setCustomerId(rs.getLong("customer_id"));
        long mid = rs.getLong("mockup_id");
        i.setMockupId(rs.wasNull() ? null : mid);
        i.setInquiryType(rs.getString("inquiry_type"));
        int q = rs.getInt("quantity");
        i.setQuantity(rs.wasNull() ? null : q);
        i.setTotalPrice(rs.getBigDecimal("total_price"));
        i.setStatus(rs.getString("status"));
        i.setNotes(rs.getString("notes"));
        if (rs.getTimestamp("created_at") != null) i.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return i;
    };

    public InquiryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Inquiry save(Inquiry inq) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO inquiry (customer_id, mockup_id, inquiry_type, quantity, total_price, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new String[]{"id"});
            ps.setLong(1, inq.getCustomerId());
            if (inq.getMockupId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, inq.getMockupId());
            ps.setString(3, inq.getInquiryType() == null ? "designer" : inq.getInquiryType());
            if (inq.getQuantity() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, inq.getQuantity());
            ps.setBigDecimal(5, inq.getTotalPrice());
            ps.setString(6, inq.getStatus() == null ? "new" : inq.getStatus());
            ps.setString(7, inq.getNotes());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) inq.setId(key.longValue());
        return inq;
    }

    public List<Inquiry> findAll() {
        return jdbc.query("SELECT * FROM inquiry ORDER BY created_at DESC", MAPPER);
    }

    public Optional<Inquiry> findById(long id) {
        return jdbc.query("SELECT * FROM inquiry WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public List<Inquiry> findByCustomerId(long customerId) {
        return jdbc.query("SELECT * FROM inquiry WHERE customer_id = ? ORDER BY created_at DESC", MAPPER, customerId);
    }

    public void updateStatus(long id, String status) {
        jdbc.update("UPDATE inquiry SET status = ? WHERE id = ?", status, id);
    }
}
