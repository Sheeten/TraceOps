package com.tka.studentmanagement;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {
    List<Student> studentList = new ArrayList<>();

    @PostMapping("/save")
    public String addStudent(@RequestBody Student s){
        System.err.println(s);
        studentList.add(s);

        return "student added successfully";
    }

    @GetMapping("/getStudent")
    public List<Student> getStudentList() {
        studentList.add(new Student(1,"Sheeten",21));
        studentList.add(new Student(2,"Chetna",20));
        return studentList;
    }

    @GetMapping("/byid/{id}")
    public Student getStudentById(@PathVariable int id){
        for (Student s : studentList){
            if (s.getId()==id){
                return s;
            }
        }
        return null;
    }

    @PutMapping("/edit/{sid}")
    public Student updateStudent(@PathVariable int sid,@RequestParam int age){
        for (Student student : studentList){
            if (student.getId()==sid){
                student.setAge(age);
                return student;
            }
        }
        return null;
    }

    @DeleteMapping("/remove")
    public String deleteStudent(@RequestParam int id){
        for (Student student : studentList){
            if (student.getId()==id){
                studentList.remove(student);
                return "student deleted";
            }
        }
        return null;
    }
}
