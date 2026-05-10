package dk.nap.atlas.controller;

import dk.nap.atlas.dto.FollowUpForm;
import dk.nap.atlas.model.Admin;
import dk.nap.atlas.repository.AdminRepository;
import dk.nap.atlas.service.FollowUpService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/follow-ups")
public class FollowUpController {

    private final FollowUpService followUpService;
    private final AdminRepository adminRepository;

    public FollowUpController(FollowUpService followUpService, AdminRepository adminRepository) {
        this.followUpService = followUpService;
        this.adminRepository = adminRepository;
    }

    @PostMapping
    public String createFollowUp(@Valid @ModelAttribute FollowUpForm form,
                                 BindingResult br,
                                 @AuthenticationPrincipal UserDetails principal,
                                 RedirectAttributes redirect) {
        if (br.hasErrors()) {
            redirect.addFlashAttribute("flashError", "Udfyld dato og type");
            return "redirect:/admin/customers/" + form.getCustomerId();
        }
        try {
            Admin admin = adminRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Admin ikke fundet"));
            followUpService.schedule(form.getCustomerId(), admin.getId(),
                form.getPlannedDate(), form.getType(), form.getNotes());
            redirect.addFlashAttribute("flashMessage", "Opfølgning planlagt");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/admin/customers/" + form.getCustomerId();
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id,
                           @RequestParam(required = false) String outcomeNote,
                           @RequestParam Long customerId,
                           RedirectAttributes redirect) {
        followUpService.complete(id, outcomeNote);
        redirect.addFlashAttribute("flashMessage", "Opfølgning markeret som udført");
        return "redirect:/admin/customers/" + customerId;
    }
}
