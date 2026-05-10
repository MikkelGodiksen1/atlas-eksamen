package dk.nap.atlas.controller;

import dk.nap.atlas.dto.InquiryForm;
import dk.nap.atlas.model.Inquiry;
import dk.nap.atlas.model.Mockup;
import dk.nap.atlas.repository.MockupRepository;
import dk.nap.atlas.service.InquiryService;
import dk.nap.atlas.service.PriceCalculationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
public class InquiryController {

    private final InquiryService inquiryService;
    private final MockupRepository mockupRepository;
    private final PriceCalculationService priceService;

    public InquiryController(InquiryService inquiryService,
                             MockupRepository mockupRepository,
                             PriceCalculationService priceService) {
        this.inquiryService = inquiryService;
        this.mockupRepository = mockupRepository;
        this.priceService = priceService;
    }

    @GetMapping("/inquiry")
    public String showInquiryForm(@RequestParam Long mockupId,
                                  @RequestParam Long productId,
                                  @RequestParam(required = false) List<Long> specIds,
                                  @RequestParam(defaultValue = "100") int quantity,
                                  Model model) {
        Mockup mockup = mockupRepository.findById(mockupId)
            .orElseThrow(() -> new NoSuchElementException("Mockup ikke fundet"));
        PriceCalculationService.PriceResult pr = priceService.calculate(productId,
            specIds == null ? List.of() : specIds, quantity);

        if (!model.containsAttribute("inquiryForm")) {
            InquiryForm form = new InquiryForm();
            form.setQuantity(quantity);
            model.addAttribute("inquiryForm", form);
        }
        model.addAttribute("mockup", mockup);
        model.addAttribute("productId", productId);
        model.addAttribute("specIds", specIds == null ? List.of() : specIds);
        model.addAttribute("quantity", quantity);
        model.addAttribute("priceResult", pr);
        return "inquiry";
    }

    @PostMapping("/inquiry/submit")
    public String submit(@Valid @ModelAttribute InquiryForm inquiryForm,
                         BindingResult br,
                         @RequestParam Long mockupId,
                         @RequestParam Long productId,
                         @RequestParam(required = false) List<Long> specIds,
                         Model model,
                         RedirectAttributes redirect) {
        if (br.hasErrors()) {
            return showInquiryForm(mockupId, productId, specIds, inquiryForm.getQuantity(), model);
        }
        Inquiry inq = inquiryService.submitDesigner(inquiryForm, mockupId, productId,
            specIds == null ? List.of() : specIds);
        redirect.addAttribute("id", inq.getId());
        return "redirect:/inquiry/confirmation";
    }

    @GetMapping("/inquiry/confirmation")
    public String confirmation(@RequestParam Long id, Model model) {
        model.addAttribute("inquiryId", id);
        return "inquiry-confirmation";
    }

    @GetMapping("/contact")
    public String showContactForm(Model model) {
        if (!model.containsAttribute("inquiryForm")) {
            model.addAttribute("inquiryForm", new InquiryForm());
        }
        return "contact";
    }

    @PostMapping("/contact/submit")
    public String submitContact(@Valid @ModelAttribute InquiryForm inquiryForm,
                                BindingResult br,
                                Model model,
                                RedirectAttributes redirect) {
        if (br.hasErrors()) {
            return "contact";
        }
        Inquiry inq = inquiryService.submitDirectContact(inquiryForm);
        redirect.addAttribute("id", inq.getId());
        return "redirect:/inquiry/confirmation";
    }
}
