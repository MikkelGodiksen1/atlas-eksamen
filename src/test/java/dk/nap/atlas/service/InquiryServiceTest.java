package dk.nap.atlas.service;

import dk.nap.atlas.model.Inquiry;
import dk.nap.atlas.repository.InquiryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * White-box test af status-transition-logik (state-diagram.md).
 */
@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock private dk.nap.atlas.repository.CustomerRepository customerRepository;
    @Mock private InquiryRepository inquiryRepository;
    @Mock private PriceCalculationService priceService;
    @InjectMocks private InquiryService service;

    private Inquiry stub(String status) {
        Inquiry i = new Inquiry();
        i.setId(1L);
        i.setStatus(status);
        return i;
    }

    @Test
    void updateStatus_newToInProgress_allowed() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(stub("new")));
        service.updateStatus(1L, "in_progress");
        verify(inquiryRepository).updateStatus(1L, "in_progress");
    }

    @Test
    void updateStatus_newToWon_disallowed() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(stub("new")));
        assertThrows(IllegalStateException.class,
            () -> service.updateStatus(1L, "won"));
    }

    @Test
    void updateStatus_closedToAnything_disallowed() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(stub("closed")));
        assertThrows(IllegalStateException.class,
            () -> service.updateStatus(1L, "in_progress"));
    }

    @Test
    void updateStatus_answeredBackToInProgress_allowed() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(stub("answered")));
        service.updateStatus(1L, "in_progress");
        verify(inquiryRepository).updateStatus(1L, "in_progress");
    }

    @Test
    void updateStatus_unknownInquiry_throws() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> service.updateStatus(99L, "closed"));
    }
}
