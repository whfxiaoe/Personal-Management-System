package com.example.pms.controller;

import com.example.pms.entity.Person;
import com.example.pms.repository.PersonRepository;
import com.example.pms.util.MKOResponse;
import com.example.pms.util.MKOResponseCode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pms")
public class PersonController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(PersonController.class.getName());
    @Autowired
    PersonRepository personRepository;
    @Autowired
    EntityManager entityManager;

    public PersonController() {
    }

    @GetMapping("login")
    public MKOResponse login(@RequestParam String tel, @RequestParam String password) {
        try {
            Person person = this.personRepository.chooseTPS(tel, password);
            return person == null ? makeResponse(MKOResponseCode.DataNotFound, "", "用户名或密码错误或已禁用") : makeSuccessResponse("登录成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误！");
        }
    }

    @GetMapping("info")
    public MKOResponse info(@RequestParam String tel) {
        try {
            Person person = this.personRepository.chooseT(tel);
            return person == null ? makeResponse(MKOResponseCode.DataNotFound, "", "查不到信息") : makeSuccessResponse(person);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return this.makeBussessErrorResponse("未知错误！");
        }
    }

    @GetMapping("list")
    public MKOResponse list(@RequestParam(defaultValue = "3") Integer state,
                            @RequestParam(defaultValue = "") String nameTel) {
        try {
            String sel = "select * from info where 1 = 1 ";
            if (state != 3) {
                sel = sel + "AND state = " + state + " ";
            }

            if (nameTel.length() != 0) {
                sel = sel + "AND (name like '%" + nameTel + "%' OR tel like '%" + nameTel + "%') ";
            }

            Query queryC = this.entityManager.createNativeQuery(sel);
            List<Map<String, Object>> result = ((SQLQuery)queryC.unwrap(SQLQuery.class)).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
            return makeSuccessResponse(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误");
        }
    }

    @GetMapping("delete")
    public MKOResponse delete(@RequestParam String tel) {
        try {
            Person person = personRepository.chooseT(tel);
            if (person == null) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "用户名或密码错误或已禁用");
            }
            this.personRepository.delete(person);
            return makeSuccessResponse("已删除");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误");
        }
    }

    @PostMapping("add")
    public MKOResponse add(@RequestBody Person personData) {
        try {
            Person person = new Person();
            person.setName(personData.getName());
            person.setAge(personData.getAge());
            person.setSex(personData.getSex());
            person.setTel(personData.getTel());
            person.setPassword(personData.getPassword());
            person.setRole(personData.getRole());
            person.setState(personData.getState());
            person.setGmtCreate(new Date());
            personRepository.saveAndFlush(person);
            return makeSuccessResponse("已添加");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误");
        }
    }

    @PostMapping("update")
    public MKOResponse update(@RequestBody Person personData) {
        try {

            Person person = new Person();
            person.setName(personData.getName());
            person.setAge(personData.getAge());
            person.setSex(personData.getSex());
            person.setTel(personData.getTel());
            person.setPassword(personData.getPassword());
            person.setRole(personData.getRole());
            person.setState(personData.getState());
            person.setGmtCreate(new Date());
            return this.makeSuccessResponse("已添加");
        } catch (Exception var3) {
            var3.printStackTrace();
            logger.error(var3.getMessage());
            return this.makeBussessErrorResponse("未知错误");
        }
    }
}

