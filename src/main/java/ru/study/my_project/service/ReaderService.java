package ru.study.my_project.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.my_project.model.Reader;
import ru.study.my_project.repository.BookLendingRepository;
import ru.study.my_project.repository.ReaderRepository;

import java.util.List;

/**
 * Сервисный слой для работы с сущностью {@link Reader}.
 *
 * <p>
 * Класс инкапсулирует бизнес-логику управления читателями и взаимодействует
 * с {@link ru.study.my_project.repository.ReaderRepository} и
 * {@link ru.study.my_project.repository.BookLendingRepository}.
 * </p>
 *
 * <p>
 * Основные функции сервиса:
 * </p>
 * <ul>
 *     <li>Получение списка всех читателей</li>
 *     <li>Поиск читателя по идентификатору</li>
 *     <li>Поиск читателей по фамилии</li>
 *     <li>Регистрация нового читателя</li>
 *     <li>Редактирование данных читателя</li>
 *     <li>Удаление читателя с проверкой активных выдач</li>
 * </ul>
 *
 * <p>
 * Все операции выполняются в транзакциях Spring с использованием аннотации
 * {@link org.springframework.transaction.annotation.Transactional}.
 * </p>
 *
 * <p><b>Бизнес-ограничения:</b></p>
 * <ul>
 *     <li>Номер читательского билета должен быть уникальным</li>
 *     <li>Комбинация ФИО и даты рождения должна быть уникальной</li>
 *     <li>Нельзя удалить читателя, у которого есть активные выдачи книг</li>
 * </ul>
 *
 */
@Service
@Transactional
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final BookLendingRepository bookLendingRepository;

    public ReaderService(ReaderRepository readerRepository, BookLendingRepository bookLendingRepository) {
        this.readerRepository = readerRepository;
        this.bookLendingRepository = bookLendingRepository;
    }

    /**
     * Вывести всех читателей
     * @return список читателей
     */
    @Transactional(readOnly = true)
    public List<Reader> findAll() {
        return readerRepository.findAll();
    }

    /**
     * Поиск по id
     * @param id идентификатор читателя
     * @return найденный читатель
     * @throws jakarta.persistence.EntityNotFoundException если читатель не найден
     */
    @Transactional(readOnly = true)
    public Reader findById(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Читатель с ID " + id + " не найден"
                ));
    }

    /**
     * Выполняет поиск читателей по фамилии без учёта регистра.
     *
     * @param lastName фамилия читателя
     * @return список читателей с указанной фамилией
     */
    public List<Reader> searchByLastName(String lastName) {
        return readerRepository.findAllByLastNameIgnoreCase(lastName);
    }

    /**
     * Регистрирует нового читателя в системе.
     *
     * <p>
     * Перед сохранением выполняются проверки:
     * </p>
     * <ul>
     *     <li>уникальность читательского билета</li>
     *     <li>уникальность комбинации ФИО и даты рождения</li>
     * </ul>
     *
     * @param reader данные нового читателя
     * @return сохранённый читатель
     * @throws IllegalStateException если читатель с такими данными уже существует
     */
    @Transactional
    public Reader registerReader(Reader reader) {

        if (readerRepository.existsByReadersTicket(reader.getReadersTicket())) {
            throw new IllegalStateException(
                    "Читательский билет уже существует: " + reader.getReadersTicket()
            );
        }

        if (readerRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                reader.getFirstName(),
                reader.getLastName(),
                reader.getDateOfBirth())) {

            throw new IllegalStateException(
                    "Читатель с таким ФИО и датой рождения уже существует"
            );
        }

        return readerRepository.save(reader);
    }


    /**
     * Обновляет данные существующего читателя.
     *
     * <p>
     * При обновлении выполняется проверка уникальности ФИО и даты рождения
     * с исключением текущего читателя.
     * </p>
     *
     * @param id идентификатор редактируемого читателя
     * @param updatedReader новые данные читателя
     * @return обновлённый читатель
     * @throws IllegalStateException если нарушается уникальность данных
     */
    public Reader updateReader(Long id, Reader updatedReader) {
        if (readerRepository.existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(
                updatedReader.getFirstName(),
                updatedReader.getLastName(),
                updatedReader.getDateOfBirth(),
                id)) {
            throw new IllegalStateException(
                    "Читатель с таким ФИО и датой рождения уже существует"
            );
        }

        Reader existing = findById(id);
        existing.setFirstName(updatedReader.getFirstName());
        existing.setLastName(updatedReader.getLastName());
        existing.setMiddleName(updatedReader.getMiddleName());
        existing.setEmail(updatedReader.getEmail());
        existing.setPhone(updatedReader.getPhone());
        existing.setDateOfBirth(updatedReader.getDateOfBirth());
        existing.setReadersTicket(updatedReader.getReadersTicket());
        return readerRepository.save(existing);
    }

    /**
     * Удаляет читателя из системы.
     *
     * <p>
     * Удаление невозможно, если у читателя имеются активные (не возвращённые) книги.
     * </p>
     *
     * @param id идентификатор читателя
     * @throws IllegalStateException если у читателя есть активные выдачи
     */
    public void deleteReader(Long id) {
        if (bookLendingRepository.existsByReaderIdAndReturnDateIsNull(id)) {
            // Если есть, выбрасываем исключение
            throw new IllegalStateException("Нельзя удалить активного читателя.");
        }
        Reader reader = findById(id); // проверка, что существует
        readerRepository.delete(reader);
    }

}
