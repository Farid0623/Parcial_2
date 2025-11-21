package com.todoapp.repository;

import com.todoapp.model.Task;
import com.todoapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@SuppressWarnings("null")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        entityManager.persist(user);

        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setIsCompleted(false);
        task.setUser(user);
    }

    @Test
    void testSaveTask() {
        Task savedTask = taskRepository.save(task);

        assertNotNull(savedTask.getId());
        assertEquals("Test Task", savedTask.getTitle());
        assertEquals(user.getId(), savedTask.getUser().getId());
    }

    @Test
    void testFindByUserId() {
        entityManager.persist(task);
        entityManager.flush();

        List<Task> tasks = taskRepository.findByUserId(user.getId());

        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    @Test
    void testFindByUserIdAndIsCompleted() {
        Task completedTask = new Task();
        completedTask.setTitle("Completed Task");
        completedTask.setDescription("Done");
        completedTask.setIsCompleted(true);
        completedTask.setUser(user);

        entityManager.persist(task);
        entityManager.persist(completedTask);
        entityManager.flush();

        List<Task> completedTasks = taskRepository.findByUserIdAndIsCompleted(user.getId(), true);
        List<Task> incompleteTasks = taskRepository.findByUserIdAndIsCompleted(user.getId(), false);

        assertEquals(1, completedTasks.size());
        assertEquals(1, incompleteTasks.size());
        assertTrue(completedTasks.get(0).getIsCompleted());
        assertFalse(incompleteTasks.get(0).getIsCompleted());
    }

    @Test
    void testDeleteTask() {
        Task savedTask = entityManager.persist(task);
        entityManager.flush();

        taskRepository.deleteById(savedTask.getId());

        List<Task> tasks = taskRepository.findByUserId(user.getId());
        assertEquals(0, tasks.size());
    }
}
