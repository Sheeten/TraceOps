package com.tka.bookmanagement.controller;

import com.tka.bookmanagement.entity.Book;
import com.tka.bookmanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @PostMapping("/save")
    public String saveBook(@RequestBody Book book){
        return bookService.addBook(book);
    }

    @GetMapping("/get/{id}")
    public List<Book> getBookById(@PathVariable int id){
        return bookService.getBook(id);
    }

    @GetMapping("/get/all")
    public List<Book> getAllBook(){
        return bookService.getAllBook();
    }

    @PutMapping("/update/{id}")
    public Book updateBook(@PathVariable int id,@RequestBody Book book){
        return bookService.updateBook(id,book);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteBook(@PathVariable int id){
        return bookService.deleteBook(id);
    }

}
