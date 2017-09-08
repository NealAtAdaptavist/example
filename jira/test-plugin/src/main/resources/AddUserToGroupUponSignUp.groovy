/**
 * A script listener which upon the creation of a new user,
 * it automatically adds him to the jira-users group.
 */

import com.atlassian.jira.component.ComponentAccessor

final def JIRA_GROUP = "jira-users"

def newUserName = event.asUser.name as String
def user = ComponentAccessor.getUserManager().getUserByName(newUserName)
def groupManager = ComponentAccessor.getGroupManager()

groupManager.addUserToGroup(user, groupManager.getGroup(JIRA_GROUP))

log.info "User ${user} added to group: ${JIRA_GROUP}"