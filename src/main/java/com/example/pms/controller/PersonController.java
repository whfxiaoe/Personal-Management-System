package com.example.pms.controller;

import com.example.pms.entity.Person;
import com.example.pms.repository.PersonRepository;
import com.example.pms.util.MKOResponse;
import com.example.pms.util.MKOResponseCode;
import java.util.Date;
import java.util.HashMap;
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

    /**
     * @Description: 用户登录
     * @Param: tel(String)     password(String)
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/07
     */
    @GetMapping("login")
    public MKOResponse login(@RequestParam String tel, @RequestParam String password) {
        try {
            Person person = personRepository.chooseTPS(tel, password);
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", person.getId());
            hashMap.put("role", person.getRole());
            return person == null ? makeResponse(MKOResponseCode.DataNotFound, "", "用户名或密码错误或已禁用") : makeSuccessResponse(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误！");
        }
    }

    /**
     * @Description: 用户详情
     * @Param: tel(String)
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/07
     */
    @GetMapping("info")
    public MKOResponse info(@RequestParam Integer id) {
        try {
            Person person = personRepository.chooseById(id);
            if (person == null) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "查不到信息");
            }
            person.setPassword("******");
            return makeSuccessResponse(person);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误！");
        }
    }


    /**
     * @Description: 用户列表
     * @Param:  tel(String)     state(Integer)
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/07
     */
    @GetMapping("list")
    public MKOResponse list(@RequestParam(defaultValue = "3") Integer state,
                            @RequestParam(defaultValue = "") String nameTel) {
        try {
            String sel = "select id,name,sex,age,tel,state,role,gmtCreate from info where 1 = 1 ";
            if (state != 3) {
                sel = sel + "AND state = " + state + " ";
            }

            if (nameTel.length() != 0) {
                sel = sel + "AND (name like '%" + nameTel + "%' OR tel like '%" + nameTel + "%') ";
            }

            Query queryC = entityManager.createNativeQuery(sel);
            List<Map<String, Object>> result = ((SQLQuery)queryC.unwrap(SQLQuery.class)).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();
            return makeSuccessResponse(result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误");
        }
    }

    /**
     * @Description: 删除用户
     * @Param:  id
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/07
     */
    @GetMapping("delete")
    public MKOResponse delete(@RequestParam Integer id) {
        try {
            Person person = personRepository.chooseById(id);
            if (person == null) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "用户名或密码错误或已禁用");
            }
            personRepository.delete(person);
            return makeSuccessResponse("已删除");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误");
        }
    }


    /**
     * @Description: 添加用户
     * @Param:
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/07
     */
    @PostMapping("add")
    public MKOResponse add(@RequestBody Person personData) {
        try {
            if(personData.getTel() == null || personData.getTel().length() != 11){
                return makeResponse(MKOResponseCode.DataFormatError,"","手机号格式不正确");
            }
            if(personRepository.validateTel(personData.getTel()) != null)
            {
                return makeResponse(MKOResponseCode.DataExist,"","手机号已存在");
            }
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

    /**
     * @Description: 修改用户信息
     * @Param:
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/08
     */
    @PostMapping("update")
    public MKOResponse update(@RequestBody Person personData) {
        try {
            if(personData.getId() == null){
                return makeResponse(MKOResponseCode.ParamsLack,"","缺少参数[id]");
            }

            Person person = personRepository.chooseById(personData.getId());
            if(person == null){
                return makeResponse(MKOResponseCode.DataNotFound,"","此[id]无数据");
            }

            person.setName(personData.getName());
            person.setAge(personData.getAge());
            person.setSex(personData.getSex());
            person.setPassword(personData.getPassword());
            person.setRole(personData.getRole());
            person.setState(personData.getState());
            person.setGmtCreate(new Date());
            personRepository.saveAndFlush(person);
            return makeSuccessResponse("已修改");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误");
        }
    }

    /**
     * @Description: 切换启用禁用状态
     * @Param:  state(Integer)      id
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/08
     */
    @GetMapping("swich")
    public MKOResponse swich(@RequestParam Integer state,
                             @RequestParam Integer id){
        try {
            Person person = personRepository.chooseById(id);
            person.setState(state);
            person.setGmtCreate(new Date());
            personRepository.saveAndFlush(person);
            return makeSuccessResponse("已修改");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误");
        }
    }
}

