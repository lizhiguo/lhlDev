package com.lhl.common.core.helper


import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import tools.jackson.databind.node.ObjectNode

class JacksonHelperTest {
    companion object{
        private val log = KotlinLogging.logger {}
    }
    @Test
    fun `JacksonHelper 扩展函数综合测试`() {
        // 1. 使用 Mapper 转换列表
        val stringToLengthMapper = Mapper<String, Int> { str -> str.length }
        val strings = listOf("apple", "banana", "cherry")
        val lengths = stringToLengthMapper.mapAll(strings)
        log.debug { "字符串长度: $lengths" }

        // 2. 使用 Mapper 和 Jackson 转换 JSON 字符串列表
        val jsonToPersonMapper = Mapper<String, Person> { json ->
            json.toType<Person>() ?: throw IllegalArgumentException("无法解析 JSON: $json")
        }
        val jsonList = listOf(
            """{"name": "Alice", "age": 25}""",
            """{"name": "Bob", "age": 30}"""
        )
        val people = jsonToPersonMapper.mapAll(jsonList)
        log.debug {"JSON 列表转 Person: $people"} // 输出: JSON 列表转 Person: [Person(name=Alice, age=25), Person(name=Bob, age=30)]
        log.debug {"Person 格式化输出: ${ people.toPrettyJsonString()}"}

        // 3. JSON 字符串转 Map
        val json = """{"name": "Charlie", "age": 35}"""
        val map: Map<String, Any?>? = json.toMap()
        log.debug {"JSON 转 Map: $map"} // 输出: JSON 转 Map: {name=Charlie, age=35}

        // 4. Map 转数据类
        val personFromMap: Person? = map?.toDataClass<Person>()
        log.debug {"Map 转 Person: $personFromMap"}// 输出: Map 转 Person: Person(name=Charlie, age=35)

        // 5. 对象转 ObjectNode
        val personObj = Person("David", 40)
        val objectNode: ObjectNode? = personObj.toObjectNode()
        log.debug {"Person 转 ObjectNode: $objectNode"} // 输出: Person 转 ObjectNode: {"name":"David","age":40}

        // 6. JSON 字符串转 Collection
        val jsonArray = """[{"name": "Eve", "age": 45}, {"name": "Frank", "age": 50}]"""
        val collection: Collection<Person>? = jsonArray.toCollection<Person>()
        log.debug {"JSON 转 Collection: $collection"} // 输出: JSON 转 Collection: [Person(name=Eve, age=45), Person(name=Frank, age=50)]

        // 7. 对象转格式化 JSON
        val prettyJson = personObj.toJsonString(pretty = true)
        log.debug {"Person 转格式化 JSON: $prettyJson"}
        // 输出:
        // Person 转格式化 JSON: {
        //   "name" : "David",
        //   "age" : 40
        // }

        // 8. 复杂泛型类型（使用 TypeReference）
        val complexJson = """[{"name": "Grace", "age": 55}]"""
        val listType = object : tools.jackson.core.type.TypeReference<List<Person>>() {}
        val list: List<Person>? = complexJson.toType(listType)
        log.debug {"JSON 转 List<Person>: $list"} // 输出: JSON 转 List<Person>: [Person(name=Grace, age=55)]

        // 9. 无效 JSON
        val invalidJson = """{"name": "Helen", "age": "invalid"}"""
        val badPerson: Person? = invalidJson.toType<Person>()
        log.debug {"无效 JSON 转 Person: $badPerson"} // 输出: 无效 JSON 转 Person: null（日志记录错误）
    }

    @Test
    fun `JacksonHelper writerWithFields 按字段写出`() {
        val person = Person("中国", 25)
        val json = person.toJsonString()
        log.debug {"Person 转 JSON: $json"}
        val json2 = JacksonHelper.writerWithFields(Person::class.java,"name").writeValueAsString(person)
        log.debug {"Person 转 JSON: $json2"}
    }
}

data class Person(val name: String, val age: Int)