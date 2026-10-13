package com.lhl.common.data.jpa.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.envers.Audited
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractAuditableJpaEntity<E, ID> :
    BaseEntity<E, ID>()  where E : AbstractAuditableJpaEntity<E, ID>{
    @Schema(description = "创建时间", hidden = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Audited
    @CreationTimestamp
    @Column(name = "create_time", nullable = true, updatable = false, comment = "创建时间")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    var createTime: LocalDateTime? = null

    @Schema(description = "更新时间", hidden = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Audited
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false, updatable = true, comment = "更新时间")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    var updateTime: LocalDateTime? = null

    @Schema(description = "创建人", hidden = true)
    @CreatedBy
    @Column(name = "create_by", length = 20, insertable = true, updatable = false, comment = "创建人")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    var createBy: String? = null

    @LastModifiedBy
    @Column(name = "update_by", length = 20, insertable = true, updatable = true, comment = "更新人")
    @Schema(description = "更新人", hidden = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    var updateBy: String? = null
}