package com.jenfer.frentmatch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jenfer.frentmatch.mapper.UserTeamMapper;
import com.jenfer.frentmatch.model.domain.UserTeam;
import com.jenfer.frentmatch.service.UserTeamService;

import org.springframework.stereotype.Service;

/**
* @author Jenf
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-02-28 22:12:30
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




