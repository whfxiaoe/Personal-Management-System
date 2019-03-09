package com.example.pms.controller;

import com.example.pms.entity.Person;
import com.example.pms.repository.PersonRepository;
import com.example.pms.util.MKOResponse;
import com.example.pms.util.MKOResponseCode;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.mysql.cj.protocol.Resultset;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.intellij.lang.annotations.Language;
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
            if(person == null) {
                return makeResponse(MKOResponseCode.DataNotFound, "", "用户名或密码错误或已禁用");
            }
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", person.getId());
            hashMap.put("role", person.getRole());
            return makeSuccessResponse(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return makeBussessErrorResponse("未知错误！");
        }
    }

    /**
     * @Description: 用户详情
     * @Param: id(Integer)
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
     * @Param:  tel(String)     state(Integer)      page(Integer)      count(Integer)
     * @return:
     * @Author: xiaoe
     * @Date: 2019/03/07-09
     */
    @GetMapping("list")
    public MKOResponse list(@RequestParam(defaultValue = "3") Integer state,
                            @RequestParam(defaultValue = "") String nameTel,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer count) {
        try {
            String scount = "SELECT COUNT(*) FROM info WHERE 1 = 1 ";
            String sel = "SELECT id,name,sex,age,tel,state,role,gmt_create FROM info WHERE 1 = 1 ";
            if (state != 3) {
                sel += "AND state = " + state + " ";
                scount += "AND state = " + state + " ";
            }

            if (nameTel.length() != 0) {
                sel += "AND (name LIKE '%" + nameTel + "%' OR tel LIKE '%" + nameTel + "%') ";
                scount += "AND (name LIKE '%" + nameTel + "%' OR tel LIKE '%" + nameTel + "%') ";
            }
            sel += "LIMIT "+(page-1)*count +"," + count;
            Query queryC = entityManager.createNativeQuery(sel);
            Query queryX = entityManager.createNativeQuery(scount);

            //判断是否可以转换
            @SuppressWarnings("unchecked")
            Map<String, BigInteger> si = (Map<String,BigInteger>)((SQLQuery)queryX.unwrap(SQLQuery.class)).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getSingleResult();
            List<Map<String, Object>> list = ((SQLQuery)queryC.unwrap(SQLQuery.class)).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).getResultList();

            Map<String,Object> result = new HashMap<>();
            //当前页数
            result.put("page",page);
            //总页数
            result.put("allPage",(si.get("COUNT(*)").intValue()-1)/count+1);
            //每页数量
            result.put("count",count);
            //总数量
            result.put("countNum",si.get("COUNT(*)"));
            //数据
            result.put("data",list);
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
                return makeResponse(MKOResponseCode.DataNotFound, "", "查无数据无需删除");
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
            if(personData.getTel() == null && personData.getTel().length() == 0){
                return makeResponse(MKOResponseCode.ParamsLack,"","缺少参数[tel]");
            }
            if(personData.getPassword() == null && personData.getPassword().length() == 0){
                return makeResponse(MKOResponseCode.ParamsLack,"","缺少参数[password]");
            }
            Person person = new Person();
            person.setAge(personData.getAge());
            person.setName(personData.getName());

            person.setTel(personData.getTel());
            person.setPassword(personData.getPassword());

            person.setSex(personData.getSex() == null? 1: personData.getSex());
            person.setRole(personData.getRole() == null? 0: personData.getRole());
            person.setState(1);
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

            person.setName(personData.getName() == null? person.getName() : personData.getName());
            person.setAge(personData.getAge() == null? person.getAge() : personData.getAge());
            person.setPassword(personData.getPassword() == null? person.getPassword() : personData.getPassword());

            person.setSex(personData.getSex() == null? 1: personData.getSex());
            person.setRole(personData.getRole() == null? 0: personData.getRole());
            person.setState(personData.getState() == null? 1:personData.getState());
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
            if(person == null){
                return makeResponse(MKOResponseCode.DataNotFound,"","此[id]无数据");
            }
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

