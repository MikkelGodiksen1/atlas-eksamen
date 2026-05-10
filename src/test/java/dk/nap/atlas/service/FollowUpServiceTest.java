package dk.nap.atlas.service;

import dk.nap.atlas.model.FollowUp;
import dk.nap.atlas.repository.FollowUpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowUpServiceTest {

    @Mock private FollowUpRepository repo;
    @InjectMocks private FollowUpService service;

    @Test
    void schedule_pastDate_throwsBR6() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThrows(IllegalArgumentException.class,
            () -> service.schedule(1L, 1L, yesterday, "opkald", "note"));
    }

    @Test
    void schedule_today_isAccepted() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        FollowUp f = service.schedule(1L, 1L, LocalDate.now(), "opkald", "note");
        assertEquals("planned", f.getStatus());
        assertEquals(LocalDate.now(), f.getPlannedDate());
    }

    @Test
    void schedule_futureDate_isAccepted() {
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        LocalDate future = LocalDate.now().plusWeeks(2);
        FollowUp f = service.schedule(1L, 1L, future, "møde", null);
        assertEquals(future, f.getPlannedDate());
    }

    @Test
    void schedule_nullDate_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> service.schedule(1L, 1L, null, "opkald", null));
    }

    private static <T> T any() { return org.mockito.ArgumentMatchers.any(); }
}
