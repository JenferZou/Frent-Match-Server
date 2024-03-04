package com.jenfer.frentmatch.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jenfer.frentmatch.model.domain.User;
import com.jenfer.frentmatch.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "0 0 0 * * ?")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("redisson:lock:recommend:user");
        try {
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                Page<User> page = userService.page(new Page<User>(1, 10), userLambdaQueryWrapper);
                String redisKey = String.format("main:recommend:user:%s", mainUserList);
                ValueOperations valueOperations = redisTemplate.opsForValue();
                try {
                    valueOperations.set(redisKey,page,30000, TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    log.error("缓存推荐用户失败", e);
                }
            }

        }catch (Exception e){
            log.error("获取redis锁失败", e);
        }finally {
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }

        }

    }


}
