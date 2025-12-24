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

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    // Поиск читателя по уникальному номеру билета
    Optional<Reader> findByReadersTicket(String readersTicket);

    // Поиск всех однофамильцев
    List<Reader> findAllByLastNameIgnoreCase(String lastName);

    boolean existsByReadersTicket(String readersTicket);

    boolean existsByEmail(String email);

    Optional<Reader> findByFirstNameAndLastNameAndDateOfBirth(
            String firstName,
            String lastName,
            LocalDate dateOfBirth
    );

    boolean existsByFirstNameAndLastNameAndDateOfBirth(
            String firstName,
            String lastName,
            LocalDate dateOfBirth
    );

    boolean existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(@NotBlank(message = "Имя обязательно") @Pattern(regexp = "^[А-ЯЁ][а-яё-]{1,20}$",
            message = "Имя должно начинаться с заглавной буквы и содержать только кириллицу") String firstName, @NotBlank(message = "Фамилия обязательна") @Pattern(regexp = "^[А-ЯЁ][а-яё-]{1,20}$",
            message = "Фамилия должна начинаться с заглавной буквы и содержать только кириллицу") String lastName, @NotNull(message = "Дата рождения обязательна") @Past(message = "Дата рождения должна быть в прошлом") LocalDate dateOfBirth, Long id);
}
