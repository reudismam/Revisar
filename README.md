# Overview #

<p style="text-align: justify;">
Code analyzers such as Error Prone and FindBugs detect code patterns symptomatic of bugs, performance issues, or bad style. These tools express patterns as quick fixes that detect and rewrite unwanted code. However, it is difficult to come up with new quick fixes and decide which ones are useful and frequently appear in real code. We propose to rely on the collective wisdom of programmers and learn quick fixes from revision histories in software repositories. We present Revisar, a tool for discovering common Java edit patterns in code repositories. Given code repositories and their revision histories, Revisar (i) identifies code edits from revisions and (ii) clusters edits into sets that can be described using an edit pattern. The designers of code analyzers can then inspect the patterns and add the corresponding quick fixes to their tools. We ran Revisar on nine popular GitHub projects, and it discovered 89 useful edit patterns that appeared in 3 or more projects. Moreover, 64% of the discovered patterns did not appear in existing tools. We then conducted a survey with 164 programmers from 124 projects and found that programmers significantly preferred eight out of the nine of the discovered patterns. Finally, we submitted 16 pull requests applying our patterns to 9 projects and, at the time of the writing, programmers accepted 7 (63.6%) of them. The results of this work aid toolsmiths in discovering quick fixes and making informed decisions about which quick fixes to prioritize based on patterns programmers actually apply in practice.
</p>

[Reudismam Rolim](http://www.dsc.ufcg.edu.br/~spg/reudismam/), [Gustavo Soares](https://gustavoasoares.github.io/), [Rohit Gheyi](http://www.dsc.ufcg.edu.br/~rohit/), [Titus Barik](https://www.barik.net/), [Loris D'Antoni](http://pages.cs.wisc.edu/~loris/)

This paper is available on [here](https://arxiv.org/abs/1803.03806).

## Setting up ##
To run Revisar, it is needed the following steps:

1. Install [PostgreSQL]( https://www.postgresql.org/). After installing this data management system, open PgAdmin and create a database named AUDb. Create a user postgres and provide the password 12345. Revisar stores its data in a database and requires this specific configuration.
2. Clone the project that will be analyzed to a folder (e.g., Projects). See the file [sh/clone.sh](sh/clone.sh) for an example of how to do that.

## Running Revisar ##
Revisar can be found in the folder /binary. Revisar is available as a jar file.
To run revisar, perform the following commands on cmd.
```sh
> java -jar .\revisar.jar <options> <args>
```
Where options can be the following:
### -e ###
Use -e option to extract concrete edits. In this case, it is needed to provide a .txt with the names of the projects to be analyzed, and the name of the folder where these projects are located. For instance, use the following command to extract the edits from the projects specified in the projects.txt file, which are located in the Projects/ folder.
```sh
> java -jar .\revisar.jar -e projects.txt Projects/
```

### -c ###
Use -c option to cluster concrete edits. 
```sh
> java -jar .\revisar.jar -c
```

### -t ###
Use -t option learn transformation for all identified clusters. The transformation will be generated to a folder cluster at the same level of the folder that contains the source code of the projects.
```sh
> java -jar .\revisar.jar -t
```

### -tid ###

Use -tid option to learn a transformation for a specific cluster. We need to provide the id of the cluster.
```sh
> java -jar .\revisar.jar -tid 123456
```
