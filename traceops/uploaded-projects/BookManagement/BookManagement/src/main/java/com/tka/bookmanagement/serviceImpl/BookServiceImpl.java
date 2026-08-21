package com.tka.bookmanagement.serviceImpl;

import com.tka.bookmanagement.dao.BookRepo;
import com.tka.bookmanagement.entity.Author;
import com.tka.bookmanagement.entity.Book;
import com.tka.bookmanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService{

    @Autowired
    private BookRepo bookRepo;

    @Override
    public String addBook(Book book) {
        bookRepo.save(book);
        return "book saved successfully";
    }

    @Override
    public List<Book> getBook(int id) {
        return bookRepo.getBooksById(id);
    }

    @Override
    public List<Book> getAllBook() {
        return bookRepo.findAll();
    }

    @Override
    public Book updateBook(int id, Book book) {
        Book existingBook = bookRepo.findById(id).orElse(null);
        if (existingBook!=null){
            existingBook.setTitle(book.getTitle());
            existingBook.setPrice(book.getPrice());
            existingBook.getAuthor().setName(book.getAuthor().getName());
            existingBook.getAuthor().setMail(book.getAuthor().getMail());
            existingBook.setCustomers(book.getCustomers());
            bookRepo.save(existingBook);
        }
        return existingBook;
    }

    @Override
    public String deleteBook(int id) {
        bookRepo.deleteById(id);
        return "book deleted successfully";
    }
}
