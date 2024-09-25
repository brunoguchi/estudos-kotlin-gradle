package com.estudos.kotlin.functions

import com.microsoft.azure.functions.ExecutionContext
import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpRequestMessage
import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.HttpStatus
import com.microsoft.azure.functions.annotation.AuthorizationLevel
import com.microsoft.azure.functions.annotation.FunctionName
import com.microsoft.azure.functions.annotation.HttpTrigger
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.minimum
import java.util.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UserProfile(
    val fullName: String,
    val age: Int?
)

class HttpTriggerJava {

    @FunctionName("HttpTriggerJava")
    fun run(
        @HttpTrigger(
            name = "req",
            methods = [HttpMethod.GET, HttpMethod.POST],
            authLevel = AuthorizationLevel.FUNCTION
        ) request: HttpRequestMessage<String>,
        context: ExecutionContext
    ): HttpResponseMessage? {

        try {
            var body = request.getBody().toString()
            val userObj = Json.decodeFromString<UserProfile>(body)

            val validateUser = Validation<UserProfile> {
                UserProfile::fullName {
                    minLength(2)
                    maxLength(100)
                }

                UserProfile::age ifPresent {
                    minimum(0)
                    maximum(150)
                }
            }

            val validationResult = validateUser(userObj)
            val json = Json.encodeToString(UserProfile.serializer(), userObj)

            val myObject = object {
                val isValid: Boolean = validationResult.isValid
                val content: String = json
            }

            return request.createResponseBuilder(HttpStatus.OK).body(myObject).build()
        } catch (e: Exception) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("A wild bad request appears").build()
        }

        // Parse query parameter
//        val query = request.getQueryParameters().get("name")
//        val name = request.getBody()!!.orElse(query)
//
//        if (name == null) {
//            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
//                .body("Please pass a name on the query string or in the request body").build()
//        } else {
//            return request.createResponseBuilder(HttpStatus.OK).body("Hello, $name").build()
//        }
    }
}
