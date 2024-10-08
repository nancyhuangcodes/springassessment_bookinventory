package com.example.springassessment.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import com.example.springassessment.exception.ResourceNotFoundException;
import com.example.springassessment.model.Book;
import com.example.springassessment.repository.BookRepository;
import com.example.springassessment.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@Validated
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<Object> allBooks(){
        List<Book> bookList=bookService.findAllBooks();
        if(bookList.isEmpty()){
            throw new ResourceNotFoundException();
        }
        return new  ResponseEntity<> (bookList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {

        Book book = bookService.findBookById(id).orElseThrow(ResourceNotFoundException::new);
        return new  ResponseEntity<> (book, HttpStatus.OK);    }

    @PostMapping
    public ResponseEntity<Object> createBook(@Valid @RequestBody Book book) {
        return new ResponseEntity<>(bookService.saveBook(book), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateBookById(@PathVariable("id") Long bookId, @RequestBody @Valid Book book){
        Book updateBook = bookService.findBookById(bookId).map(b -> {
            b.setAuthor(book.getAuthor());
            b.setTitle(book.getTitle());
            return bookService.saveBook(b);
        }).orElseThrow(ResourceNotFoundException::new);
        return new ResponseEntity<>(updateBook, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteBookById (@PathVariable("id")Long bookId){

        Book deleteBook = bookService.findBookById(bookId).map(b->{
            bookService.deleteBookById(b.getId());
            return b;
        }).orElseThrow(ResourceNotFoundException::new);

        String response = String.format("%s deleted successfully", deleteBook.getTitle());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<Object> countCustomer (){

        long count = bookService.count();

        if(count <= 0)
            return new ResponseEntity<>("Book not found.", HttpStatus.NOT_FOUND);

        Map<String, Object> totalBooks = new HashMap<String, Object>();
        totalBooks.put("total", count);

        return new ResponseEntity<>(totalBooks, HttpStatus.OK);
    }
}