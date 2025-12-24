package ru.study.my_project.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.study.my_project.model.Book;
import ru.study.my_project.model.Reader;
import ru.study.my_project.service.BookService;
import ru.study.my_project.service.ReaderService;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;


    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String listBooks(Model model) {
        List<Book> readers = bookService.findAll();
        model.addAttribute("books", readers);
        return "books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/create";
    }

    // ответ на создание
    @PostMapping
    public String createBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {
        // Проверка стандартной валидации (пустые поля, неправильный формат)
        if (bindingResult.hasErrors()) {
            return "books/create"; // Возвращаем форму с подсвеченными ошибками
        }

        try {
            bookService.registerBook(book);
            // Если всё успешно — пишем сообщение
            ra.addFlashAttribute("successMessage", "Книга успешно добавлена!");
            return "redirect:/books";
        } catch (Exception e) {
            // Ловим ошибки из БД или Сервиса.
            // Добавляем сообщение об ошибке
            ra.addFlashAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            return "redirect:/books/new"; // Возвращаем на форму
        }
    }

    //Изменение
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        return "books/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(
            @PathVariable Long id,
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "books/edit";
        }
        try {
            ra.addFlashAttribute("successMessage", "Книга успешно обновлена");
            bookService.updateBook(id, book);
            return "redirect:/books";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "books/edit";
        }
    }

    // Удаление
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookService.deleteBook(id);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books";
    }
}
