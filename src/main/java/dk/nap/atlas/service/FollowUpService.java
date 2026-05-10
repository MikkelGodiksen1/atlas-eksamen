package dk.nap.atlas.service;

import dk.nap.atlas.model.FollowUp;
import dk.nap.atlas.repository.FollowUpRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Håndterer planlægning og udførelse af opfølgninger.
 * Forretningsregel BR6: dato må ikke være i fortiden ved oprettelse.
 */
@Service
public class FollowUpService {

    private final FollowUpRepository repo;

    public FollowUpService(FollowUpRepository repo) {
        this.repo = repo;
    }

    public FollowUp schedule(long customerId, long adminId, LocalDate plannedDate, String type, String notes) {
        if (plannedDate == null) throw new IllegalArgumentException("Plandato er påkrævet");
        if (plannedDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Plandato skal være i dag eller senere");
        }
        FollowUp f = new FollowUp();
        f.setCustomerId(customerId);
        f.setAdminId(adminId);
        f.setPlannedDate(plannedDate);
        f.setType(type);
        f.setStatus("planned");
        f.setNotes(notes);
        return repo.save(f);
    }

    public void complete(long followUpId, String outcomeNote) {
        repo.markCompleted(followUpId, outcomeNote);
    }

    public List<FollowUp> overdueFollowUps() { return repo.findOverdue(); }
    public List<FollowUp> todayFollowUps()   { return repo.findToday(); }
    public List<FollowUp> upcomingFollowUps(){ return repo.findUpcoming(); }

    public List<FollowUp> historyForCustomer(long customerId) {
        return repo.findByCustomerId(customerId);
    }
}
