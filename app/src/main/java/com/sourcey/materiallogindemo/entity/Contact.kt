package com.yitter.android.entity

import com.parse.ParseClassName
import com.parse.ParseFile
import com.parse.ParseObject

/**
 * Created by @santafebound on 2015-11-07.
 */

@ParseClassName("Contact")
class Contact : ParseObject() {

    /* Contact Content */
    val name by ParseDelegate<String>()
    val profilePicture by ParseDelegate<ParseFile>()
    val badgeIcon by ParseDelegate<String>()

}