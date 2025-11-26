package org.zen.fortknox.application

import org.zen.fortknox.tools.theme.Theme

/* theme                    -> Application default theme */
/* confirmExit              -> User put some information on form and wants to exit, so user should confirm it first */
/* allowScreenshot          -> Allow user to take screenshot of screens contains sensitive information */
/* lockTimeout              -> How many seconds take to allow user to enter the application passcode after 3 times failed attempt */
data class AppSettings(
    var theme: Theme,
    var confirmExit: Boolean,
    var allowScreenshot: Boolean,
    var lockTimeout: Int
)
