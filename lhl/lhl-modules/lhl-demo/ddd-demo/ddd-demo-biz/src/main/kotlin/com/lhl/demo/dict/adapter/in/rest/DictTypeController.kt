package com.lhl.demo.dict.adapter.`in`.rest

import com.lhl.demo.dict.api.model.DictTypeCond
import com.lhl.demo.dict.api.model.DictTypeCreateVO
import com.lhl.demo.dict.api.model.DictTypeDTO
import com.lhl.demo.dict.api.model.DictTypeUpdateVO
import com.lhl.demo.dict.api.model.DictTypeView
import com.lhl.demo.dict.app.command.DictTypeCmd
import com.lhl.demo.dict.app.port.`in`.DictTypeQueryCase
import com.lhl.demo.dict.app.port.`in`.DictTypeUseCase
import com.lhl.demo.dict.app.query.DictTypeQuery
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
 * 字典类型 REST 控制器
 *
 * 只注入 UseCase / QueryCase，不含任何业务逻辑。
 */
@Tag(name = "数据字典-字典类型", description = "字典类型的增删改查与状态变更")
@RestController
@RequestMapping("/api/v1/dict/types")
@PrimaryAdapter
class DictTypeController(
    private val useCase: DictTypeUseCase,
    private val queryCase: DictTypeQueryCase,
) {

    @Operation(summary = "新增字典类型", description = "字典类型编码需全局唯一")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "创建成功"),
        ApiResponse(responseCode = "400", description = "参数校验失败或编码重复"),
    )
    @PostMapping
    fun create(@RequestBody @Valid vo: DictTypeCreateVO): DictTypeDTO = useCase.handle(vo.toCommand())

    @Operation(summary = "修改字典类型")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "修改成功"),
        ApiResponse(responseCode = "400", description = "参数校验失败"),
        ApiResponse(responseCode = "404", description = "字典类型不存在"),
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "字典类型 ID", required = true, example = "1") @PathVariable id: Long,
        @RequestBody @Valid vo: DictTypeUpdateVO,
    ): DictTypeDTO = useCase.handle(vo.toCommand(id))

    @Operation(summary = "删除字典类型", description = "类型下若存在字典项则禁止删除")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "删除成功"),
        ApiResponse(responseCode = "404", description = "字典类型不存在"),
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @Parameter(description = "字典类型 ID", required = true) @PathVariable id: Long,
    ) = useCase.handle(DictTypeCmd.Delete(id))

    @Operation(summary = "字典类型状态变更")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "状态变更成功"),
        ApiResponse(responseCode = "400", description = "状态值非法"),
        ApiResponse(responseCode = "404", description = "字典类型不存在"),
    )
    @PatchMapping("/{id}/status")
    fun changeStatus(
        @Parameter(description = "字典类型 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "目标状态", required = true, example = "DISABLED") @RequestParam status: String,
    ): DictTypeDTO = useCase.handle(DictTypeCmd.ChangeStatus(id, status))

    @Operation(summary = "按 ID 查询字典类型")
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "字典类型 ID", required = true) @PathVariable id: Long,
    ): DictTypeView? = queryCase.handle(DictTypeQuery.GetQuery(id))

    @Operation(summary = "分页查询字典类型")
    @GetMapping
    fun page(
        @Parameter(description = "字典类型编码") @RequestParam(required = false) dictCode: String?,
        @Parameter(description = "名称关键字") @RequestParam(required = false) name: String?,
        @Parameter(description = "状态") @RequestParam(required = false) status: String?,
        @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") pageNum: Int,
        @Parameter(description = "每页条数", example = "10") @RequestParam(defaultValue = "10") pageSize: Int,
    ) = queryCase.handle(
        DictTypeQuery.PageQuery(DictTypeCond(dictCode, name, status), pageNum, pageSize)
    )
}
