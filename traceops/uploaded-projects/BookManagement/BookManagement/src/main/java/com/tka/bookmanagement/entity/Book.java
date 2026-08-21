package com.tka.bookmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    @Id
    @Column(unique = true,nullable = false,name = "Book_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private int price;

    @OneToOne(cascade = CascadeType.ALL)
    private Author author;

    @OneToMany(cascade = CascadeType.ALL)
    List<Customers> customers;
}
