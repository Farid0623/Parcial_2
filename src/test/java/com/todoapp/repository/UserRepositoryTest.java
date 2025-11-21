package com.todoapp.repository;

import com.todoapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@SuppressWarnings("null")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
    }

    @Test
    void testSaveUser() {
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
    }

    @Test
    void testFindByEmail() {
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void testExistsByEmail() {
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_NotFound() {
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        assertFalse(exists);
    }

    @Test
    void testDeleteUser() {
        User savedUser = entityManager.persist(user);
        entityManager.flush();

        userRepository.deleteById(savedUser.getId());

        Optional<User> found = userRepository.findById(savedUser.getId());
        assertFalse(found.isPresent());
    }
}
