package com.todoapp.service;

import com.todoapp.dto.TaskDTO;
import com.todoapp.exception.ResourceNotFoundException;
import com.todoapp.model.Task;
import com.todoapp.model.User;
import com.todoapp.repository.TaskRepository;
import com.todoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setIsCompleted(false);
        task.setUser(user);

        taskDTO = new TaskDTO();
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setIsCompleted(false);
        taskDTO.setUserId(1L);
    }

    @Test
    void testCreateTask_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO result = taskService.createTask(taskDTO);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals(1L, result.getUserId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTask_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(taskDTO);
        });
        
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testGetTaskById_Success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));

        TaskDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
    }

    @Test
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTaskById(1L);
        });
    }

    @Test
    void testGetAllTasks() {
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setIsCompleted(true);
        task2.setUser(user);

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task, task2));

        List<TaskDTO> results = taskService.getAllTasks();

        assertEquals(2, results.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testGetTasksByUserId_Success() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(taskRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(task));

        List<TaskDTO> results = taskService.getTasksByUserId(1L);

        assertEquals(1, results.size());
        assertEquals("Test Task", results.get(0).getTitle());
    }

    @Test
    void testGetTasksByUserId_UserNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksByUserId(1L);
        });
    }

    @Test
    void testUpdateTaskStatus_Success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO result = taskService.updateTaskStatus(1L, true);

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testDeleteTask_Success() {
        when(taskRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(taskRepository).deleteById(anyLong());

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTask_NotFound() {
        when(taskRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(1L);
        });
        
        verify(taskRepository, never()).deleteById(anyLong());
    }
}
