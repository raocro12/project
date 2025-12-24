package ru.study.my_project.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.study.my_project.model.Reader;
import ru.study.my_project.repository.BookLendingRepository;
import ru.study.my_project.repository.ReaderRepository;

import java.util.List;

@Service
@Transactional
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final BookLendingRepository bookLendingRepository;

    public ReaderService(ReaderRepository readerRepository, BookLendingRepository bookLendingRepository) {
        this.readerRepository = readerRepository;
        this.bookLendingRepository = bookLendingRepository;
    }

    //вывести всех читателей
    @Transactional(readOnly = true)
    public List<Reader> findAll() {
        return readerRepository.findAll();
    }

    //поиск по id
    @Transactional(readOnly = true)
    public Reader findById(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Читатель с ID " + id + " не найден"
                ));
    }

    //поиск по фамилии
    public List<Reader> searchByLastName(String lastName) {
        return readerRepository.findAllByLastNameIgnoreCase(lastName);
    }

    //создание нового читателя
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

    //изменить данные читателя
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

    //удалить читателя
    public void deleteReader(Long id) {
        if (bookLendingRepository.existsByReaderIdAndReturnDateIsNull(id)) {
            // Если есть, выбрасываем исключение
            throw new IllegalStateException("Нельзя удалить активного читателя.");
        }
        Reader reader = findById(id); // проверка, что существует
        readerRepository.delete(reader);
    }

}
