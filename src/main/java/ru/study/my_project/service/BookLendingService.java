package ru.study.my_project.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.my_project.model.*;
import ru.study.my_project.repository.*;

import java.time.LocalDate;
import java.util.List;


/**
 * Сервисный слой для управления выдачей и возвратом книг.
 *
 * <p>
 * Отвечает за бизнес-логику, связанную с сущностью {@link BookLending}:
 * оформление выдачи книг читателям, возврат книг,
 * редактирование записей о выдаче и контроль доступности книг.
 * </p>
 *
 * <p>
 * Сервис взаимодействует с:
 * <ul>
 *   <li>{@link BookLendingRepository} — операции с выдачами книг</li>
 *   <li>{@link ReaderRepository} — получение читателей</li>
 *   <li>{@link BookRepository} — получение книг</li>
 * </ul>
 * </p>
 *
 * <p>
 * Все операции выполняются в рамках транзакции.
 * Методы, не изменяющие данные, помечены {@code readOnly = true}.
 * </p>
 */
@Service
@Transactional
public class BookLendingService {
    private final BookLendingRepository lendingRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    public BookLendingService(
            BookLendingRepository bookLendingRepository,
            ReaderRepository readerRepository,
            BookRepository bookRepository) {
        this.lendingRepository = bookLendingRepository;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Получить все выдачи
     * @return список всех выдач
     */
    @Transactional(readOnly = true)
    public List<BookLending> findAll() {
        return lendingRepository.findAll();
    }


    /**
     * Найти запись о выдаче по идентификатору.
     *
     * @param id идентификатор записи о выдаче
     * @return найденная запись
     * @throws EntityNotFoundException если запись не найдена
     */
    public BookLending findById(Long id) {
        return lendingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Выдача с ID " + id + " не найдена"
        ));
    }

    /**
     * Оформить выдачу книги читателю.
     *
     * <p>
     * Перед выдачей выполняются проверки:
     * <ul>
     *   <li>книга не должна быть уже выдана и не возвращена</li>
     *   <li>читатель должен существовать</li>
     *   <li>книга должна существовать</li>
     * </ul>
     * </p>
     *
     * <p>
     * Дата выдачи устанавливается автоматически текущей датой.
     * </p>
     *
     * @param readerId идентификатор читателя
     * @param bookId идентификатор книги
     * @return сохраненная запись о выдаче
     * @throws RuntimeException если книга уже выдана, либо читатель или книга не найдены
     */
    public BookLending issueBook(Long readerId, Long bookId) {
        if (lendingRepository.existsByBookIdAndReturnDateIsNull(bookId)) {
            throw new RuntimeException("Книга уже выдана и еще не возвращена!");
        }

        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Читатель не найден"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));

        BookLending lending = new BookLending();
        lending.setReader(reader);
        lending.setBook(book);
        lending.setDateOfIssue(LocalDate.now());

        return lendingRepository.save(lending);
    }

    /**
     * Оформить возврат книги.
     *
     * <p>
     * Если книга уже была возвращена ранее,
     * операция запрещается.
     * </p>
     *
     * <p>
     * Дата возврата устанавливается текущей датой.
     * </p>
     *
     * @param lendingId идентификатор записи о выдаче
     * @return обновленная запись о выдаче
     * @throws RuntimeException если запись не найдена или книга уже возвращена
     */
    public BookLending returnBook(Long lendingId) {
        BookLending lending = lendingRepository.findById(lendingId)
                .orElseThrow(() -> new RuntimeException("Запись о выдаче не найдена"));

        if (lending.getReturnDate() != null) {
            throw new RuntimeException("Эта книга уже была возвращена ранее");
        }

        lending.setReturnDate(LocalDate.now());
        return lendingRepository.save(lending);
    }

    /**
     * Получить список активных выдач (книги, которые сейчас на руках).
     *
     * <p>
     * Используется, например, для отображения
     * выпадающего списка при возврате книг.
     * </p>
     *
     * @return список активных выдач
     */
    @Transactional(readOnly = true)
    public List<BookLending> findActiveLendings() {
        return lendingRepository.findAllByReturnDateIsNull();
    }

    /**
     * Обновить данные записи о выдаче.
     *
     * <p>
     * Если в процессе обновления меняется книга,
     * выполняется проверка, что новая книга
     * не находится на руках у другого читателя.
     * </p>
     *
     * @param id идентификатор записи о выдаче
     * @param readerId идентификатор читателя
     * @param bookId идентификатор книги
     * @param dateOfIssue дата выдачи
     * @param returnDate дата возврата
     * @throws RuntimeException если запись, книга или читатель не найдены
     * @throws IllegalStateException если новая книга уже выдана
     */
    @Transactional
    public void updateLending(Long id, Long readerId, Long bookId, LocalDate dateOfIssue, LocalDate returnDate) {
        BookLending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));

        if (!lending.getBook().getId().equals(bookId) &&
                lendingRepository.existsByBookIdAndReturnDateIsNull(bookId)) {
            throw new IllegalStateException("Выбранная новая книга уже находится на руках!");
        }

        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new RuntimeException("Читатель не найден"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));

        lending.setReader(reader);
        lending.setBook(book);
        lending.setDateOfIssue(dateOfIssue);
        lending.setReturnDate(returnDate);

        lendingRepository.save(lending);
    }


    /**
     * Найти все просроченные книги на сегодня
     */
    @Transactional(readOnly = true)
    public List<BookLending> getOverdueLendings() {
        return lendingRepository.findAllByReturnDateIsNullAndDateOfDueBefore(LocalDate.now());
    }

    public BookLending save(BookLending lending) {
        return lendingRepository.save(lending);
    }

    /**
     * Удалить запись о выдаче.
     *
     * <p>
     * Удаление запрещено, если книга еще не возвращена.
     * </p>
     *
     * @param id идентификатор записи
     * @throws IllegalStateException если выдача активна
     */
    public void deleteLending(Long id) {
        BookLending lending = findById(id);

        if (lending.getReturnDate() == null) {
            throw new IllegalStateException("Нельзя удалить активную запись: книга еще не возвращена в библиотеку.");
        }
        BookLending bookLending = findById(id);
        lendingRepository.delete(bookLending);
    }

}
