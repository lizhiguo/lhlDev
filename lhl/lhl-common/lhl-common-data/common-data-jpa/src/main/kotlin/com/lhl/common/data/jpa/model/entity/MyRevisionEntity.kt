package com.lhl.common.data.jpa.model.entity

import com.lhl.common.data.jpa.annotation.IdGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionListener
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "revinfo")
@RevisionEntity(MyRevisionListener::class) // 关联监听器来自动填充操作人
class MyRevisionEntity {

    @Id
    @IdGenerator
    @RevisionNumber
    @Column(name = "rev")
    var id: Long = 0

    @RevisionTimestamp
    @Column(name = "revtstmp")
    var timestamp: Long = 0

    // 扩展：记录是谁做的操作
    @Column(name = "operator_id", length = 20, comment = "操作人ID")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    var operatorId: String? = null

    // 扩展：记录是那个IP地址做的操作
    @Column(name = "operator_ip_address", length = 20, comment = "操作人IP地址")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    var operatorIpAddress: String? = null
}

class MyRevisionListener : RevisionListener {
    override fun newRevision(revisionEntity: Any) {
        val entity = revisionEntity as MyRevisionEntity
        // 假设你使用 Spring Security，在这里获取当前用户 ID
        // val currentUserId = SecurityContextHolder.getContext().authentication?.name
        entity.operatorId = "system_or_user_id"
        entity.operatorIpAddress = "127.0.0.1"
    }
}