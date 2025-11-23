package org.zen.fortknox.tools.theme

enum class Theme {
    System,Light,Dark ;

    fun next(): Theme {
        return when (this) {
            System -> Light
            Light -> Dark
            Dark -> System
        }
    }
}