### Jenkins app Set up:
1. Install docker
2. docker build -t local/pre-configured-jenkins-agent .
3. docker network create custom-network
4. Change volume paths as per your system 
  docker run --name jenkins-docker --network custom-network --rm --volume "D:\SOFTWARES\jenkins\data":/var/jenkins_slave  --volume "D:\SOFTWARES\jenkins\data":/var/jenkins_home -p 8080:8080 -p 50000:50000 local/pre-configured-jenkins-agent
5. Once jenkins up and runnin, access at http://localhost:8080
   For admin password look at console output of step4
6. Once login install all suggested plugins, it will take few minutes.
7. Create user with username "admin" and password "admin"

### Credential set up
1. Go to https://github.com/settings/tokens
2. Generate classic token for your profile
3. Login to jenkins and Click on Settings icon top right corner.
4. Click on "Credentials"
5. Add your username and token generated
### Jenkins lib set up
6. Again Click on Settings icon top right corner. Then System
9. Search for "Global Trusted Pipeline Libraries" on page and configure this repository as jenkins Lib
   Name: jenkinsLib
   Default Version: main
   Select below: Load implicitly?
                Allow default version to be overridden?
                Include @Library changes in job recent changes?
   Retrieval Method: Modern SCM
   Source Code Management: Github
   Select credentials set up previously to access github
10. Repository HTTPS Url: https://github.com/hpatel1234/jenkins
11. Save
### Configure multibranch pipeline
1. Go to Jenkins home
2. Click on New item.
3. Provide name as "sample-app-for-doc-updater" . Then select "Multibranch Pipeline"
4. In Branch sources section select your git hub credentials and Provide https://github.com/hpatel1234/sample-app-for-doc-updater repositor URL
5. Save and let Jenkins scan multi branch pipeline.