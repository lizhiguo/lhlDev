package com.lhl.demo.dict.adapter.`in`.rest

import com.lhl.demo.dict.api.model.DictItemCond
import com.lhl.demo.dict.api.model.DictItemCreateVO
import com.lhl.demo.dict.api.model.DictItemDTO
import com.lhl.demo.dict.api.model.DictItemUpdateVO
import com.lhl.demo.dict.api.model.DictItemView
import com.lhl.demo.dict.app.command.DictItemCmd
import com.lhl.demo.dict.app.port.`in`.DictItemQueryCase
import com.lhl.demo.dict.app.port.`in`.DictItemUseCase
import com.lhl.demo.dict.app.query.DictItemQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.jmolecules.architecture.hexagonal.PrimaryAdapter
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * 字典项 REST 控制器
 */
@Tag(name = "数据字典-字典项", description = "字典项的增删改查、按类型查询与状态变更")
@RestController
@RequestMapping("/api/v1/dict/items")
@PrimaryAdapter
class DictItemController(
    private val useCase: DictItemUseCase,
    private val queryCase: DictItemQueryCase,
) {

    @Operation(summary = "新增字典项", description = "同一字典类型下字典项编码需唯一")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "创建成功"),
        ApiResponse(responseCode = "400", description = "参数校验失败或编码重复"),
    )
    @PostMapping
    fun create(@RequestBody @Valid vo: DictItemCreateVO): DictItemDTO = useCase.handle(vo.toCommand())

    @Operation(summary = "修改字典项")
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "字典项 ID", required = true) @PathVariable id: Long,
        @RequestBody @Valid vo: DictItemUpdateVO,
    ): DictItemDTO = useCase.handle(vo.toCommand(id))

    @Operation(summary = "删除字典项")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@Parameter(description = "字典项 ID", required = true) @PathVariable id: Long) =
        useCase.handle(DictItemCmd.Delete(id))

    @Operation(summary = "字典项状态变更")
    @PatchMapping("/{id}/status")
    fun changeStatus(
        @Parameter(description = "字典项 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "目标状态", required = true, example = "DISABLED") @RequestParam status: String,
    ): DictItemDTO = useCase.handle(DictItemCmd.ChangeStatus(id, status))

    @Operation(summary = "按 ID 查询字典项")
    @GetMapping("/{id}")
    fun get(@Parameter(description = "字典项 ID", required = true) @PathVariable id: Long): DictItemView? =
        queryCase.handle(DictItemQuery.GetQuery(id))

    @Operation(summary = "按字典类型查询字典项列表（翻译用）")
    @GetMapping
    fun listByType(
        @Parameter(description = "字典类型编码", required = true, example = "gender") @RequestParam dictTypeCode: String,
        @Parameter(description = "状态过滤") @RequestParam(required = false) status: String?,
    ): List<DictItemView>? =
        queryCase.handle(DictItemQuery.ListQuery(DictItemCond(dictTypeCode = dictTypeCode, status = status)))

    @Operation(summary = "分页查询字典项")
    @GetMapping("/page")
    fun page(
        @Parameter(description = "字典类型编码") @RequestParam(required = false) dictTypeCode: String?,
        @Parameter(description = "字典项编码") @RequestParam(required = false) itemCode: String?,
        @Parameter(description = "名称关键字") @RequestParam(required = false) itemName: String?,
        @Parameter(description = "状态") @RequestParam(required = false) status: String?,
        @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") pageNum: Int,
        @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") pageSize: Int,
    ) = queryCase.handle(
        DictItemQuery.PageQuery(DictItemCond(dictTypeCode, itemCode, itemName, status), pageNum, pageSize)
    )
}
