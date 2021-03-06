package com.example.pms.repository;

import com.example.pms.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Query(value = "SELECT * FROM info WHERE tel = ?1 AND password = ?2", nativeQuery = true)
    Person chooseTP(String tel, String password);

    @Query(value = "SELECT * FROM info WHERE tel = ?1", nativeQuery = true)
    Person validateTel(String tel);

    @Query(value = "SELECT * FROM info WHERE id = ?1", nativeQuery = true)
    Person chooseById(Integer id);

}
