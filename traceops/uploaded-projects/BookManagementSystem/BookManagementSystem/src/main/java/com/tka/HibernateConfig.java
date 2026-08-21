package com.tka;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateConfig {
    public static SessionFactory getFactory(){
        return new Configuration().configure()
                .addAnnotatedClass(Book.class)
                .buildSessionFactory();
    }
}
