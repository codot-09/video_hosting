package org.example.telegrambot.service;

import org.example.telegrambot.entity.Book;
import org.example.telegrambot.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void save(Book book) throws Exception {
        bookRepository.save(book);
    }


    public List<Book> findBookByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public long getNumberBooks(){
        return bookRepository.count();
    }

    public List<Book> getAllBooks(){
        return bookRepository.findAll();
    }

    public void deleteBookById(Long bookId){
        bookRepository.deleteById(bookId);
    }
}
