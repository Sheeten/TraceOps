package com.tka;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Scanner;

public class HibernateMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning){
            System.out.println("1.To add Book : ");
            System.out.println("2.To Get Book By Id : ");
            System.out.println("3.To Get all Book : ");
            System.out.println("4.To Update Book price : ");
            System.out.println("5.To delete Book : ");
            System.out.println("6.To Get Book by genre : ");
            System.out.println("7.To Get Book By Alphabet : ");
            System.out.print("Enter choice : ");
            int choice = sc.nextInt();
            switch (choice){
                case 1 :

            }
        }
    }
}
