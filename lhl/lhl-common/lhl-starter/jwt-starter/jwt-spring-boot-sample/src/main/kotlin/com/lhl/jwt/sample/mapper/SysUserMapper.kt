package com.lhl.jwt.sample.mapper

import com.lhl.jwt.sample.entity.SysUser
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

/**
 * 用户表读通道。
 *
 * 所有查询都带上 `deleted = 0`，逻辑删除的数据不会进入认证链路。
 */
@Mapper
interface SysUserMapper {

    @Select(
        """
        SELECT id, username, password, mobile, nickname, wx_open_id, status, deleted, create_time, update_time
        FROM sys_user
        WHERE username = #{username} AND deleted = 0
        """
    )
    fun selectByUsername(@Param("username") username: String): SysUser?

    @Select(
        """
        SELECT id, username, password, mobile, nickname, wx_open_id, status, deleted, create_time, update_time
        FROM sys_user
        WHERE mobile = #{mobile} AND deleted = 0
        """
    )
    fun selectByMobile(@Param("mobile") mobile: String): SysUser?

    @Select(
        """
        SELECT id, username, password, mobile, nickname, wx_open_id, status, deleted, create_time, update_time
        FROM sys_user
        WHERE wx_open_id = #{wxOpenId} AND deleted = 0
        """
    )
    fun selectByWxOpenId(@Param("wxOpenId") wxOpenId: String): SysUser?

    @Select(
        """
        SELECT id, username, password, mobile, nickname, wx_open_id, status, deleted, create_time, update_time
        FROM sys_user
        WHERE id = #{id} AND deleted = 0
        """
    )
    fun selectById(@Param("id") id: Long): SysUser?

    @Select(
        """
        SELECT id, username, password, mobile, nickname, wx_open_id, status, deleted, create_time, update_time
        FROM sys_user
        WHERE deleted = 0
        ORDER BY id
        """
    )
    fun selectList(): List<SysUser>

    @Select("SELECT COUNT(1) FROM sys_user WHERE username = #{username}")
    fun countByUsername(@Param("username") username: String): Int

    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{id} AND deleted = 0")
    fun updateStatus(@Param("id") id: Long, @Param("status") status: Int): Int

    /** 逻辑删除，保留行以便审计 */
    @Update("UPDATE sys_user SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    fun logicDelete(@Param("id") id: Long): Int

    @Insert(
        """
        INSERT INTO sys_user (username, password, mobile, nickname, wx_open_id, status, deleted)
        VALUES (#{username}, #{password}, #{mobile}, #{nickname}, #{wxOpenId}, #{status}, #{deleted})
        """
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(user: SysUser): Int
}