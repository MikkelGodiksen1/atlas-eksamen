package dk.nap.atlas.controller;

import dk.nap.atlas.dto.CustomerForm;
import dk.nap.atlas.model.Customer;
import dk.nap.atlas.model.FollowUp;
import dk.nap.atlas.model.Inquiry;
import dk.nap.atlas.model.Mockup;
import dk.nap.atlas.repository.CustomerRepository;
import dk.nap.atlas.repository.InquiryRepository;
import dk.nap.atlas.repository.MockupRepository;
import dk.nap.atlas.service.FollowUpService;
import dk.nap.atlas.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CustomerRepository customerRepository;
    private final InquiryRepository inquiryRepository;
    private final MockupRepository mockupRepository;
    private final InquiryService inquiryService;
    private final FollowUpService followUpService;

    public AdminController(CustomerRepository customerRepository,
                           InquiryRepository inquiryRepository,
                           MockupRepository mockupRepository,
                           InquiryService inquiryService,
                           FollowUpService followUpService) {
        this.customerRepository = customerRepository;
        this.inquiryRepository = inquiryRepository;
        this.mockupRepository = mockupRepository;
        this.inquiryService = inquiryService;
        this.followUpService = followUpService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<FollowUp> overdue = followUpService.overdueFollowUps();
        List<FollowUp> today = followUpService.todayFollowUps();
        List<Inquiry> recent = inquiryRepository.findAll();
        model.addAttribute("overdue", overdue);
        model.addAttribute("today", today);
        model.addAttribute("recentInquiries", recent.subList(0, Math.min(5, recent.size())));
        model.addAttribute("totalInquiries", recent.size());
        return "admin/dashboard";
    }

    @GetMapping("/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "admin/customers";
    }

    @GetMapping("/customers/new")
    public String newCustomerForm(Model model) {
        if (!model.containsAttribute("customerForm")) {
            model.addAttribute("customerForm", new CustomerForm());
        }
        return "admin/customer-form";
    }

    @PostMapping("/customers/create")
    public String createCustomer(@Valid @ModelAttribute CustomerForm customerForm,
                                 BindingResult br,
                                 RedirectAttributes redirect) {
        if (br.hasErrors()) {
            return "admin/customer-form";
        }
        Optional<Customer> existing = customerRepository.findByCvr(customerForm.getCvr());
        if (existing.isPresent()) {
            br.rejectValue("cvr", "duplicate", "CVR er allerede registreret");
            return "admin/customer-form";
        }
        Customer c = new Customer(
            customerForm.getCompanyName(),
            customerForm.getCvr(),
            customerForm.getContactPerson(),
            customerForm.getEmail(),
            customerForm.getPhone(),
            customerForm.getAddress());
        customerRepository.save(c);
        redirect.addFlashAttribute("flashMessage", "Kunde oprettet: " + c.getCompanyName());
        return "redirect:/admin/customers";
    }

    @GetMapping("/customers/{id}")
    public String customerDetail(@PathVariable Long id, Model model) {
        Customer c = customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Kunde ikke fundet"));
        model.addAttribute("customer", c);
        model.addAttribute("inquiries", inquiryRepository.findByCustomerId(id));
        model.addAttribute("followUps", followUpService.historyForCustomer(id));
        return "admin/customer-detail";
    }

    @GetMapping("/inquiries")
    public String listInquiries(Model model) {
        List<Inquiry> all = inquiryRepository.findAll();
        model.addAttribute("inquiries", all);
        return "admin/inquiries";
    }

    @GetMapping("/inquiries/{id}")
    public String inquiryDetail(@PathVariable Long id, Model model) {
        Inquiry inq = inquiryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Forespørgsel ikke fundet"));
        Customer c = customerRepository.findById(inq.getCustomerId()).orElse(null);
        Mockup m = inq.getMockupId() == null ? null : mockupRepository.findById(inq.getMockupId()).orElse(null);
        model.addAttribute("inquiry", inq);
        model.addAttribute("customer", c);
        model.addAttribute("mockup", m);
        return "admin/inquiry-detail";
    }

    @PostMapping("/inquiries/{id}/status")
    public String updateInquiryStatus(@PathVariable Long id,
                                      @RequestParam String newStatus,
                                      RedirectAttributes redirect) {
        try {
            inquiryService.updateStatus(id, newStatus);
            redirect.addFlashAttribute("flashMessage", "Status opdateret til " + newStatus);
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/admin/inquiries/" + id;
    }
}
