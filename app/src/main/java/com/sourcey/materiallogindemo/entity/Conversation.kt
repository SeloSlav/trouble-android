package com.yitter.android.entity

import com.parse.ParseClassName
import com.parse.ParseFile
import com.parse.ParseObject

/**
 * Created by @santafebound on 2015-11-07.
 */

@ParseClassName("Conversation")
class Conversation : ParseObject() {

    /* Contact Content */
    val contact by ParseDelegate<Contact>()
    val previewText by ParseDelegate<String>()

}