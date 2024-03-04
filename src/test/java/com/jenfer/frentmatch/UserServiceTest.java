package com.jenfer.frentmatch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jenfer.frentmatch.model.domain.User;
import com.jenfer.frentmatch.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class UserServiceTest {

   @Resource
   private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    private List<Long> mainUserList = Arrays.asList(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L);
    @Test
    public void testSearchUserByTags() {
        List<String> list = Arrays.asList("大一");
        List<User> users = userService.searchUserByTags(list);
        System.out.println(users);
    }


    @Test
    public void testMainList(){
        String redisKey = String.format("main:recommend:user:%s", mainUserList);
        System.out.println(redisKey);
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Page<User> page = userService.page(new Page<User>(1, 10), userLambdaQueryWrapper);
        ValueOperations valueOperations = redisTemplate.opsForValue();

        try {
            valueOperations.set(redisKey,page,30000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.error("缓存推荐用户失败", e);
        }
    }


}
