package com.example.springassessment.controller;

import com.example.springassessment.model.Book;
import com.example.springassessment.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.example.springassessment.exception.ResourceNotFoundException;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_ENDPOINT = "/api/books";

    private Book book1, book2;
    private final List<Book> bookList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bookRepo.deleteAll();

        book1 = Book.builder()
                .author("J.D Salinger")
                .title("Catcher in the Rye")
                .build();

        book2 = Book.builder()
                .author("J.K. Rowling")
                .title("Harry Potter")
                .build();

        bookList.add(book1);
        bookList.add(book2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void allBooks() throws Exception {
        bookRepo.saveAll(bookList);

        ResultActions resultActions = mockMvc.perform(get(API_ENDPOINT));

        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(bookList.size())));

    }


    @Test
    void createBook() throws Exception {
        bookRepo.save(book1);
        String requestBody = objectMapper.writeValueAsString(book1);

        ResultActions resultActions = mockMvc.perform(post(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        resultActions.andExpect(status().isCreated())
                .andDo(print())
                .andExpect(jsonPath("$.author").value(book1.getAuthor()))
                .andExpect(jsonPath("$.title").value(book1.getTitle()))
                .andExpect(result -> assertNotNull(result.getResponse().getContentAsString())); //not null
    }

    @Test
    void updateById() throws Exception {
        bookRepo.save(book1);
        Book updateBook1 = bookRepo.findById(book1.getId()).get();

        book1.setAuthor("Updated author");
        book1.setTitle("Updated title");

        //converts Java object into a JSON string
        String requestBody = objectMapper.writeValueAsString(updateBook1);

        ResultActions resultActions = mockMvc.perform(put(API_ENDPOINT.concat("/{id}"), updateBook1.getId())
                .contentType(MediaType.APPLICATION_JSON).content(requestBody));

        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.author").value(updateBook1.getAuthor()))
                .andExpect(jsonPath("$.title").value(updateBook1.getTitle()))
                .andExpect(result -> assertNotNull(result.getResponse().getContentAsString())); //not null
    }

    @Test
    void deleteById() throws Exception {
        bookRepo.saveAll(bookList);
        Book deleteBook1 = bookRepo.findById(book1.getId()).get();

        String expectedResponse = String.format("%s deleted successfully", deleteBook1.getTitle());

        ResultActions resultActions = mockMvc.perform(delete(API_ENDPOINT.concat("/{id}"), deleteBook1.getId()));

        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(result -> assertEquals(expectedResponse, result.getResponse().getContentAsString()));

    }

    @Test
    void getById() throws Exception {
        bookRepo.saveAll(bookList);
        ResultActions resultActions = mockMvc.perform(get(API_ENDPOINT.concat("/{id}"), book1.getId()));

        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.author").value(book1.getAuthor()))
                .andExpect(jsonPath("$.title").value(book1.getTitle()))
                .andExpect(result -> assertNotNull(result.getResponse().getContentAsString())); //not null
    }


    @Test
    void resourceNotFoundException() throws Exception {
        bookRepo.saveAll(bookList);
        Long erroneousId = 1000L;

        ResultActions resultActions = mockMvc.perform(get(API_ENDPOINT.concat("/{id}"), erroneousId));
        resultActions.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(content().string("{\"error\":\"Resource not found.\"}"))
                .andExpect(result -> assertInstanceOf(ResourceNotFoundException.class, result.getResolvedException()));
    }

    @Test
    void countBooks() throws Exception {
        // arrange - setup precondition
        bookRepo.saveAll(bookList);
        long count = bookRepo.count();

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("total", count);

        String expectedString = objectMapper.writeValueAsString(expectedResponse);

        // act - action or behaviour to test (http://localhost:8080/api/customers/count

        ResultActions resultActions = mockMvc.perform(get(API_ENDPOINT.concat("/count")));

        // assert - verify the output

        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(result -> assertEquals(expectedString, result.getResponse().getContentAsString()));
    }
}