package com.petapp.petapp.repository;

import com.petapp.petapp.entity.PetInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PetInfoRepository extends JpaRepository<PetInfo, Long> {

    List<PetInfo> findByPetType(String petType);

    List<PetInfo> findByPetStatus(String petStatus);

    List<PetInfo> findByLocation(String location);

    List<PetInfo> findByEventDate(LocalDate eventDate);

    List<PetInfo> findByPetTypeAndPetStatus(String petType, String petStatus);

    List<PetInfo> findByLocationContainingIgnoreCase(String location);
}
