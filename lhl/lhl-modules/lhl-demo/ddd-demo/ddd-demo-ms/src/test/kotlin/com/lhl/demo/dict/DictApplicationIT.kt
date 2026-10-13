package com.lhl.demo.dict

import com.lhl.common.core.exception.BizException
import com.lhl.demo.dict.api.model.DictItemCond
import com.lhl.demo.dict.api.model.DictTypeCond
import com.lhl.demo.dict.app.command.DictItemCmd
import com.lhl.demo.dict.app.command.DictTypeCmd
import com.lhl.demo.dict.app.port.`in`.DictItemQueryCase
import com.lhl.demo.dict.app.port.`in`.DictItemUseCase
import com.lhl.demo.dict.app.port.`in`.DictTypeQueryCase
import com.lhl.demo.dict.app.port.`in`.DictTypeUseCase
import com.lhl.demo.dict.app.query.DictItemQuery
import com.lhl.demo.dict.app.query.DictTypeQuery
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * 数据字典模块集成测试（H2 内存库，覆盖 CRUD 与查询双通道）
 */
@SpringBootTest
class DictApplicationIT {

    @Autowired
    private lateinit var dictTypeUseCase: DictTypeUseCase

    @Autowired
    private lateinit var dictItemUseCase: DictItemUseCase

    @Autowired
    private lateinit var dictTypeQueryCase: DictTypeQueryCase

    @Autowired
    private lateinit var dictItemQueryCase: DictItemQueryCase

    @Test
    fun `字典类型 CRUD 与查询`() {
        val created = dictTypeUseCase.handle(
            DictTypeCmd.Create(dictCode = "gender", name = "性别", description = "测试", status = "ENABLED")
        )
        assertNotNull(created.id)
        assertEquals("gender", created.dictCode)

        val updated = dictTypeUseCase.handle(
            DictTypeCmd.Modify(entityId = created.id!!, name = "性别(改)", status = "DISABLED")
        )
        assertEquals("性别(改)", updated.name)
        assertEquals("DISABLED", updated.status)

        val statusChanged = dictTypeUseCase.handle(DictTypeCmd.ChangeStatus(created.id!!, "ENABLED"))
        assertEquals("ENABLED", statusChanged.status)

        val byId = dictTypeQueryCase.handle(DictTypeQuery.GetQuery(created.id!!))
        assertNotNull(byId)
        assertEquals("gender", byId!!.dictCode)

        val page = dictTypeQueryCase.handle(DictTypeQuery.PageQuery(DictTypeCond(dictCode = "gender"), 1, 10))
        assertNotNull(page)
        assertTrue(page!!.total >= 1L)

        dictTypeUseCase.handle(DictTypeCmd.Delete(created.id!!))
        assertNull(dictTypeQueryCase.handle(DictTypeQuery.GetQuery(created.id!!)))
    }

    @Test
    fun `字典项 CRUD 与按类型查询`() {
        val type = dictTypeUseCase.handle(DictTypeCmd.Create(dictCode = "color", name = "颜色"))
        val item = dictItemUseCase.handle(
            DictItemCmd.Create(dictTypeCode = "color", itemCode = "red", itemName = "红色", sort = 1)
        )
        assertEquals("red", item.itemCode)

        val list = dictItemQueryCase.handle(DictItemQuery.ListQuery(DictItemCond(dictTypeCode = "color")))
        assertNotNull(list)
        assertTrue(list!!.any { it.itemCode == "red" })

        val page = dictItemQueryCase.handle(DictItemQuery.PageQuery(DictItemCond(dictTypeCode = "color"), 1, 10))
        assertTrue(page!!.total >= 1L)

        dictItemUseCase.handle(DictItemCmd.Delete(item.id!!))
        dictTypeUseCase.handle(DictTypeCmd.Delete(type.id!!))
    }

    @Test
    fun `字典类型编码唯一性校验`() {
        val t = dictTypeUseCase.handle(DictTypeCmd.Create(dictCode = "uniq", name = "唯一"))
        try {
            val ex = assertThrows(BizException::class.java) {
                dictTypeUseCase.handle(DictTypeCmd.Create(dictCode = "uniq", name = "唯一2"))
            }
            assertEquals("DICT_0002", ex.code)
        } finally {
            dictTypeUseCase.handle(DictTypeCmd.Delete(t.id!!))
        }
    }
}
