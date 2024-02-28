package com.jenfer.frentmatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jenfer.frentmatch.model.domain.Team;
import org.apache.ibatis.annotations.Mapper;


/**
* @author Jenf
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2024-02-28 22:12:30
*/

@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}




