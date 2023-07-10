package dev.redicloud.utils.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

val gson = GsonBuilder()
    .scanInterfaceRoutes("dev.redicloud")
    .fixKotlinAnnotations()
    .serializeNulls()
    .create()

fun GsonBuilder.fixKotlinAnnotations(): GsonBuilder {
    addSerializationExclusionStrategy(object : ExclusionStrategy {

        override fun shouldSkipField(f: FieldAttributes?): Boolean =
            f?.getAnnotation(Expose::class.java)?.serialize == false

        override fun shouldSkipClass(p0: Class<*>?): Boolean =
            p0?.getAnnotation(Expose::class.java)?.serialize == false

    }).addDeserializationExclusionStrategy(object : ExclusionStrategy {

        override fun shouldSkipField(f: FieldAttributes?): Boolean =
            f?.getAnnotation(Expose::class.java)?.deserialize == false

        override fun shouldSkipClass(clazz: Class<*>?): Boolean =
            clazz?.getAnnotation(Expose::class.java)?.deserialize == false

    }).serializeNulls()
    return this
}