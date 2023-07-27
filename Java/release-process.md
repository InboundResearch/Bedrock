# Release Process
Bedrock is currently deployed via Sonatype Nexus and the web app is built into a docker container deployed on AWS Elastic Container Service (ECS). We use a self-built container running Tomcat 10 on Open JDK 17.

1) in the development branch, complete all changes, run "mvn clean install" and check in any changes. You will need to be running mongod for the database tests to succeed, and you will need to be online with access to bedrock.brettonw.com for the network bag tests to succeed.
2) merge the development branch changes to main (or use a pull request, which might be easier)
3) check out the main branch
4) make sure AWS-ECS command-line interface (CLI) tools are up-to-date.
5) change the parent pom version to the release version (remove "-SNAPSHOT") and check in the change
6) mvn clean install
7) check in the newly built artifacts that have to be preserved (the web distribution, for example)
8) mvn clean deploy
9) merge main changes back to development (again using a pull request)
10) check the development branch
11) increment the parent pom bedrock version, add "-SNAPSHOT" to it, and check in the change
12) mvn clean install
13) (NOT APPLICABLE TO BEDROCK 2) deploy the site changes by navigating to AWS and stopping the task associated with the running instance. AWS will automatically relaunch a new instance with the updated container definition.
