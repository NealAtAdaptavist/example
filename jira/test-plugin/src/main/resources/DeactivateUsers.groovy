/**
 * A script that deactivates users that belong to the JIRA Internal Directory and have never logged in.
 * It also sends an email to this user because he will not be able to log in again unless we make him active again.
 */

import com.atlassian.crowd.manager.directory.DirectoryManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.login.LoginManager
import utils.HelperFunctions

def DIRECTORY_TO_CHECK = "JIRA Internal Directory"
def loginManager = ComponentAccessor.getComponent(LoginManager)
def directoryManager = ComponentAccessor.getComponent(DirectoryManager)
def userManager = ComponentAccessor.getUserManager()

def internalDirectoryId = directoryManager.findAllDirectories()?.find {it.name == DIRECTORY_TO_CHECK}?.id
def neverLoggedInUsers = userManager.getAllApplicationUsers()?.findAll { user ->
    user.directoryId == internalDirectoryId && !loginManager.getLoginInfo(user.username)?.lastLoginTime
}

neverLoggedInUsers?.
    findAll { it.active}?.
    each { user ->
        new HelperFunctions().deactivateUser(user)
        new HelperFunctions().sendEmail(user.emailAddress, "Deactivated", "You account has be de activated")
    }