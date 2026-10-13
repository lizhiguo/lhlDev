package com.lhl.com.lhl.demo

import com.lhl.common.core.helper.toPrettyJsonString
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.TreeMap

private val log = KotlinLogging.logger {}

data class User(
    var id: Int? = null,
    var name: String? = null
)
interface UserService {

    fun findUser(id: Int): User?

    fun findUsers(): Collection<User>

    fun updateUser(id: Int,user: User): User?
}

@Service("userService")
class UserServiceImpl : UserService {
    private val users: MutableMap<Int, User> = TreeMap()

    init {
        users[1] = User(1, "John Coltrane")
        users[2] = User(2, "Miles Davis")
        users[3] = User(3, "Sonny Rollins")
    }

    override fun findUser(id: Int): User? {
        log.debug { "id==$id" }
        return users[id]
    }

    override fun findUsers(): Collection<User> {
        log.debug { users.values.toPrettyJsonString() }
        return users.values
    }

    override fun updateUser(id: Int, user: User): User? {
        log.debug { user.toPrettyJsonString() }
        return users[id]?.apply {
            name = user.name
        }
    }
}