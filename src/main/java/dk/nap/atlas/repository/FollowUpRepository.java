package dk.nap.atlas.repository;

import dk.nap.atlas.model.FollowUp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class FollowUpRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<FollowUp> MAPPER = (rs, n) -> {
        FollowUp f = new FollowUp();
        f.setId(rs.getLong("id"));
        f.setCustomerId(rs.getLong("customer_id"));
        f.setAdminId(rs.getLong("admin_id"));
        if (rs.getDate("planned_date") != null) f.setPlannedDate(rs.getDate("planned_date").toLocalDate());
        f.setType(rs.getString("type"));
        f.setStatus(rs.getString("status"));
        f.setNotes(rs.getString("notes"));
        if (rs.getTimestamp("created_at") != null) f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("completed_at") != null) f.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
        return f;
    };

    public FollowUpRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public FollowUp save(FollowUp f) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO follow_up (customer_id, admin_id, planned_date, type, status, notes) VALUES (?, ?, ?, ?, ?, ?)",
                new String[]{"id"});
            ps.setLong(1, f.getCustomerId());
            ps.setLong(2, f.getAdminId());
            ps.setDate(3, Date.valueOf(f.getPlannedDate()));
            ps.setString(4, f.getType());
            ps.setString(5, f.getStatus() == null ? "planned" : f.getStatus());
            ps.setString(6, f.getNotes());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) f.setId(key.longValue());
        return f;
    }

    public Optional<FollowUp> findById(long id) {
        return jdbc.query("SELECT * FROM follow_up WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public List<FollowUp> findByCustomerId(long customerId) {
        return jdbc.query("SELECT * FROM follow_up WHERE customer_id = ? ORDER BY planned_date DESC", MAPPER, customerId);
    }

    public List<FollowUp> findOverdue() {
        return jdbc.query(
            "SELECT * FROM follow_up WHERE status = 'planned' AND planned_date < ? ORDER BY planned_date",
            MAPPER, Date.valueOf(LocalDate.now()));
    }

    public List<FollowUp> findToday() {
        return jdbc.query(
            "SELECT * FROM follow_up WHERE status = 'planned' AND planned_date = ? ORDER BY id",
            MAPPER, Date.valueOf(LocalDate.now()));
    }

    public List<FollowUp> findUpcoming() {
        return jdbc.query(
            "SELECT * FROM follow_up WHERE status = 'planned' ORDER BY planned_date",
            MAPPER);
    }

    public void markCompleted(long id, String outcomeNote) {
        jdbc.update(
            "UPDATE follow_up SET status = 'completed', completed_at = CURRENT_TIMESTAMP, notes = COALESCE(notes, '') || CASE WHEN COALESCE(notes,'')='' THEN '' ELSE chr(10) END || ? WHERE id = ?",
            outcomeNote == null ? "" : "Udfald: " + outcomeNote, id);
    }
}
