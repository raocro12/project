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

/**
 * Контроллер для управления читателями библиотеки.
 * <p>
 * Обрабатывает HTTP-запросы, связанные с:
 * <ul>
 *     <li>просмотром списка читателей</li>
 *     <li>поиском читателей</li>
 *     <li>добавлением нового читателя</li>
 *     <li>редактированием данных читателя</li>
 *     <li>удалением читателя</li>
 * </ul>
 *
 * <p>
 * Работает в связке с {@link ReaderService}, который содержит бизнес-логику.
 */
@Controller
@RequestMapping("/readers")
public class ReaderController {

    private final ReaderService readerService;

    /**
     * Конструктор контроллера читателей.
     *
     * @param readerService сервис для работы с читателями
     */
    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    /**
     * Отображение списка всех читателей.
     *
     * @param model модель для передачи данных в представление
     * @return имя HTML-шаблона со списком читателей
     */
    @GetMapping
    public String listReaders(Model model) {
        List<Reader> readers = readerService.findAll();
        model.addAttribute("readers", readers);
        return "readers/list";
    }

    /**
     * Поиск читателей по фамилии.
     *
     * @param lastName фамилия читателя
     * @param model модель для передачи результатов поиска
     * @return HTML-шаблон со списком найденных читателей
     */
    @GetMapping("/search")
    public String searchReaders(@RequestParam String lastName, Model model) {
        model.addAttribute("readers", readerService.searchByLastName(lastName));
        return "readers/list";
    }

    /**
     * Отображение формы добавления нового читателя.
     *
     * @param model модель для передачи пустого объекта Reader
     * @return HTML-шаблон формы создания читателя
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("reader", new Reader());
        return "readers/create";
    }

    /**
     * Обработка формы добавления нового читателя.
     *
     * @param reader объект читателя с заполненными данными
     * @param bindingResult результат валидации формы
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список читателей или форму с ошибкой
     */
    @PostMapping
    public String createReader(
            @Valid @ModelAttribute("reader") Reader reader,
            BindingResult bindingResult,
            RedirectAttributes ra
    ) {
        if (bindingResult.hasErrors()) {
            return "readers/create";
        }

        try {
            readerService.registerReader(reader);
            return "redirect:/readers";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Ошибка при сохранении: " + e.getMessage());
            return "redirect:/readers/new";
        }
    }

    /**
     * Отображение формы редактирования читателя.
     *
     * @param id идентификатор читателя
     * @param model модель для передачи данных читателя
     * @return HTML-шаблон формы редактирования
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("reader", readerService.findById(id));
        return "readers/edit";
    }

    /**
     * Обработка формы редактирования читателя.
     *
     * @param id идентификатор редактируемого читателя
     * @param reader объект с обновлёнными данными
     * @param bindingResult результат валидации
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список читателей
     */
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
            readerService.updateReader(id, reader);
            ra.addFlashAttribute("successMessage", "Читатель успешно обновлен");
            return "redirect:/readers";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/readers/edit/" + id;
        }
    }

    /**
     * Удаление читателя по идентификатору.
     * <p>
     * Удаление запрещено, если у читателя есть активные выдачи книг.
     *
     * @param id идентификатор читателя
     * @param ra объект для передачи flash-сообщений
     * @return перенаправление на список читателей
     */
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
