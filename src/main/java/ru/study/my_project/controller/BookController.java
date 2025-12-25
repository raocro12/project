package ru.study.my_project.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.study.my_project.model.Book;
import ru.study.my_project.service.BookService;

import java.util.List;

/**
 * Контроллер для управления книгами библиотеки.
 * <p>
 * Обрабатывает HTTP-запросы, связанные с:
 * <ul>
 *     <li>просмотром списка книг</li>
 *     <li>добавлением новой книги</li>
 *     <li>редактированием информации о книге</li>
 *     <li>удалением книги</li>
 * </ul>
 *
 * <p>
 * Использует {@link BookService} для выполнения бизнес-логики.
 *
 */
@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    /**
     * Конструктор контроллера книг.
     *
     * @param bookService сервис для работы с книгами
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Отображение списка всех книг.
     *
     * @param model модель для передачи списка книг в представление
     * @return HTML-шаблон со списком книг
     */
    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        return "books/list";
    }

    /**
     * Отображение формы добавления новой книги.
     *
     * @param model модель для передачи пустого объекта Book
     * @return HTML-шаблон формы создания книги
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/create";
    }

    /**
     * Обработка формы добавления новой книги.
     * <p>
     * Выполняет валидацию данных и сохраняет книгу в базе данных.
     *
     * @param book объект книги с заполненными данными
     * @param bindingResult результат валидации формы
     * @param model модель представления
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список книг или возврат формы с ошибками
     */
    @PostMapping
    public String createBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {
        if (bindingResult.hasErrors()) {
            return "books/create";
        }

        try {
            bookService.registerBook(book);
            ra.addFlashAttribute("successMessage", "Книга успешно добавлена!");
            return "redirect:/books";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            return "redirect:/books/new";
        }
    }

    /**
     * Отображение формы редактирования книги.
     *
     * @param id идентификатор книги
     * @param model модель для передачи данных книги
     * @return HTML-шаблон формы редактирования книги
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        return "books/edit";
    }

    /**
     * Обработка формы редактирования книги.
     *
     * @param id идентификатор редактируемой книги
     * @param book объект с обновлёнными данными книги
     * @param bindingResult результат валидации
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список книг или возврат формы с ошибкой
     */
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
            bookService.updateBook(id, book);
            ra.addFlashAttribute("successMessage", "Книга успешно обновлена");
            return "redirect:/books";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "books/edit";
        }
    }

    /**
     * Удаление книги по идентификатору.
     * <p>
     * Удаление запрещено, если книга находится на руках у читателя.
     *
     * @param id идентификатор книги
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список книг
     */
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
