def call(Closure config) {
    def settings = [:]
    config.delegate = settings
    config.resolveStrategy = Closure.DELEGATE_FIRST
    config()

    echo "Auto-doc: ${settings.enableAutoDocumentation}"
    node {
        WORKSPACE_DIR=env.WORKSPACE
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
        stage('Creating python virtual environmane') {
            dir(repoName) {
                print("We are creating virtual environment please wait.")
                sh 'python3 -m venv virtual_env'
                def bin_dir='virtual_env/bin'
                dir(bin_dir) {
                    print("Environment created installing dependencies")
                    sh 'bash -c "source activate && pip3 install -r ../../documentation/requirements.txt && deactivate"'
                    print("Dependencies installed.")
                }
            }

        }
        stage('Create knowledge graph and documentation') {
            dir(repoName) {
                def bin_dir='virtual_env/bin'
                dir(bin_dir) {
                    print("Creating knowledge graph")
                    sh 'bash -c "source activate && python3 ../../documentation/knowledge_graph_builder.py --repo ../../src/main/java && deactivate"'
                    print("Knowledge graph created, started creating documentation")
                    sh 'bash -c "source activate && python3 ../../documentation/main.py && deactivate"'
                }
            }

        }
        stage('Moving Auto generated documentation to source directory') {
            dir(repoName) {

            }

        }
        stage('Creating Pull request') {
            dir(repoName) {
                sh 'bash -c "git checkout main && git checkout -b auto-doc"'
                sh 'bash -c "mkdir -p ../../documentation/generated/"'
                def bin_dir='virtual_env/bin'
                dir(bin_dir) {
                    sh 'bash -c "mv documentation.html ../../documentation/generated/documentation.html"'
                }
                sh 'bash -c "git commit documentation/generated/documentation.html -m \"Auto Committed file\""'
                withCredentials([usernamePassword(credentialsId: "GITHUB_CRED", usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                    sh """
                            git push https://${GIT_USER}:${GIT_PASS}@github.com/hpatel1234/${repoName}.git HEAD:auto-doc
                        """
                }
                //sh 'bash -c "git push -u origin auto-doc"'
            }

        }
    }
    
}