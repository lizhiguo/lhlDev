package com.lhl.jwt.sample.mapper

import com.lhl.jwt.sample.entity.SysRole
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

/**
 * 角色 / 权限表读通道。
 */
@Mapper
interface SysRoleMapper {

    /** 查某用户拥有的全部角色与权限 */
    @Select(
        """
        SELECT r.id, r.role_code, r.role_name, r.type, r.remark, r.create_time
        FROM sys_role r
        INNER JOIN sys_user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId}
        ORDER BY r.type, r.id
        """
    )
    fun selectByUserId(@Param("userId") userId: Long): List<SysRole>

    @Select(
        """
        SELECT id, role_code, role_name, type, remark, create_time
        FROM sys_role
        ORDER BY type, id
        """
    )
    fun selectList(): List<SysRole>

    @Select("SELECT id FROM sys_role WHERE role_code = #{roleCode}")
    fun selectIdByCode(@Param("roleCode") roleCode: String): Long?

    @Insert(
        """
        INSERT INTO sys_role (role_code, role_name, type, remark)
        VALUES (#{roleCode}, #{roleName}, #{type}, #{remark})
        """
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(role: SysRole): Int
}

/**
 * 用户与角色关联表。
 */
@Mapper
interface SysUserRoleMapper {

    @Select("SELECT COUNT(1) FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    fun count(@Param("userId") userId: Long, @Param("roleId") roleId: Long): Int

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    fun insert(@Param("userId") userId: Long, @Param("roleId") roleId: Long): Int
}