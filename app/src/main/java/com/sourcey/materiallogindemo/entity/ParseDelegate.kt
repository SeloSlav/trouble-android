package com.yitter.android.entity

/**
 * Created by @santafebound on 9/2/2017.
 */

import com.parse.ParseObject
import kotlin.reflect.KProperty

public class ParseDelegate<T> {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(parseObj: ParseObject, propertyMetadata: KProperty<*>): T = parseObj.get(propertyMetadata.name) as T

    operator fun setValue(parseObj: ParseObject, propertyMetadata: KProperty<*>, a: Any?) {
        if (a != null) {
            parseObj.put(propertyMetadata.name, a)
        }
    }
}