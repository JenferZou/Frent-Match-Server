package com.jenfer.frentmatch;

import com.jenfer.frentmatch.model.domain.User;
import com.jenfer.frentmatch.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class UserServiceTest {

   @Resource
   private UserService userService;
    @Test
    public void testSearchUserByTags() {
        List<String> list = Arrays.asList("大一");
        List<User> users = userService.searchUserByTags(list);
        System.out.println(users);
    }



}
