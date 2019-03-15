package com.example.buyonline.repository;

import com.example.buyonline.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersonRepository extends JpaRepository<Goods, Integer> {

    @Query(value = "SELECT * FROM info WHERE tel = ?1 AND password = ?2", nativeQuery = true)
    Goods chooseTP(String tel, String password);

    @Query(value = "SELECT * FROM info WHERE tel = ?1", nativeQuery = true)
    Goods validateTel(String tel);

    @Query(value = "SELECT * FROM info WHERE id = ?1", nativeQuery = true)
    Goods chooseById(Integer id);

}
