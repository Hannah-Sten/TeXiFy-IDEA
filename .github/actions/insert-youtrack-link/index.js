const core = require('@actions/core');
const github = require('@actions/github');
const {Octokit} = require("@octokit/rest");

function getText() {
    // Attempt to find the text to replace. This is somewhat involved since we trigger on many actions.
    const event = github.context.eventName;

    if (event === 'issues') {
        return github.context.payload.issue.body;
    }
    else if (event === 'issue_comment') {
        return github.context.payload.comment.body;
    }
    else if (event === 'pull_request') {
        return github.context.payload.pull_request.body;
    }
    else if (event === 'pull_request_review') {
        return github.context.payload.review.body;
    }
    else if (event === 'pull_request_review_comment') {
        return github.context.payload.comment.body;
    }

    return null;
}

async function setText(token, replacement) {
    const client = new Octokit({auth: token});

    const event = github.context.eventName;

    if (event === 'issues') {
        await client.rest.issues.update({
            owner: github.context.repo.owner,
            repo: github.context.repo.repo,
            issue_number: github.context.payload.issue.number,
            body: replacement,
        });
    }
    else if (event === 'issue_comment') {
        await client.rest.issues.updateComment({
            owner: github.context.repo.owner,
            repo: github.context.repo.repo,
            comment_id: github.context.payload.comment.id,
            body: replacement,
        });
    }
    else if (event === 'pull_request') {
        await client.rest.pulls.update({
            owner: github.context.repo.owner,
            repo: github.context.repo.repo,
            pull_number: github.context.payload.pull_request.number,
            body: replacement,
        });
    }
    else if (event === 'pull_request_review') {
        await client.rest.pulls.updateReview({
            owner: github.context.repo.owner,
            repo: github.context.repo.repo,
            pull_number: github.context.payload.pull_request.number,
            review_id: github.context.payload.review.id,
            body: replacement,
        });
    }
    else if (event === 'pull_request_review_comment') {
        await client.rest.pulls.updateReviewComment({
            owner: github.context.repo.owner,
            repo: github.context.repo.repo,
            comment_id: github.context.payload.comment.id,
            body: replacement,
        });
    }
}

try {
    const prefix = core.getInput('issue-prefix');
    const baseUrl = core.getInput('youtrack-base-url');
    const token = core.getInput('repo-token');

    const text = getText();
    if (text === null || text === undefined) {
        return;
    }

    const regex = new RegExp(`(?<!\\[|\/issue\/)\\b(${prefix}-\\d+)\\b(?!\\])`, 'g');

    if (!regex.test(text)) {
        // Exit early when no links need to be inserted to avoid API calls
        return
    }

    const replacement = text.replace(regex, `[$1](${baseUrl}/issue/$1)`);

    setText(token, replacement);
}
catch (e) {
    core.setFailed(e.message);
}
