package com.lhl.com.lhl.demo

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.springframework.web.bind.annotation.*
import java.io.Serializable
import java.math.BigDecimal
import java.util.*

@RestController
class SimpleController {

    @EndpointInject("direct:add-product")
    lateinit var productProducer: ProducerTemplate

    @EndpointInject("direct:add-products")
    lateinit var productsProducer: ProducerTemplate

    @Operation(summary = "添加单个产品信息")
    @PutMapping("/products/{sku}")
    fun addProduct(@RequestBody product: Product, @PathVariable("sku") sku: String): String {
        productProducer.sendBody(product)
        return ""
    }

    @Operation(summary = "添加一堆产品信息")
    @PostMapping("/products")
    fun addProducts(@RequestBody products: List<Product>): String {
        productsProducer.sendBody(products)
        return ""
    }

    data class Product(val id: UUID = UUID.randomUUID(), val sku:String, val exchange:String, val price: BigDecimal, val published:Boolean = false):
        Serializable

}

@Tag(name = "User操作")
@RestController
@RequestMapping("user")
class UserController(val userService: UserService,val producerTemplate: ProducerTemplate) {

    companion object{
        private const val UPDATE_USER = "direct:update-user"
    }

    @EndpointInject("direct:get-user")
    lateinit var getUserTemplate: ProducerTemplate

    @EndpointInject("direct:get-users")
    lateinit var getUsersTemplate: ProducerTemplate

    @Operation(summary = "根据用户ID,获得用户信息")
    @GetMapping("/{id}")
    fun getUser(@PathVariable("id") id: Int): User? {
        return  getUserTemplate.requestBody(id, User::class.java)
    }

    @Operation(summary = "根据用户ID,跟新用户信息")
    @PostMapping("/{id}")
    fun updateUser(@PathVariable("id") id: Int, @RequestBody user: User): User? {
        val headers = mapOf("userId" to id)
        return producerTemplate.requestBodyAndHeaders(UPDATE_USER, user, headers, User::class.java)
    }

    @Operation(summary = "获得用户列表")
    @GetMapping("/all")
    fun getUsers(): List<User>? {
        val result = null
        // 尝试将结果转换为 List<User>
        @Suppress("UNCHECKED_CAST")
        return result as? List<User>
    }

    @Operation(summary = "用户异常信息")
    @GetMapping("/ex")
    fun getEx(): List<User>? {
        val result = null
        throw ArithmeticException("不允许除以零。")
    }

    data class Product(val id: UUID = UUID.randomUUID(), val sku:String, val exchange:String, val price: BigDecimal, val published:Boolean = false):
        Serializable

}