package com.lhl.common.data.jpa.helper


import com.lhl.common.core.helper.SnowflakeGeneratorHelper
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator

/**
 * 雪花ID生成
 */
class SnowflakeGenerator : IdentifierGenerator {
    override fun generate(session: SharedSessionContractImplementor?, `object`: Any?): Long {
        return SnowflakeGeneratorHelper.generate()
    }

}
