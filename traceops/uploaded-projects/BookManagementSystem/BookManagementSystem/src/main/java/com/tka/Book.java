package com.tka;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Book {
    @Id
    @Column(name = "book_id")
    private int id;
    @Column(unique = true,nullable = false)
    private String title;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private String genre;
}
