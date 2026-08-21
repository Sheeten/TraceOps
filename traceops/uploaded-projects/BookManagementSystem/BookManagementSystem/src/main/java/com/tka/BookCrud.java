package com.tka;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class BookCrud {
    static Scanner sc = new Scanner(System.in);
    public static void saveBook(){
        SessionFactory sf = HibernateConfig.getFactory();
        Session session = sf.openSession();
        boolean isRunning = true;
        while (isRunning){
            System.out.print("Enter book id : ");
            int id = sc.nextInt();
            sc.nextLine();
            System.out.print("Enter book Title : ");
            String title = sc.nextLine();
            System.out.print("Enter book price : ");
            double price = sc.nextDouble();
            sc.nextLine();
            System.out.print("Enter book genre : ");
            String genre = sc.nextLine();
            Book book = new Book(id,title,price,genre);
            session.persist(book);
            System.out.print("Want to add another book(yes/no) : ");
            String choice = sc.next();
            if (choice.equalsIgnoreCase("no")) isRunning=false;
        }
    }

    public static void getBookById(){
        SessionFactory sf = HibernateConfig.getFactory();
        Session session = sf.openSession();
        Query query = session.createQuery("select b from Book b where Book.id = :id");
        System.out.print("Enter id of book to get : ");
        int id = sc.nextInt();
        Book singleResult = (Book)query.setParameter("id",id).getSingleResult();
        System.out.println(singleResult);
    }

    public static void getAllBook(){
        SessionFactory sf = HibernateConfig.getFactory();
        Session session = sf.openSession();
        List<Book> bookList =session.createQuery("FROM Book ").getResultList();
        System.out.println(bookList);
    }

    public static void updateBookPrice(){
        SessionFactory sf = HibernateConfig.getFactory();
        Session session = sf.openSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("update Book b set b.price= :price where b.id= :id");
    }
}
