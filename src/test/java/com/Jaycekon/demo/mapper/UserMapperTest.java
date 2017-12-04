package com.Jaycekon.demo.mapper;

import com.Jaycekon.demo.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by weijie_huang on 2017/9/7.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("com.Jaycekon.demo.mapper")
public class UserMapperTest {
    @Autowired
    private UserMapper mapper;

    @Test
    public void testInset() {
        User user = new User("Jaycekon","1234","1234","123");
        int i = mapper.insert(user);
        Assert.assertNotEquals(0, i);
    }


    @Test
    public void testSelect(){
        User user = mapper.selectByName("Jaycekon");
        Assert.assertNotEquals(null,user);
    }
}
