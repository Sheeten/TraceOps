package com.tka.patientmanagementsystem.repositary;

import com.tka.patientmanagementsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepo extends JpaRepository<Patient,Long> {

    List<Patient> getPatientById(Long id);
}
