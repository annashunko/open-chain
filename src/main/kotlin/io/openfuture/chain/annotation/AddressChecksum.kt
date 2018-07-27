package io.openfuture.chain.annotation

import io.openfuture.chain.validation.AddressChecksumValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AddressChecksumValidator::class])
annotation class AddressChecksum(
    val message: String = "AddressChecksum is not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)