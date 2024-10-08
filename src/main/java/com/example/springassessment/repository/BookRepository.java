package com.example.springassessment.repository;

import com.example.springassessment.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book,Long> {

    // save() -- save() method is also equivalent to performing an update
// findOne()
// findById()
// findByEmail()
// findAll()
// count()
// delete()
// deleteById()
}