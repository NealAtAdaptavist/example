/**
 * When all the subtasks of an issue are in a status then move the parent issue to that status
 */

import com.atlassian.jira.issue.MutableIssue
import utils.HelperFunctions

def ACTION_ID = 21
def STATUS_NAME = "Done"

def issue = issue as MutableIssue

if (! issue.isSubTask())
    return

def parentIssue = issue.parentObject
parentIssue.findAll {it.status.name == STATUS_NAME}?.size() + 1 == parentIssue.subTaskObjects.size() ?
    new HelperFunctions().transitIssue(parentIssue, ACTION_ID) : null