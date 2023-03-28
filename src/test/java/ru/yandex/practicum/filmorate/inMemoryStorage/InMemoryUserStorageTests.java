//package ru.yandex.practicum.filmorate.inMemoryStorage;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import ru.yandex.practicum.filmorate.model.User;
//import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@Slf4j
//public class InMemoryUserStorageTests extends InMemoryUserStorage {
//    User user1;
//    User user2;
//
//    InMemoryUserStorage inMemoryUserStorage = new InMemoryUserStorage();
//
//    @BeforeEach
//    void beforeEach() {
//        InMemoryUserStorage.globalId = 1L;
//        inMemoryUserStorage.findAll().clear();
//
//        user1 = new User();
//        user1.setEmail("francesy@gmail.com");
//        user1.setLogin("francesy");
//        user1.setName("Frances");
//        user1.setBirthday(LocalDate.of(1990, 1, 15));
//
//        user2 = new User();
//        user2.setEmail("petrov@yandex.ru");
//        user2.setLogin("vasyapetrov");
//        user2.setName("Vasya");
//        user2.setBirthday(LocalDate.of(1997, 10,8));
//    }
//
//    @Test
//    public void add_shouldReturnValidUserWhenAdd() {
//        inMemoryUserStorage.add(user1);
//
//        assertEquals(user1, inMemoryUserStorage.findUser(1));
//
//        inMemoryUserStorage.add(user2);
//
//        assertEquals(user2, inMemoryUserStorage.findUser(2));
//    }
//
//    @Test
//    public void remove_shouldRemoveUser() {
//
//        assertEquals(0, inMemoryUserStorage.findAll().size());
//
//        inMemoryUserStorage.add(user1);
//
//        assertEquals(user1, inMemoryUserStorage.findUser(1));
//
//        inMemoryUserStorage.remove(user1);
//
//        assertEquals(0, inMemoryUserStorage.findAll().size());
//    }
//
//    @Test
//    public void update_shouldUpdateUser() {
//        inMemoryUserStorage.add(user1);
//
//        assertEquals(user1, inMemoryUserStorage.findUser(user1.getId()));
//
//        user1.setName("Изольда");
//
//        inMemoryUserStorage.update(user1);
//        User user = inMemoryUserStorage.findUser(user1.getId());
//
//        assertEquals("Изольда", user.getName());
//    }
//
//    @Test
//    public void findAll_shouldReturnCorrectUsersSize() {
//        inMemoryUserStorage.add(user1);
//        inMemoryUserStorage.add(user2);
//
//        assertEquals(2, inMemoryUserStorage.findAll().size());
//    }
//}
