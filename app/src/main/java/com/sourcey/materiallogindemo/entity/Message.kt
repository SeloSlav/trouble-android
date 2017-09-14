package com.yitter.android.entity

import com.parse.ParseClassName
import com.parse.ParseFile
import com.parse.ParseObject

/**
 * Created by @santafebound on 2015-11-07.
 */

@ParseClassName("Message")
class Message : ParseObject() {

    /* Message Pointers */
    val author by ParseDelegate<ParseObject>()
    val contact by ParseDelegate<Contact>()

    /* Message Content */
    val messageText by ParseDelegate<String>()

}