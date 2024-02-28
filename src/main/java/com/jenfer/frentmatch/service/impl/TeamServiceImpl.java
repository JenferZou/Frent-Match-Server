package com.jenfer.frentmatch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jenfer.frentmatch.mapper.TeamMapper;
import com.jenfer.frentmatch.model.domain.Team;
import com.jenfer.frentmatch.service.TeamService;

import org.springframework.stereotype.Service;

/**
* @author Jenf
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-02-28 22:12:30
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




