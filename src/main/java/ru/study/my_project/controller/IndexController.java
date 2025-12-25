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

 /**
 * Контроллер главной страницы библиотечной системы.
 *
 * <p>
 * Отвечает за отображение главной страницы и обработку основных
 * пользовательских действий: выдачу и возврат книг.
 * </p>
 *
 * <p>
 * Контроллер взаимодействует со следующими сервисами:
 * </p>
 * <ul>
 *     <li>{@link ru.study.my_project.service.ReaderService} — для получения списка читателей</li>
 *     <li>{@link ru.study.my_project.service.BookService} — для получения списка книг</li>
 *     <li>{@link ru.study.my_project.service.BookLendingService} — для управления выдачами книг</li>
 * </ul>
 *
 * <p>
 * Использует шаблонизатор Thymeleaf для отображения страницы
 * {@code src/main/resources/templates/index.html}.
 * </p>
 *
 */
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
      * Отображает главную страницу библиотечной системы.
      *
      * <p>
      * Загружает:
      * </p>
      * <ul>
      *     <li>список всех читателей</li>
      *     <li>список всех книг</li>
      *     <li>список активных выдач (не возвращённых книг)</li>
      * </ul>
      *
      * @param model модель для передачи данных в представление
      * @return имя HTML-шаблона главной страницы
      */
    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("readers", readerService.findAll());
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("activeLendings", bookLendingService.findActiveLendings());
        return "index";
    }

     /**
      * Обрабатывает выдачу книги читателю.
      *
      * <p>
      * В случае успешной выдачи отображает сообщение об успехе.
      * При возникновении ошибки отображает сообщение с описанием причины.
      * </p>
      *
      * @param readerId идентификатор читателя
      * @param bookId идентификатор книги
      * @param redirectAttributes атрибуты для передачи сообщений после редиректа
      * @return перенаправление на главную страницу
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
      * Обрабатывает возврат книги.
      *
      * <p>
      * Устанавливает дату возврата книги и обновляет статус выдачи.
      * </p>
      *
      * @param lendingId идентификатор записи выдачи книги
      * @param redirectAttributes атрибуты для передачи сообщений после редиректа
      * @return перенаправление на главную страницу
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
