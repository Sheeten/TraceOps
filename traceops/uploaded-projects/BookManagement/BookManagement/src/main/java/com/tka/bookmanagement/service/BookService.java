package com.tka.bookmanagement.service;

import com.tka.bookmanagement.entity.Book;

import java.util.List;

public interface BookService {
    String addBook(Book book);
    List<Book> getBook(int id);
    List<Book> getAllBook();
    Book updateBook(int id,Book book);
    String deleteBook(int id);
}
