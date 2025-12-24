package ru.study.my_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.study.my_project.service.BookLendingService;
import ru.study.my_project.service.BookService;
import ru.study.my_project.service.ReaderService;

@Controller
public class IndexController {
    private final BookLendingService bookLendingService;
    private final ReaderService readerService;
    private final BookService bookService;

    public IndexController(BookLendingService bookLendingService, ReaderService readerService, BookService bookService) {
        this.bookLendingService = bookLendingService;
        this.readerService = readerService;
        this.bookService = bookService;
    }

    /**
     * Отображение главной страницы (Dashboard)
     */
    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("readers", readerService.findAll());
        model.addAttribute("books", bookService.findAll());
        // Используем новый метод сервиса для получения только активных записей
        model.addAttribute("activeLendings", bookLendingService.findActiveLendings());
        return "index"; // Возвращаем шаблон src/main/resources/templates/index.html
    }

    /**
     * Обработка выдачи книги с главной страницы
     */
    @PostMapping("/lending/issue")
    public String issueBook(@RequestParam Long readerId, @RequestParam Long bookId, RedirectAttributes redirectAttributes) {
        try {
            bookLendingService.issueBook(readerId, bookId);
            redirectAttributes.addFlashAttribute("successMessage", "Книга успешно выдана!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/"; // Перенаправляем на главную страницу
    }

    /**
     * Обработка возврата книги с главной страницы
     */
    @PostMapping("/lending/return")
    public String returnBook(@RequestParam Long lendingId, RedirectAttributes redirectAttributes) {
        try {
            bookLendingService.returnBook(lendingId);
            redirectAttributes.addFlashAttribute("successMessage", "Книга успешно принята!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/"; // Перенаправляем на главную страницу
    }
}
