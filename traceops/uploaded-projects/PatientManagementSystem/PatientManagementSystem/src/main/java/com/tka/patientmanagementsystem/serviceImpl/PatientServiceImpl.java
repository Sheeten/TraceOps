package com.tka.patientmanagementsystem.serviceImpl;

import com.tka.patientmanagementsystem.entity.Patient;
import com.tka.patientmanagementsystem.repositary.PatientRepo;
import com.tka.patientmanagementsystem.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepo patientRepo;


    @Override
    public String addPatient(Patient patient) {
        patientRepo.save(patient);
        return "Patient saved successfully";
    }

    @Override
    public List<Patient> getPatientById(Long id) {
        return patientRepo.getPatientById(id);
    }

    @Override
    public List<Patient> getAllPatient() {
        return patientRepo.findAll();
    }

    @Override
    public Patient updatePatient(Long id, Patient patient) {
        Patient existingPatient = patientRepo.findById(id).orElseThrow(()->new RuntimeException("patient not found"));
        existingPatient.setName(patient.getName());
        existingPatient.setDisease(patient.getDisease());
        patientRepo.save(existingPatient);
        return existingPatient;
    }

    @Override
    public Patient PartialUpdateStudent(Long id, Patient patient) {
        Patient existingPatient = patientRepo.findById(id).orElseThrow(()->new RuntimeException("patient not found"));
        if (existingPatient.getName() != null) {
            existingPatient.setName(patient.getName());
        }
        if (existingPatient.getDisease()!=null){
            existingPatient.setDisease(patient.getDisease());
        }
        patientRepo.save(existingPatient);
        return existingPatient;
    }

    @Override
    public String deletePatient(Long id) {
        patientRepo.deleteById(id);
        return "Patient deleted successfully";
    }
}
