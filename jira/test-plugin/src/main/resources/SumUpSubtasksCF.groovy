/**
 * Scenario: A scripted field in the parent issue that holds the sum of
 * it's subtasks (with name Sales Product) custom field values.
 * Searcher: Number
 * Template: Number
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue

enableCache = {-> false}

def issue = issue as Issue

// get the custom field with name Product Price and type Number
def cf = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Product Price")

// if the issue is subtask do nothing - we only care for parent issues
if (issue.isSubTask())
    return null

// get all the subtasks with issue type name Sales Product
def salesProductSubtasks = issue.getSubTaskObjects()?.findAll {it.issueType.name == "Sales Product"}
def sum = 0

// sum up their values
salesProductSubtasks?.each { subtask ->
    def cfValue = subtask.getCustomFieldValue(cf) as Double

    // if the value is not null or 0 (Groovy Truth)
    if (cfValue) {
        sum += cfValue
    }
}

sum.toDouble()