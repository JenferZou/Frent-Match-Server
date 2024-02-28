package com.jenfer.frentmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jenfer.frentmatch.model.domain.UserTeam;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Jenf
* @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
* @createDate 2024-02-28 22:12:30
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




