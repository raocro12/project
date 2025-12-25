package ru.study.my_project.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.my_project.model.Book;
import ru.study.my_project.repository.BookLendingRepository;
import ru.study.my_project.repository.BookRepository;

import java.util.List;

/**
 * Сервисный слой для работы с книгами.
 *
 * <p>
 * Отвечает за бизнес-логику, связанную с сущностью {@link Book}:
 * поиск, создание, обновление и удаление книг.
 * </p>
 *
 * <p>
 * Сервис взаимодействует с:
 * <ul>
 *   <li>{@link BookRepository} — для операций с таблицей книг</li>
 *   <li>{@link BookLendingRepository} — для проверки, находится ли книга на руках</li>
 * </ul>
 * </p>
 *
 * <p>
 * Все методы выполняются в транзакции.
 * Методы только для чтения помечены {@code readOnly = true}.
 * </p>
 */
@Service
@Transactional
public class BookService {
    private final BookRepository bookRepository;
    private final BookLendingRepository bookLendingRepository;

    public BookService(BookRepository bookRepository, BookLendingRepository bookLendingRepository) {

        this.bookRepository = bookRepository;
        this.bookLendingRepository = bookLendingRepository;
    }

    /**
     * Поиск книги по id.
     *
     * @param id идентификатор книги
     * @return найденная книга
     * @throws RuntimeException если книга не найдена
     */
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));
    }

    /**
     * Поиск книг по автору.
     *
     * <p>
     * Поиск осуществляется по частичному совпадению имени автора,
     * без учета регистра.
     * </p>
     *
     * @param author имя или часть имени автора
     * @return список книг данного автора
     */
    @Transactional(readOnly = true)
    public List<Book> findByAuthor(String author) {

        return bookRepository.findAllByAuthorContainingIgnoreCase(author);
    }

    /**
     * Поиск книг по жанру.
     *
     * @param genre жанр книги
     * @return список книг выбранного жанра
     */
    public List<Book> findByGenre(String genre) {

        return bookRepository.findAllByGenre(genre);
    }

    /**
     * Получение списка всех книг.
     *
     * @return список всех книг в библиотеке
     */
    @Transactional(readOnly = true)
    public List<Book> findAll() {

        return bookRepository.findAll();
    }

    /**
     * Регистрация новой книги.
     *
     * <p>
     * Перед сохранением выполняется проверка:
     * книга с таким названием, автором и издательством
     * не должна уже существовать.
     * </p>
     *
     * @param book новая книга
     * @return сохраненная книга
     * @throws RuntimeException если книга уже существует
     */
    public Book registerBook(@Valid Book book) {

        if (bookRepository.existsByNameAndAuthorAndPublishingHouse(book.getName(), book.getAuthor(), book.getPublishingHouse())) {
            throw new RuntimeException("Книга уже существует ");
        } else {
            return bookRepository.save(book);
        }
    }

    /**
     * Обновление данных существующей книги.
     *
     * <p>
     * Выполняется проверка на дубликат:
     * нельзя изменить книгу так, чтобы она совпадала
     * с уже существующей.
     * </p>
     *
     * @param id идентификатор книги
     * @param updatedBook новые данные книги
     * @return обновленная книга
     * @throws RuntimeException если книга с такими данными уже существует
     */
    public Book updateBook(Long id, Book updatedBook) {
        if (bookRepository.existsByNameAndAuthorAndPublishingHouse(updatedBook.getName(), updatedBook.getAuthor(), updatedBook.getPublishingHouse()))
        {
            throw new RuntimeException("Книга уже существует ");
        }
        Book existing = findById(id);
        existing.setAuthor(updatedBook.getAuthor());
        existing.setGenre(updatedBook.getGenre());
        existing.setName(updatedBook.getName());
        existing.setPublishingHouse(updatedBook.getPublishingHouse());
        existing.setYearOfPublication(updatedBook.getYearOfPublication());
        return bookRepository.save(existing);
    }

    /**
     * Удаление книги.
     *
     * <p>
     * Книга не может быть удалена, если она в данный момент
     * находится на руках у читателя (активная выдача).
     * </p>
     *
     * @param id идентификатор книги
     * @throws IllegalStateException если книга сейчас выдана
     */
    public void deleteBook(Long id) {
        if (bookLendingRepository.existsByBookIdAndReturnDateIsNull(id)) {
            throw new IllegalStateException("Нельзя удалить книгу: она сейчас находится на руках у читателя.");
        }
        Book book = findById(id);
        bookRepository.delete(book);
    }
}
