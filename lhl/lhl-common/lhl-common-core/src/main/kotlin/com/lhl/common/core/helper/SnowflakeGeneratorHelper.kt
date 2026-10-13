package com.lhl.common.core.helper

import cn.hutool.v7.core.data.id.IdUtil

/**
 * 雪花算法ID生成器辅助类
 *
 * 基于Hutool工具库实现的雪花算法ID生成器，用于生成全局唯一的Long类型ID。
 * 雪花算法生成的ID具有以下特点：
 * - 全局唯一性
 * - 趋势递增
 * - 包含时间戳信息
 */
object SnowflakeGeneratorHelper {
    /**
     * 生成雪花算法ID
     *
     * @param workerId 工作机器ID，默认值为1，取值范围0-31
     * @param dataCenterId 数据中心ID，默认值为1，取值范围0-31
     * @return 生成的Long类型唯一ID
     */
    fun generate(workerId: Long = 1, dataCenterId: Long = 1): Long {
        return IdUtil.getSnowflake(workerId, dataCenterId).next()
    }
}
