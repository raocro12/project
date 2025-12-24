package ru.study.my_project.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.study.my_project.model.BookLending;
import ru.study.my_project.model.Reader;
import ru.study.my_project.service.BookLendingService;
import ru.study.my_project.service.BookService;
import ru.study.my_project.service.ReaderService;

import java.time.LocalDate;

@Controller
@RequestMapping("/lendings")
public class BookLendingController {
    private final BookLendingService bookLendingService;
    private final ReaderService readerService;
    private final BookService bookService;

    public BookLendingController(
            BookLendingService bookLendingService,
            ReaderService readerService,
            BookService bookService
    ) {
        this.bookLendingService = bookLendingService;
        this.readerService = readerService;
        this.bookService = bookService;
    }

    /**
     * Список всех выдач
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("lendings", bookLendingService.findAll());
        return "lendings/list";
    }

    // 1. Показать форму редактирования
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        BookLending lending = bookLendingService.findById(id);
        model.addAttribute("lending", lending);
        model.addAttribute("readers", readerService.findAll());
        model.addAttribute("books", bookService.findAll());
        return "lendings/edit";
    }

    // 2. Обработать сохранение
    @PostMapping("/edit/{id}")
    public String updateLending(
            @PathVariable Long id,
            @RequestParam Long readerId,
            @RequestParam Long bookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfIssue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            RedirectAttributes ra
    ) {
        try {
            bookLendingService.updateLending(id, readerId, bookId, dateOfIssue, returnDate);
            ra.addFlashAttribute("successMessage", "Запись обновлена успешно");
            return "redirect:/lendings";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Ошибка обновления: " + e.getMessage());
            return "redirect:/lendings/edit/" + id;
        }
    }


    // Удаление
    @GetMapping("/delete/{id}")
    public String deleteLending(@PathVariable Long id, RedirectAttributes ra) {
        try{
            bookLendingService.deleteLending(id);
        } catch (Exception e){
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/lendings";
    }
}
