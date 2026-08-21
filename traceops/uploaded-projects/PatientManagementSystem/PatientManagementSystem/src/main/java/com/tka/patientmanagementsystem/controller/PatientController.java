package com.tka.patientmanagementsystem.controller;

import com.tka.patientmanagementsystem.entity.Patient;
import com.tka.patientmanagementsystem.serviceImpl.PatientServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@CrossOrigin("http://localhost:5173/")
public class PatientController {

    private final PatientServiceImpl patientService;

    public PatientController(PatientServiceImpl patientService){
        this.patientService=patientService;
    }

    @PostMapping("/save")
    public String savePatient(@RequestBody Patient patient){
        return patientService.addPatient(patient);
    }

    @GetMapping("/get/{id}")
    public List<Patient> getPatientById(@PathVariable Long id){
        return patientService.getPatientById(id);
    }

    @GetMapping("/getAll")
    public List<Patient> getAllPatient(){
        return patientService.getAllPatient();
    }

    @PutMapping("/update/{id}")
    public Patient updatePatient(@PathVariable Long id,@RequestBody Patient patient){
        return patientService.updatePatient(id,patient);
    }

    @DeleteMapping("/delete/{id}")
    public String deletePatient(@PathVariable Long id){
        return patientService.deletePatient(id);
    }
}
