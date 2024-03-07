package com.jenfer.frentmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jenfer.frentmatch.common.ErrorCode;
import com.jenfer.frentmatch.enums.TeamStatusEnum;
import com.jenfer.frentmatch.exception.BusinessException;
import com.jenfer.frentmatch.mapper.TeamMapper;
import com.jenfer.frentmatch.model.domain.Team;
import com.jenfer.frentmatch.model.domain.User;
import com.jenfer.frentmatch.model.domain.UserTeam;
import com.jenfer.frentmatch.model.dto.TeamQuery;
import com.jenfer.frentmatch.model.request.TeamJoinRequest;
import com.jenfer.frentmatch.model.request.TeamQuitRequest;
import com.jenfer.frentmatch.model.request.TeamUpdateRequest;
import com.jenfer.frentmatch.model.vo.TeamUserVo;
import com.jenfer.frentmatch.model.vo.UserVo;
import com.jenfer.frentmatch.service.TeamService;



import com.jenfer.frentmatch.service.UserService;
import com.jenfer.frentmatch.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author Jenf
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-02-28 22:12:30
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{


    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;


    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long userId = loginUser.getId();
        Integer maxnum = team.getMaxNum();
        if(maxnum<1||maxnum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不符合要求");
        }
        if(StringUtils.isBlank(team.getName())||team.getName().length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名称不符合要求");
        }
        if(StringUtils.isNotBlank(team.getDescription())&&team.getDescription().length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述不符合要求");
        }
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if(statusEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不符合要求");
        }
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(password)||password.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍密码不符合要求");
            }
        }
        if(new Date().after(team.getExpireTime())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍过期时间不符合要求");
        }

        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.eq(Team::getUserId,userId);
        long hasTeamNum = this.count(teamLambdaQueryWrapper);
        if(hasTeamNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已创建队伍数超过5个");
        }
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return teamId;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
       //1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        //2. 不展示已过期的队伍（根据过期时间筛选）
        //3. 可以通过某个关键词同时对名称和描述查询
        //4. 只有管理员才能查看加密还有非公开的房间
        //5. 关联查询已加入队伍的用户信息
        //6. 关联查询已加入队伍的用户信息（可能会很耗费性能，建议大家用自己写 SQL 的方式实现）todo
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(teamQuery!=null){
            Long id = teamQuery.getId();
            if(id!=null&&id>0){
                teamLambdaQueryWrapper.eq(Team::getId,id);
            }
            List<Long> idList = teamQuery.getIdList();
            if(!CollectionUtils.isEmpty(idList) && idList.size()>0){
                teamLambdaQueryWrapper.in(Team::getId,idList);
            }
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                teamLambdaQueryWrapper.and(qw->qw.like(Team::getName,searchText)
                        .or().like(Team::getDescription,searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                teamLambdaQueryWrapper.like(Team::getName,name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                teamLambdaQueryWrapper.like(Team::getDescription,description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum!=null&&maxNum>0){
                teamLambdaQueryWrapper.eq(Team::getMaxNum,maxNum);
            }

            Long userId = teamQuery.getUserId();
            if(userId!=null&&userId>0){
                teamLambdaQueryWrapper.eq(Team::getUserId,userId);
            }

            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if(statusEnum==null){
                    teamLambdaQueryWrapper.eq(Team::getStatus,TeamStatusEnum.PRIVATE);
            }
            if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                    throw new BusinessException(ErrorCode.NO_AUTH,"非管理员不能查看非公开的队伍");
            }
            teamLambdaQueryWrapper.eq(Team::getStatus,statusEnum.getValue());
        }
        teamLambdaQueryWrapper.and(qw->qw.gt(Team::getExpireTime,new Date())
                .or().isNull(Team::getExpireTime));
        List<Team> teamList = this.list(teamLambdaQueryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        ArrayList<TeamUserVo> teamUserVoList = new ArrayList<>();
        for(Team team:teamList){
            Long userId = team.getUserId();
            if(userId==null||userId<=0){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
            if(user!=null){
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user,userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }

        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 只有管理员或者队伍的创建者可以修改
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {

       //其他人、未满、未过期，允许加入多个队伍，但是要有个上限 P0
        //1. 用户最多加入 5 个队伍
        //2. 队伍必须存在，只能加入未满、未过期的队伍
        //3. 不能加入自己的队伍，不能重复加入已加入的队伍（幂等性）
        //4. 禁止加入私有的队伍
        //5. 如果加入的队伍是加密的，必须密码匹配才可以
        //6. 新增队伍 - 用户关联信息
        if(teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();

        if(teamId==null||teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        Team team = this.getById(teamId);
        Date expireTime = team.getExpireTime();
        if(expireTime==null||expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }

        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"私有队伍不允许加入");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password)||!password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }

        Long userId = loginUser.getId();
        if(userId==null||userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //查看登录用户已经加入多少个队伍，超过限制不给加入
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId,userId);
        long hasJoinTeamNum = userTeamService.count(userTeamLambdaQueryWrapper);

        if(hasJoinTeamNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多加入5个队伍");
        }

        userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId,teamId)
                .eq(UserTeam::getUserId,userId);
        long count = userTeamService.count(userTeamLambdaQueryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
        }

        //查看要加入的队伍已经有多少用户加入，超过限定数量不给加入
        userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId,teamId);
        long hasJoinUser = userTeamService.count(userTeamLambdaQueryWrapper);
        if(hasJoinUser>=team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数已满");
        }

        //插入用户队伍信息表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //请求参数：队伍 id
        //1.  校验请求参数
        //2.  校验队伍是否存在
        //3.  校验我是否已加入队伍
        //4.  如果队伍
        //  a.  只剩一人，队伍解散
        //  b.  还有其他人
        //    ⅰ.  如果是队长退出队伍，权限转移给第二早加入的用户 —— 先来后到
        //只用取 id 最小的 2 条数据
        //   	 ⅱ.  非队长，自己退出队伍
        if(teamQuitRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamQuitRequest.getTeamId();
        if(teamId==null||teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }

        Long userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId,teamId)
                .eq(UserTeam::getUserId,userId);
        long count = userTeamService.count(userTeamLambdaQueryWrapper);
        if(count<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户未加入该队伍");
        }

        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);

        if(teamHasJoinNum==1){
            this.removeById(teamId);
        }else {
            if(team.getUserId()==userId){
                LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
                userTeamQueryWrapper.eq(UserTeam::getTeamId, teamId)
                        .orderByAsc(UserTeam::getId);
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList)||userTeamList.size()<=1){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不足，无法解散");
                }
                UserTeam nextUserTeam = userTeamList.get(1);

                Long nextTeamLeaderId  = nextUserTeam.getUserId();

                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);

                if(!result){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新队伍队长失败");
                }
            }

        }
        return userTeamService.remove(userTeamLambdaQueryWrapper);
    }

    @Override
    public boolean deleteTeam(long id, User loginUser) {
        Team team = getTeamById(id);
        Long teamId = team.getId();

        if(team.getUserId()!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        boolean result = userTeamService.remove(userTeamLambdaQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除队伍关联信息失败");
        }
        return this.removeById(teamId);

    }


    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        return userTeamService.count(userTeamLambdaQueryWrapper);
    }



}




