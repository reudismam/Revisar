package br.ufcg.spg.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class GitProject {
	
	public static void main(String [] args) {
		System.out.println("START");
		try {
			List<String> lines = GitProject.getProjects("git_query.txt");
			FileUtils.writeLines(new File("git_projects.txt"), lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
	}
	
	
	public static List<String> getProjects(String gitQuery) throws IOException {
		List<String> lines = FileUtils.readLines(new File(gitQuery));
        List<String> projects = new ArrayList<>();
        for (String line : lines) {
        	if (line.contains("clone_url")) {
        		line = "git clone " + line.substring(line.indexOf("https"), line.length() - 2);
        		projects.add(line);
        	}
        }
		return projects;
	}
}
