package com.example.pms.repository;

import com.example.pms.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.HashMap;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Query(value = "SELECT id FROM info WHERE tel = ?1 AND password = ?2 AND state = 1", nativeQuery = true)
    Person chooseTPS(String tel, String password);

    @Query(value = "SELECT * FROM info WHERE tel = ?1", nativeQuery = true)
    Person validateTel(String tel);

}
