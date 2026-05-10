package dk.nap.atlas.repository;

import dk.nap.atlas.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-baseret repository for Customer-entiteten.
 * Implementerer Repository-mønstret som beskrevet i design-class-diagram.md.
 * GRASP: Information Expert for at hente og persistere Customer-data.
 */
@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Customer> ROW_MAPPER = (rs, rowNum) -> {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setCompanyName(rs.getString("company_name"));
        c.setCvr(rs.getString("cvr"));
        c.setContactPerson(rs.getString("contact_person"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        if (rs.getTimestamp("created_at") != null) c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return c;
    };

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Customer> findAll() {
        return jdbc.query("SELECT * FROM customer ORDER BY company_name", ROW_MAPPER);
    }

    public Optional<Customer> findById(long id) {
        return jdbc.query("SELECT * FROM customer WHERE id = ?", ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Customer> findByCvr(String cvr) {
        return jdbc.query("SELECT * FROM customer WHERE cvr = ?", ROW_MAPPER, cvr).stream().findFirst();
    }

    public Customer save(Customer c) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO customer (company_name, cvr, contact_person, email, phone, address) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getCompanyName());
            ps.setString(2, c.getCvr());
            ps.setString(3, c.getContactPerson());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getPhone());
            ps.setString(6, c.getAddress());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) c.setId(key.longValue());
        return c;
    }

    public void update(Customer c) {
        jdbc.update(
            "UPDATE customer SET company_name = ?, contact_person = ?, email = ?, phone = ?, address = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            c.getCompanyName(), c.getContactPerson(), c.getEmail(), c.getPhone(), c.getAddress(), c.getId());
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM customer WHERE id = ?", id);
    }
}
