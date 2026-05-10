package dk.nap.atlas.service;

import dk.nap.atlas.dto.InquiryForm;
import dk.nap.atlas.model.Customer;
import dk.nap.atlas.model.Inquiry;
import dk.nap.atlas.repository.CustomerRepository;
import dk.nap.atlas.repository.InquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Håndterer afgivelse af forespørgsel og status-overgange.
 * @Transactional sikrer atomisk find-or-create-customer + insert-inquiry (NFR-R1).
 */
@Service
public class InquiryService {

    /** Tilladte status-overgange jf. state-diagram.md. */
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "new",         Set.of("in_progress", "closed"),
        "in_progress", Set.of("answered", "closed"),
        "answered",    Set.of("won", "lost", "in_progress"),
        "won",         Set.of("closed"),
        "lost",        Set.of("closed"),
        "closed",      Set.of()
    );

    private final CustomerRepository customerRepository;
    private final InquiryRepository inquiryRepository;
    private final PriceCalculationService priceService;

    public InquiryService(CustomerRepository customerRepository,
                          InquiryRepository inquiryRepository,
                          PriceCalculationService priceService) {
        this.customerRepository = customerRepository;
        this.inquiryRepository = inquiryRepository;
        this.priceService = priceService;
    }

    @Transactional
    public Inquiry submitDesigner(InquiryForm form, Long mockupId, long productId, List<Long> specIds) {
        Customer customer = findOrCreateCustomer(form);
        PriceCalculationService.PriceResult pr = priceService.calculate(productId, specIds, form.getQuantity());

        Inquiry inq = new Inquiry();
        inq.setCustomerId(customer.getId());
        inq.setMockupId(mockupId);
        inq.setInquiryType("designer");
        inq.setQuantity(form.getQuantity());
        inq.setTotalPrice(pr.getTotalPrice());
        inq.setStatus("new");
        inq.setNotes(form.getMessage());
        return inquiryRepository.save(inq);
    }

    @Transactional
    public Inquiry submitDirectContact(InquiryForm form) {
        Customer customer = findOrCreateCustomer(form);
        Inquiry inq = new Inquiry();
        inq.setCustomerId(customer.getId());
        inq.setMockupId(null);
        inq.setInquiryType("direct_contact");
        inq.setStatus("new");
        StringBuilder note = new StringBuilder(form.getMessage() == null ? "" : form.getMessage());
        if (form.getPreferredCallTime() != null && !form.getPreferredCallTime().isBlank()) {
            if (note.length() > 0) note.append("\n");
            note.append("Foretrukken opkalds-tid: ").append(form.getPreferredCallTime());
        }
        inq.setNotes(note.toString());
        return inquiryRepository.save(inq);
    }

    public void updateStatus(long inquiryId, String newStatus) {
        Inquiry inq = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("Forespørgsel ikke fundet: " + inquiryId));
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(inq.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                "Ulovlig status-overgang fra '" + inq.getStatus() + "' til '" + newStatus + "'");
        }
        inquiryRepository.updateStatus(inquiryId, newStatus);
    }

    private Customer findOrCreateCustomer(InquiryForm form) {
        return customerRepository.findByCvr(form.getCvr())
            .map(existing -> {
                existing.setCompanyName(form.getCompanyName());
                existing.setContactPerson(form.getContactPerson());
                existing.setEmail(form.getEmail());
                existing.setPhone(form.getPhone());
                if (form.getAddress() != null) existing.setAddress(form.getAddress());
                customerRepository.update(existing);
                return existing;
            })
            .orElseGet(() -> customerRepository.save(new Customer(
                form.getCompanyName(),
                form.getCvr(),
                form.getContactPerson(),
                form.getEmail(),
                form.getPhone(),
                form.getAddress()
            )));
    }
}
