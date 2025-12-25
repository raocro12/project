package ru.study.my_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * Сущность Book представляет книгу в библиотечной системе.
 * Содержит информацию об авторе, названии, жанре, издательстве
 * и годе издания. Связана с сущностью BookLending
 * (выдачи книг читателям).
 * Ограничения целостности:
 * - Уникальность книги по полям: name, author, publishingHouse</li>
 * - Проверка корректности года издания</li>
 */
@Getter
@Setter
@Entity
@Table(name = "book", schema = "public", uniqueConstraints = {
        // Соответствует ограничению из бд uq_book_unique_author: ("name", "author", "publishing_house")
        @UniqueConstraint(name = "uq_book_unique_author",
                columnNames = {"name", "author", "publishing_house"})
})
public class Book {

    /**
     * Уникальный идентификатор книги.
     * Генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_book")
    private Long id;

    /**
     * Автор книги.
     * Обязательное поле.
     * Должен быть указан на кириллице.
     */
    @NotBlank(message = "Автор обязателен")
    @Size(max = 100)
    @Pattern(regexp = "^[А-ЯЁ][а-яё]*( [А-ЯЁ][а-яё]*)*$",
            message = "Автор должен быть указан на кириллице (например: Иванов Иван Иванович)")
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    /**
     * Жанр книги.
     * Допускаются только символы кириллицы.
     */
    @Size(max = 50)
    @Pattern(regexp = "^[А-ЯЁа-яё ]*$", message = "Жанр должен содержать только кириллицу")
    @Column(name = "genre", length = 50)
    private String genre;

    /**
     * Название книги.
     * Обязательное поле.
     */
    @NotBlank(message = "Название книги обязательно")
    @Size(max = 225, message = "Название не может быть длиннее 225 символов")
    @Column(name = "name", nullable = false, length = 225)
    private String name;

    /**
     * Издательство.
     * Обязательное поле.
     */
    @Size(max = 50)
    @NotBlank(message = "Издательство обязательно")
    @Column(name = "publishing_house", nullable = false, length = 50)
    private String publishingHouse;

    /**
     * Год издания книги.
     * Должен содержать ровно 4 цифры
     * и не превышать текущий год.
     */
    @Column(name = "year_of_publication")
    private Integer yearOfPublication;

    /**
     * Список выдач данной книги читателям.
     * Связь один-ко-многим с сущностью BookLending.
     */
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<BookLending> lendings;

    /**
     * Метод для проверки года издания перед сохранением и изменением.
     * Приводит значение к строковому типу и проверяет,
     * что в нем ровно 4 цифры.
     * Проверяет, что число не больше текущего года.
     *
     * @throws IllegalStateException если год некорректен
     *
     */
    @PrePersist
    @PreUpdate
    public void validateYear() {
        if (yearOfPublication != null) {
            String yearStr = String.valueOf(yearOfPublication);
            if (!yearStr.matches("^\\d{4}$")) {
                throw new IllegalStateException("Год издания должен содержать ровно 4 цифры");
            }

            int currentYear = java.time.Year.now().getValue();
            if (yearOfPublication > currentYear) {
                throw new IllegalStateException("Год издания не может быть в будущем");
            }
        }
    }

    public Book(){
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
