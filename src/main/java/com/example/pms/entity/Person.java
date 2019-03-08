package com.example.pms.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@ToString
@Getter
@Setter
@Entity
@Table(name = "info")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String tel;                     //电话(账号)
    private String password;
    private String name;
    private Integer age;
    private Integer sex;                    //性别：1男；0女
    private Integer state;                  //状态：1启用；0禁用

    private Integer role;                   //角色：0普通用户，1管理员
    private Date gmtCreate;                  //创建日期

}
