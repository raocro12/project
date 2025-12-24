package ru.study.my_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "book", schema = "public", uniqueConstraints = {
        // Соответствует ограничению из бд uq_book_unique_author: ("name", "author", "publishing_house")
        @UniqueConstraint(name = "uq_book_unique_author",
                columnNames = {"name", "author", "publishing_house"})
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_book")
    private Long id;

    @NotBlank(message = "Автор обязателен")
    @Size(max = 100)
    // Регулярное выражение из вашего SQL: разрешает слова с большой буквы через пробел
    @Pattern(regexp = "^[А-ЯЁ][а-яё]*( [А-ЯЁ][а-яё]*)*$",
            message = "Автор должен быть указан на кириллице (например: Иванов Иван Иванович)")
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Size(max = 50)
    @Pattern(regexp = "^[А-ЯЁа-яё ]*$", message = "Жанр должен содержать только кириллицу")
    @Column(name = "genre", length = 50)
    private String genre;

    @NotBlank(message = "Название книги обязательно")
    @Size(max = 225, message = "Название не может быть длиннее 225 символов")
    @Column(name = "name", nullable = false, length = 225)
    private String name;

    @Size(max = 50)
    @Column(name = "publishing_house", length = 50)
    private String publishingHouse;

    @Column(name = "year_of_publication")
    private Integer yearOfPublication;

    // Связь с выдачами книг
    @OneToMany(mappedBy = "book",  fetch = FetchType.LAZY)
    private List<BookLending> lendings;

    // Валидация года издания перед сохранением
    @PrePersist
    @PreUpdate
    public void validateYear() {
        if (yearOfPublication != null) {
            String yearStr = String.valueOf(yearOfPublication);
            // Проверяем, что в строке ровно 4 символа и все они цифры
            if (!yearStr.matches("^\\d{4}$")) {
                throw new IllegalStateException("Год издания должен содержать ровно 4 цифры");
            }

            // Проверяем, что дата публикации не больше текущей
            int currentYear = java.time.Year.now().getValue();
            if (yearOfPublication > currentYear) {
                throw new IllegalStateException("Год издания не может быть в будущем");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
