/**
 * A custom script post function that when there is an issue A transition then all the issues
 * then all the linked with it, via a <LINK_TYPE> link type, issues will transit
 * through the action with id <ACTION_ID>
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.workflow.TransitionOptions
import utils.HelperFunctions

final def LINK_TYPE = "Epic-Story Link"
final def ACTION_ID = 21

def issue = issue as MutableIssue

ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.id)?.each { it ->
    if (it.issueLinkType.name == LINK_TYPE) {

        //if this link is an epic story link them transit the story to in progress
        def destinationObject = it.destinationObject
        new HelperFunctions().transitIssue(destinationObject, ACTION_ID)
    }
}