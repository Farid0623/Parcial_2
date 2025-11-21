package com.todoapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.dto.TaskDTO;
import com.todoapp.dto.UserDTO;
import com.todoapp.model.Task;
import com.todoapp.model.User;
import com.todoapp.repository.TaskRepository;
import com.todoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class TodoAppE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCompleteUserAndTaskWorkflow() throws Exception {
        UserDTO newUser = new UserDTO();
        newUser.setName("Alice Johnson");
        newUser.setEmail("alice@example.com");

        MvcResult createUserResult = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andReturn();

        String userResponse = createUserResult.getResponse().getContentAsString();
        UserDTO createdUser = objectMapper.readValue(userResponse, UserDTO.class);
        Long userId = createdUser.getId();
        assertNotNull(userId);

        TaskDTO task1 = new TaskDTO();
        task1.setTitle("Buy groceries");
        task1.setDescription("Milk, bread, eggs");
        task1.setIsCompleted(false);
        task1.setUserId(userId);

        MvcResult createTask1Result = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andExpect(jsonPath("$.isCompleted").value(false))
                .andReturn();

        String task1Response = createTask1Result.getResponse().getContentAsString();
        TaskDTO createdTask1 = objectMapper.readValue(task1Response, TaskDTO.class);
        Long task1Id = createdTask1.getId();

        TaskDTO task2 = new TaskDTO();
        task2.setTitle("Finish project report");
        task2.setDescription("Complete the Q4 report");
        task2.setIsCompleted(false);
        task2.setUserId(userId);

        MvcResult createTask2Result = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Finish project report"))
                .andReturn();

        String task2Response = createTask2Result.getResponse().getContentAsString();
        TaskDTO createdTask2 = objectMapper.readValue(task2Response, TaskDTO.class);
        Long task2Id = createdTask2.getId();

        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists());

        Map<String, Boolean> statusUpdate = new HashMap<>();
        statusUpdate.put("isCompleted", true);

        mockMvc.perform(patch("/api/tasks/" + task1Id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(true));

        Task updatedTask = taskRepository.findById(task1Id).orElse(null);
        assertNotNull(updatedTask);
        assertTrue(updatedTask.getIsCompleted());

        mockMvc.perform(delete("/api/tasks/" + task2Id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Buy groceries"))
                .andExpect(jsonPath("$[0].isCompleted").value(true));

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        assertEquals(1, userRepository.count());
        assertEquals(1, taskRepository.count());

        User finalUser = userRepository.findById(userId).orElse(null);
        assertNotNull(finalUser);
        assertEquals("Alice Johnson", finalUser.getName());
        assertEquals("alice@example.com", finalUser.getEmail());

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());

        assertEquals(0, userRepository.count());
        assertEquals(0, taskRepository.count());
    }

    @Test
    void testErrorHandling() throws Exception {
        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 9999"));

        mockMvc.perform(get("/api/tasks/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 9999"));

        TaskDTO invalidTask = new TaskDTO();
        invalidTask.setTitle("Invalid Task");
        invalidTask.setDescription("Should fail");
        invalidTask.setUserId(9999L);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 9999"));

        UserDTO user1 = new UserDTO();
        user1.setName("Bob Smith");
        user1.setEmail("bob@example.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        UserDTO user2 = new UserDTO();
        user2.setName("Bob Jones");
        user2.setEmail("bob@example.com");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email bob@example.com already exists"));
    }
}
