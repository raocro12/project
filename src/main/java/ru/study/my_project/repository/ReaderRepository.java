package ru.study.my_project.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.study.my_project.model.Reader;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Репозиторий для работы с сущностью Reader.
 * Интерфейс предоставляет стандартные CRUD операции.
 *
 */
@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    /**
     * Поиск читателя по уникальному номеру билета
     * @param readersTicket
     * @return Optional<Reader>
     */
    Optional<Reader> findByReadersTicket(String readersTicket);

    /**
     * Поиск всех однофамильцев
     * @param lastName
     * @return List<Reader>
     */
    List<Reader> findAllByLastNameIgnoreCase(String lastName);

    /**
     * Проверка на существование читательского билета
     * @param readersTicket
     * @return bool
     */
    boolean existsByReadersTicket(String readersTicket);

    /**
     * Проверка на существование по email
     * @param email
     * @return bool
     */
    boolean existsByEmail(String email);


    /**
     * Проверка по Фамилии, имени и дате рождения
     * @param firstName
     * @param lastName
     * @param dateOfBirth
     * @return bool
     */
    boolean existsByFirstNameAndLastNameAndDateOfBirth(
            String firstName,
            String lastName,
            LocalDate dateOfBirth
    );

    /**
     * Проверка по фамилии, имени и дате рождения, исключая данное id
     * @param firstName
     * @param lastName
     * @param dateOfBirth
     * @param id
     * @return bool
     */
    boolean existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(@NotBlank(message = "Имя обязательно") @Pattern(regexp = "^[А-ЯЁ][а-яё-]{1,20}$",
            message = "Имя должно начинаться с заглавной буквы и содержать только кириллицу") String firstName, @NotBlank(message = "Фамилия обязательна") @Pattern(regexp = "^[А-ЯЁ][а-яё-]{1,20}$",
            message = "Фамилия должна начинаться с заглавной буквы и содержать только кириллицу") String lastName, @NotNull(message = "Дата рождения обязательна") @Past(message = "Дата рождения должна быть в прошлом") LocalDate dateOfBirth, Long id);
}
