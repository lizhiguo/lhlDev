package com.lhl.jwt.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JwtSampleApplication

fun main(args: Array<String>) {
    runApplication<JwtSampleApplication>(*args)
}