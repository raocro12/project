package ru.study.my_project.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.my_project.model.Book;
import ru.study.my_project.repository.BookLendingRepository;
import ru.study.my_project.repository.BookRepository;

import java.util.List;

@Service
@Transactional
public class BookService {
    private final BookRepository bookRepository;
    private final BookLendingRepository bookLendingRepository;

    public BookService(BookRepository bookRepository, BookLendingRepository bookLendingRepository) {

        this.bookRepository = bookRepository;
        this.bookLendingRepository = bookLendingRepository;
    }

    //найти по id
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));
    }

    //найти по автору
    @Transactional(readOnly = true)
    public List<Book> findByAuthor(String author) {

        return bookRepository.findAllByAuthorContainingIgnoreCase(author);
    }

    // найти по жанру
    public List<Book> findByGenre(String genre) {

        return bookRepository.findAllByGenre(genre);
    }

    //вывести все книги
    @Transactional(readOnly = true)
    public List<Book> findAll() {

        return bookRepository.findAll();
    }

    //регистрация новой книги
    public Book registerBook(@Valid Book book) {

        if (bookRepository.existsByNameAndAuthorAndPublishingHouse(book.getName(), book.getAuthor(), book.getPublishingHouse())) {
            throw new RuntimeException("Книга уже существует ");
        } else {
            return bookRepository.save(book);
        }
    }

    //изменение данных книги
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

    //удалить книгу
    public void deleteBook(Long id) {
        if (bookLendingRepository.existsByBookIdAndReturnDateIsNull(id)) {
            // Если есть, выбрасываем исключение
            throw new IllegalStateException("Нельзя удалить книгу: она сейчас находится на руках у читателя.");
        }
        Book book = findById(id); // проверка, что существует
        bookRepository.delete(book);
    }
}
