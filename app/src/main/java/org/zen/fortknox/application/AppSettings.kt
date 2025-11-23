package org.zen.fortknox.application

import org.zen.fortknox.tools.theme.Theme

/* Confirm exit             -> User put some information on form and wants to exit, so user should confirm it first */
data class AppSettings(
    var theme: Theme, var confirmExit: Boolean
)
