package ru.study.my_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * Сущность BookLending представляет выдачу книги читателю в библиотечной системе.
 * Содержит информацию о книге, читателе, дате выдаче, дате возврата.
 * Связана с сущностями Book, Reader.
 * Определён уникальный ключ: "id_reader", "id_book", "date_of_issue".
 *
 */
@Setter
@Getter
@Entity
@Table(name = "book_lending", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uq_book_lending_unique",
                columnNames = {"id_reader", "id_book", "date_of_issue"})
})
public class BookLending {

    /**
     * Уникальный идентификатор выдачи книги.
     * Генерируется автоматически.
     *
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_book_lending")
    private Long id;

    /**
     * Читатель.
     * Не может быть пустым.
     * Устанавливается связь с другой таблицей - Reader.
     *
     */
    @NotNull(message = "Читатель должен быть указан")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reader", nullable = false, foreignKey = @ForeignKey(name = "fk_reader"))
    private Reader reader;

    /**
     * Книга.
     * Поле не может быть пустым.
     * Устанавливается связь с другой таблицей - Book.
     *
     */
    @NotNull(message = "Книга должна быть указана")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_book", nullable = false, foreignKey = @ForeignKey(name = "fk_book"))
    private Book book;

    /**
     * Дата выдачи книги.
     * Не может быть пустым.
     *
     */
    @NotNull(message = "Дата выдачи обязательна")
    @PastOrPresent(message = "Дата выдачи не может быть в будущем")
    @Column(name = "date_of_issue", nullable = false)
    private LocalDate dateOfIssue;

    /**
     * Предполагаемая дата возврата книги.
     *
     */
    @Column(name = "date_of_due")
    private LocalDate dateOfDue;

    /**
     * Действительная дата возврата книги.
     *
     */
    @PastOrPresent(message = "Дата возврата не может быть в будущем")
    @Column(name = "return_date")
    private LocalDate returnDate;

    public BookLending() {
    }

    /**
     * Автоматическое заполнение дат перед сохранением в БД.
     * Если дата выдачи не задана — ставим текущую,
     * срок возврата — через 14 дней.
     *
     */
    @PrePersist
    public void prePersist() {
        if (this.dateOfIssue == null) {
            this.dateOfIssue = LocalDate.now();
        }
        if (this.dateOfDue == null) {
            this.dateOfDue = this.dateOfIssue.plusDays(14);
        }
    }

    /**
     * Валидация логики дат перед сохранением или обновлением.
     *
     */
    @PreUpdate
    public void validateDates() {
        if (returnDate != null && returnDate.isBefore(dateOfIssue)) {
            throw new IllegalStateException("Дата возврата не может быть раньше даты выдачи");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookLending that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
