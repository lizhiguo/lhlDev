package com.lhl.com.lhl.demo

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class DemoCamelRouter : RouteBuilder() {
    @Throws(Exception::class)
    override fun configure() {
        errorHandler(deadLetterChannel("direct:error"))
        /*onException(Exception::class.java)
            .handled(true)
            .log(LoggingLevel.ERROR, "Error processing exchange: \${exception.message}")*/

        from("direct:get-user").routeId(UserService::class.java.simpleName+".findUser")
            .to("bean:userService?method=findUser(\${body})")
            .setBody(simple("\${body}"))

        from("direct:get-users").routeId(UserService::class.java.simpleName+".findUsers")
            .to("bean:userService?method=findUsers")
            .setBody(simple("\${body}"))


        from("direct:update-user")
            .routeId(UserService::class.java.simpleName+".updateUser")
            /*.process {exchange ->
                val headers = exchange.getIn().headers
                val user = exchange.getIn().getBody(User::class.java)
                val userId = headers["userId"] as? Int
                exchange.getIn().body = arrayOf(userId, user)
            }*/
            .to("bean:userService?method=updateUser(\${header.userId}, \${body})")
            .description("根据用户ID，跟新用户信息")
    }
}