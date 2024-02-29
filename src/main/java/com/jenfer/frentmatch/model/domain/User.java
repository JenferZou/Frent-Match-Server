package com.jenfer.frentmatch.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;


@TableName(value = "user")
@Data
@ToString
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    @TableField(value = "username")
    private String username;

    /**
     * 账号
     */
    @TableField(value = "userAccount")
    private String userAccount;

    /**
     * 用户头像
     */
    @TableField(value = "avatarUrl")
    private String avatarUrl;


    /**
     * 性别
     */
    @TableField(value = "gender")
    private Integer gender;


    /**
     * 密码
     */
    @TableField(value = "userPassword")
    private String userPassword;


    /**
     * 电话
     */
    @TableField(value = "phone")
    private String phone;


    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;


    /**
     * 状态 0 - 正常
     */
    @TableField(value = "userStatus")
    private Integer userStatus;


    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;


    /**
     *
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField(value = "isDelete")
    private Integer isDelete;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    @TableField(value = "userRole")
    private Integer userRole;

    /**
     * 星球编号
     */
    @TableField(value = "planetCode")
    private String planetCode;


    /**
     * 标签列表 json
     */
    @TableField(value = "tags")
    private String tags;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;



}

