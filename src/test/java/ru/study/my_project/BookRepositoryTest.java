package ru.study.my_project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.study.my_project.repository.BookRepository;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// Указываем использовать реальную БД из application.properties, а не встроенную в памяти
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

//    @Autowired
//    private BookRepository readerRepository;
//
//    @Autowired
//    private javax.sql.DataSource dataSource;
//
//    @Test
//    void testDatabaseConnectionAndRead() {
//        try {
//            System.out.println("Подключен к URL: " + dataSource.getConnection().getMetaData().getURL());
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//        // Проверяем, что мы можем просто посчитать записи (тест на наличие таблицы)
//        long count = bookRepository.count();
//        System.out.println("Записей в БД найдено: " + count);
//
//        // Тест пройдет успешно, если метод count() не выдаст ошибку
//        assertThat(count).isNotNull();
//    }
}
