/**
 * A script that adds the reporter and the request participants of a source issue, as watchers to a target issue.
 * Can be used as a post function when an issue <sourceIssue> gets resolved as duplicate
 * and the duplicate issue is provided as a link <linkedIssues> with link type Duplicate <LINK_TYPE>
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.user.ApplicationUser

def LINK_TYPE = "Duplicate"

def watcherManager = ComponentAccessor.getWatcherManager()
def issueLinkManager = ComponentAccessor.getIssueLinkManager()
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

def sourceIssue = issue as Issue
def linkedIssues = issueLinkManager.getLinkCollection(sourceIssue, currentUser).getOutwardIssues(LINK_TYPE)

// for each duplicate issue it's watchers and participants to the
linkedIssues?.each { linkedIssue ->

    log.debug "Linked issue ${linkedIssue.key}"

// A list of users that will be the watchers of the issue
    List<ApplicationUser> watchers = []
    def reporter = sourceIssue.reporter

    // add the reporter as a watcher
    if (reporter) {
        watchers.add(reporter)
    }

    def requestParticipantsCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Request participants")
    if (requestParticipantsCF) {
        def participants = sourceIssue.getCustomFieldValue(requestParticipantsCF) as List<ApplicationUser>

        // add the participants. if any, as well
        if (participants) {
            watchers.addAll(participants)
        }
    }

    watchers?.each { it ->
        watcherManager.startWatching(it, linkedIssue)
    }

    log.info "${watchers?.join(", ")} added as watchers to target issue ${linkedIssue?.key}"
}