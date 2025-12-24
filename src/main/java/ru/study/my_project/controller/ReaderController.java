package ru.study.my_project.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.study.my_project.model.Reader;
import ru.study.my_project.service.ReaderService;

import java.util.List;

@Controller
@RequestMapping("/readers")
public class ReaderController {
    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public String listReaders(Model model) {
        List<Reader> readers = readerService.findAll();
        model.addAttribute("readers", readers);
        return "readers/list";
    }

    // Поиск
    @GetMapping("/search")
    public String searchReaders(@RequestParam String lastName, Model model) {
        model.addAttribute("readers", readerService.searchByLastName(lastName));
        return "readers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("reader", new Reader());
        return "readers/create";
    }

    @PostMapping
    public String createReader(
            @Valid @ModelAttribute("reader") Reader reader,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {
        if (bindingResult.hasErrors()) {
            return "readers/create";
        }

        try {
            readerService.registerReader(reader);
            return "redirect:/readers";
        } catch (Exception e) {
            // Ловим ошибки из БД или Сервиса.
            // Добавляем сообщение об ошибке
            ra.addFlashAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            return "redirect:/readers/new"; // Возвращаем на форму
        }
    }

    // Изменение
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("reader", readerService.findById(id));
        return "readers/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateReader(
            @PathVariable Long id,
            @Valid @ModelAttribute Reader reader,
            BindingResult bindingResult,
            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "readers/edit";
        }
        try {
            ra.addFlashAttribute("successMessage", "Читатель успешно обновлен");
            readerService.updateReader(id, reader);
            return "redirect:/readers";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/readers/edit/";
        }
    }

    // Удаление
    @GetMapping("/delete/{id}")
    public String deleteReader(@PathVariable Long id, RedirectAttributes ra) {
        try {
            readerService.deleteReader(id);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/readers";
    }
}
