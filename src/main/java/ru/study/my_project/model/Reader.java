package ru.study.my_project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.el.stream.Optional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Сущность Reader представляет читателя.
 * Содержит информацию об ФИО, номере телефона, почте, дате рождения.
 * Определён уникальный ключ: фамилия + имя + дата рождения.
 *
 */
@Getter
@Setter
@Entity
@Table(name = "reader", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uq_reader_unique_name_birth",
                columnNames = {"first_name", "last_name", "date_of_birth"})
})
public class Reader {

    /**
     * Уникальный идентификатор читателя.
     * Генерируется автоматически.
     *
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reader")
    private Long id;

    /**
     * Читательский билет.
     * Обязательное поле.
     *
     */
    @NotBlank(message = "Читательский билет не может быть пустым")
    @Size(max = 20)
    @Column(name = "readers_ticket", unique = true, length = 20)
    private String readersTicket;

    /**
     * Фамилия читателя.
     * Обязательное поле.
     *
     */
    @NotBlank(message = "Фамилия обязательна")
    @Pattern(regexp = "^[А-ЯЁ][а-яё-]{1,20}$",
            message = "Фамилия должна начинаться с заглавной буквы и содержать только кириллицу")
    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;


    /**
     * Отчество читателя.
     *
     */
    @Pattern(regexp = "^[А-ЯЁ][а-яё-]{1,20}$", message = "Отчество должно начинаться с заглавной буквы и содержать только кириллицу")
    @Column(name = "middle_name", length = 20)
    private String middleName;

    /**
     * Имя читателя.
     * Обязательное поле.
     *
     */
    @NotBlank(message = "Имя обязательно")
    @Pattern(regexp = "^[А-ЯЁ][а-яё-]{1,20}$",
            message = "Имя должно начинаться с заглавной буквы и содержать только кириллицу")
    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    /**
     * Дата рождения читателя.
     * Обязательное поле.
     *
     */
    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * Почта читателя.
     *
     */
    @Email(message = "Некорректный формат email")
    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Телефон читателя.
     * Обязательное поле.
     *
     */
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\d{10}$", message = "Телефон должен состоять ровно из 10 цифр")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * Связь "Один ко многим" с таблицей выдачи книг.
     * mappedBy указывает на поле 'reader' в классе BookLending
     *
     */
    @OneToMany(mappedBy = "reader", fetch = FetchType.LAZY)
    private List<BookLending> lendings;

    public Reader() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reader reader)) return false;
        return Objects.equals(id, reader.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
