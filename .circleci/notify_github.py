#!/usr/bin/env python3

from os import environ
from datetime import datetime
from github import Github
import re
import sys

REPOSITORY = environ.get("CIRCLE_PROJECT_REPONAME", "timo-reymann/intellij-mjml-support")
PREFIX = "[NEW SNAPSHOT]"


def create_comment(g, issue_id, message):
    repo = g.get_repo(REPOSITORY)
    issue = repo.get_issue(int(issue_id))
    existing_comment = None

    for comment in issue.get_comments():
        if PREFIX in comment.body:
            existing_comment = comment
            break

    issue.create_comment(f"{PREFIX}\n{message}")
    if existing_comment is not None:
        existing_comment.delete()


def parse_issue_from_commit(message):
    matcher = re.search(r'.*\(#(.*)\)', message)
    if matcher is None:
        return None
    else:
        return matcher.group(1)


if __name__ == "__main__":
    g = Github(environ.get("GITHUB_TOKEN"))
    issue = parse_issue_from_commit(environ.get("COMMIT_MESSAGE"))
    if issue is None:
        print("Oh no, no feature. Anyways ...")
        sys.exit(0)

    print(issue)

    create_comment(g, issue,"""
    Latest snapshot mentioning this issue has been published to marketplace:
    <a href="https://plugins.jetbrains.com/plugin/16418-mjml-support/versions/snapshot">{environ.get("SNAPSHOT_VERSION")}</a>
    """.lstrip())

