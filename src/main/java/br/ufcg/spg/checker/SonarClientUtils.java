package br.ufcg.spg.checker;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.SonarClient;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;
import org.sonar.wsclient.issue.Issues;

import java.util.List;

public class SonarClientUtils {
  public static void main(String [] args) {
    SonarClient client = SonarClient.builder()
            .url("http://localhost:9000").build();
    IssueQuery query = IssueQuery.create();
    int page = 1;
    boolean sawTemp = false;
    while(true) {
      query.pageIndex(page++);
      IssueClient issueClient = client.issueClient();
      Issues issues = issueClient.find(query);
      List<Issue> issueList = issues.list();

      for (int i = 0; i < issueList.size(); i++) {
        if (sawTemp && !issueList.get(i).projectKey().contains("temp.java")) {
          return;
        }
        if (issueList.get(i).componentKey().contains("temp.java")) {
          sawTemp = true;
          throw new RuntimeException();
        }
        System.out.println(issueList.get(i).projectKey() + " " +
                issueList.get(i).componentKey() + " " +
                issueList.get(i).line() + " " +
                issueList.get(i).ruleKey() + " " +
                issueList.get(i).severity() + " " +
                issueList.get(i).message());
      }
    }
  }
}
