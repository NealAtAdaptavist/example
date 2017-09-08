/**
 * A script that generates a report with information about the customers.
 * I)   Number of customers
 * II)  Customers that have never logged in
 * III) Customer is jira-sers group (this should always be equal to I)
 * IV)  Customers in jira-users group that have never logged in
 */

import com.atlassian.crowd.manager.directory.DirectoryManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.login.LoginManager

def DIRECTORY_TO_CHECK = "JIRA Internal Directory"
def JIRA_USERS_GROUP = "jira-users"

def loginManager = ComponentAccessor.getComponent(LoginManager)
def directoryManager = ComponentAccessor.getComponent(DirectoryManager)
def groupManager = ComponentAccessor.getGroupManager()
def userManager = ComponentAccessor.getUserManager()

def internalDirectoryId = directoryManager.findAllDirectories()?.find {it.name == DIRECTORY_TO_CHECK}?.id
def jiraUsersGroup = groupManager.getGroup(JIRA_USERS_GROUP)

// Get all users that belong to JIRA Internal Directory
def allInternalDirectoryUsers = userManager.getAllUsers()?.findAll {
    it.directoryId == internalDirectoryId
}

// Get all the users that belong to JIRA Internal Directory and have never logged in
def internalDirectoryUsersNeverLoggedIn = allInternalDirectoryUsers?.findAll {
    loginManager.getLoginInfo(it.username).lastLoginTime == null
}

// Get all the users that belong to JIRA Internal Directory and to jira-users group
def internalUsersBelongToGroup = allInternalDirectoryUsers?.findAll {
    groupManager.isUserInGroup(it, jiraUsersGroup)
}

// Get all the users that belong to JIRA Internal Directory and to jira-users group and have never logged in
def jiraUsersHaveNeverLoggedIn = internalDirectoryUsersNeverLoggedIn?.findAll {
    groupManager.isUserInGroup(it, jiraUsersGroup)
}

"users that belong to JIRA Internal Directory: ${allInternalDirectoryUsers?.size()} <br>" +
"all the users that belong to JIRA Internal Directory and have never logged in: ${internalDirectoryUsersNeverLoggedIn?.size()}<br>"
"all the users that belong to JIRA Internal Directory and to jira-users group: ${internalUsersBelongToGroup?.size()}<br>" +
"all the users that belong to JIRA Internal Directory and to jira-users group and have never logged in: ${jiraUsersHaveNeverLoggedIn?.size()}"