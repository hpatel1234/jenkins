def call(Closure config) {
    def settings = [:]
    config.delegate = settings
    config.resolveStrategy = Closure.DELEGATE_FIRST
    config()

    echo "Auto-doc: ${settings.enableAutoDocumentation}"
    node {
        try {
            def WORKSPACE_DIR=env.WORKSPACE
            def repoName = env.JOB_NAME.tokenize('/')[0]

            stage('Checkout source code') {
                dir(repoName) {
                    checkout([
                            $class: 'GitSCM',
                            branches: [[name: env.BRANCH_NAME]],
                            doGenerateSubmoduleConfigurations: false,
                            extensions: [],
                            userRemoteConfigs: [[
                                                        url: 'https://github.com/hpatel1234/sample-app-for-doc-updater.git',
                                                        credentialsId: 'GITHUB_CRED'
                                                ]]
                    ])
                }

            }
            if("main" != env.BRANCH_NAME) {
                stage('Creating python virtual environment') {
                    dir(repoName) {
                        print("We are creating virtual environment please wait.")
                        sh 'python3 -m venv /var/jenkins_slave/workspace/virtual_env'
                        def bin_dir='/var/jenkins_slave/workspace/virtual_env/bin'
                        dir(bin_dir) {
                            print("Environment created installing dependencies")
                            sh "bash -c 'source activate && pip3 install -r ${env.WORKSPACE}/${repoName}/documentation/requirements.txt && deactivate'"
                            print("Dependencies installed.")
                        }
                    }

                }
                stage('Create knowledge graph and documentation') {
                    dir(repoName) {
                        def bin_dir='/var/jenkins_slave/workspace/virtual_env/bin'
                        dir(bin_dir) {
                            print("Creating knowledge graph")
                            sh "bash -c 'source activate && python3 ${env.WORKSPACE}/${repoName}/documentation/knowledge_graph_builder.py --repo ${env.WORKSPACE}/${repoName}/src/main/java && deactivate'"
                            print("Knowledge graph created, started creating documentation")
                            sh "bash -c 'source activate && python3 ${env.WORKSPACE}/${repoName}/documentation/main.py && deactivate'"
                        }
                    }

                }

                stage('Push updated documentation') {
                    dir(repoName) {
                        sh "git checkout main && git checkout -b ${env.BRANCH_NAME}-auto-doc"
                        def bin_dir='/var/jenkins_slave/workspace/virtual_env/bin'
                        dir(bin_dir) {
                            sh "bash -c 'mkdir -p ${env.WORKSPACE}/${repoName}/documentation/generated/'"
                            sh "bash -c 'mv documentation.html ${env.WORKSPACE}/${repoName}/documentation/generated/documentation.html'"
                        }
                        sh '''
                        git config user.email "hpatel571989@gmail.com"
                        git config user.name "hpatel1234"
                    # Check if file is tracked by git
                    if ! git ls-files --error-unmatch "$documentation/generated/documentation.html" > /dev/null 2>&1; then
                        git add "documentation/generated/documentation.html"
                    fi

                    # Check if there's anything to commit
                    if git diff --cached --quiet; then
                        echo "Nothing to commit"
                    else
                        git commit -m "Auto Committed file"
                    fi
                '''
                        withCredentials([usernamePassword(credentialsId: "GITHUB_CRED", usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                            sh "git push https://${GIT_USER}:${GIT_PASS}@github.com/hpatel1234/${repoName}.git HEAD:${env.BRANCH_NAME}-auto-doc"
                        }
                    }

                }
                stage('Create Pull Request') {
                    withCredentials([string(credentialsId: 'github-api-cred', variable: 'GITHUB_TOKEN')]) {
                        def payload = """{
                          "title": "Automated PR from Jenkins",
                          "head": "${env.BRANCH_NAME}-auto-doc",
                          "base": "main",
                          "body": "This PR was created by Jenkins using GitHub API plugin for updating documentation"
                        }"""
                        sh """
                            curl -s -X POST \
                              -H "Authorization: token ${GITHUB_TOKEN}" \
                              -H "Accept: application/vnd.github.v3+json" \
                              https://api.github.com/repos/hpatel1234/${repoName}/pulls \
                              -d '${payload}'
                        """
                    }
                }
            } else {
                stage('Publish to confluence') {
                    withCredentials([string(credentialsId: 'confluence-api-token', variable: 'CONFLUENCE_TOKEN')]) {
                        def CONFLUENCE_USER = 'hpatel5719891@gmail.com'      // Jenkins string credential (username)
                        def CONFLUENCE_URL = 'https://innovathon.atlassian.net/wiki'
                        def CONFLUENCE_SPACE = 'DS'
                        def PARENT_PAGE_ID = '1179657'
                        def PAGE_TITLE = 'Sample Application ETL Logic'
                        dir(repoName) {
                            def htmlContent = readFile('documentation/generated/documentation.html')
                            htmlContent = htmlContent.replace('"', '\\"').replace('\n', '')  // sanitize

                            def payload = """
                        {
                            "type": "page",
                            "title": "${PAGE_TITLE}",
                            "ancestors": [{"id": ${PARENT_PAGE_ID}}],
                            "space": {"key": "${CONFLUENCE_SPACE}"},
                            "body": {
                                "storage": {
                                    "value": "${htmlContent}",
                                    "representation": "storage"
                                }
                            }
                        }
                        """
                            sh """
                        curl -u "${CONFLUENCE_USER}:${CONFLUENCE_TOKEN}" \
                         -X POST \
                         -H "Content-Type: application/json" \
                         ${CONFLUENCE_URL}/rest/api/content \
                         -d '${payload}'
                        """
                        }
                    }

                }
            }

        } finally {
            cleanWs()
        }
    }
    
}