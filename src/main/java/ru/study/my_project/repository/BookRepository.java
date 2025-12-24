package ru.study.my_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.study.my_project.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    //найти все книги по названию
    List<Book> findByNameContainingIgnoreCase(String name);

    // Найти все книги конкретного автора
    List<Book> findAllByAuthorContainingIgnoreCase(String author);

    // Найти книги по жанру
    List<Book> findAllByGenre(String genre);

    //проверка уникальности
    boolean existsByNameAndAuthorAndPublishingHouse(
            String name,
            String author,
            String publishingHouse
    );
}
