package com.tka.patientmanagementsystem.service;

import com.tka.patientmanagementsystem.entity.Patient;

import java.util.List;

public interface PatientService {
    String addPatient(Patient patient);
    List<Patient> getPatientById(Long id);
    List<Patient> getAllPatient();
    Patient updatePatient(Long id,Patient patient);
    Patient PartialUpdateStudent(Long id,Patient patient);
    String deletePatient(Long id);
}
