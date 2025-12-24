package ru.study.my_project.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.my_project.model.*;
import ru.study.my_project.repository.*;

import java.time.LocalDate;
import java.util.List;

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

    @Transactional(readOnly = true)
    public List<BookLending> findAll() {
        return lendingRepository.findAll();
    }


    // найти выдачу по id
    public BookLending findById(Long id) {
        return lendingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Выдача с ID " + id + " не найдена"
        ));
    }
    /**
     * Оформить выдачу книги
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
     * Оформить возврат книги
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
     * Список только тех книг, которые сейчас на руках.
     * Нужен для выпадающего списка в блоке "Возврат" на главной странице.
     */
    @Transactional(readOnly = true)
    public List<BookLending> findActiveLendings() {
        return lendingRepository.findAllByReturnDateIsNull();
    }

    //изменить данные выдачи
    @Transactional
    public void updateLending(Long id, Long readerId, Long bookId, LocalDate dateOfIssue, LocalDate returnDate) {
        BookLending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));

        // Если книгу меняют на другую, проверяем, не занята ли новая книга
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
     * Найти все просроченные книги на сегодня (2025 год)
     */
    @Transactional(readOnly = true)
    public List<BookLending> getOverdueLendings() {
        return lendingRepository.findAllByReturnDateIsNullAndDateOfDueBefore(LocalDate.now());
    }

    public BookLending save(BookLending lending) {
        return lendingRepository.save(lending);
    }

    //удалить выдачу
    public void deleteLending(Long id) {
        BookLending lending = findById(id);

        if (lending.getReturnDate() == null) {
            throw new IllegalStateException("Нельзя удалить активную запись: книга еще не возвращена в библиотеку.");
        }
        BookLending bookLending = findById(id); // проверка, что существует
        lendingRepository.delete(bookLending);
    }

}
