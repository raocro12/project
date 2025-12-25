package ru.study.my_project.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.study.my_project.model.Book;
import ru.study.my_project.model.BookLending;
import ru.study.my_project.model.Reader;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы с сущностью BookLending.
 * Интерфейс предоставляет стандартные CRUD операции.
 *
 */
@Repository
public interface BookLendingRepository extends JpaRepository<BookLending, Long> {

    //дата возврата - нулл, не возвращенная книга
    boolean existsByBookIdAndReturnDateIsNull(Long bookId);

    // Найти все записи о выдаче конкретному читателю
    List<BookLending> findAllByReaderId(Long readerId);

    // Найти все книги, которые еще не вернули (returnDate IS NULL)
    List<BookLending> findAllByReturnDateIsNull();

    // Найти просроченные книги (дата возврата null и срок сдачи прошел)
    // Используем встроенный механизм запросов Spring Data
    List<BookLending> findAllByReturnDateIsNullAndDateOfDueBefore(java.time.LocalDate today);

    List<BookLending> findByDateOfIssueBetween(
            LocalDate start,
            LocalDate end
    );

    boolean existsByReaderAndBookAndDateOfIssue(@NotNull(message = "Читатель должен быть указан") Reader reader, @NotNull(message = "Книга должна быть указана") Book book, @NotNull(message = "Дата выдачи обязательна") @PastOrPresent(message = "Дата выдачи не может быть в будущем") LocalDate dateOfIssue);

    boolean existsByReaderIdAndReturnDateIsNull(Long id);
}
