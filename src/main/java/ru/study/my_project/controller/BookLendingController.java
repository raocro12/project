package ru.study.my_project.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.study.my_project.model.BookLending;
import ru.study.my_project.service.BookLendingService;
import ru.study.my_project.service.BookService;
import ru.study.my_project.service.ReaderService;

import java.time.LocalDate;

/**
 * Контроллер для управления выдачами книг читателям.
 * <p>
 * Обрабатывает HTTP-запросы, связанные с:
 * <ul>
 *     <li>просмотром всех выдач книг</li>
 *     <li>редактированием записей о выдаче</li>
 *     <li>возвратом книг</li>
 *     <li>удалением записей о выдаче</li>
 * </ul>
 *
 * <p>
 * Использует сервисы {@link BookLendingService}, {@link ReaderService}
 * и {@link BookService} для работы с бизнес-логикой приложения.
 *
 */
@Controller
@RequestMapping("/lendings")
public class BookLendingController {

    private final BookLendingService bookLendingService;
    private final ReaderService readerService;
    private final BookService bookService;

    /**
     * Конструктор контроллера выдач книг.
     *
     * @param bookLendingService сервис работы с выдачами
     * @param readerService сервис работы с читателями
     * @param bookService сервис работы с книгами
     */
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
     * Отображение списка всех выдач книг.
     *
     * @param model модель для передачи списка выдач в представление
     * @return HTML-шаблон со списком выдач
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("lendings", bookLendingService.findAll());
        return "lendings/list";
    }

    /**
     * Отображение формы редактирования записи о выдаче книги.
     *
     * @param id идентификатор записи о выдаче
     * @param model модель для передачи данных выдачи, книг и читателей
     * @return HTML-шаблон формы редактирования выдачи
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        BookLending lending = bookLendingService.findById(id);
        model.addAttribute("lending", lending);
        model.addAttribute("readers", readerService.findAll());
        model.addAttribute("books", bookService.findAll());
        return "lendings/edit";
    }

    /**
     * Обработка формы редактирования записи о выдаче книги.
     *
     * @param id идентификатор записи о выдаче
     * @param readerId идентификатор читателя
     * @param bookId идентификатор книги
     * @param dateOfIssue дата выдачи книги
     * @param returnDate дата возврата книги (может быть {@code null})
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список выдач
     */
    @PostMapping("/edit/{id}")
    public String updateLending(
            @PathVariable Long id,
            @RequestParam Long readerId,
            @RequestParam Long bookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfIssue,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
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

    /**
     * Удаление записи о выдаче книги.
     * <p>
     * Удаление запрещено, если книга ещё не возвращена.
     *
     * @param id идентификатор записи о выдаче
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список выдач
     */
    @GetMapping("/delete/{id}")
    public String deleteLending(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookLendingService.deleteLending(id);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/lendings";
    }
}
