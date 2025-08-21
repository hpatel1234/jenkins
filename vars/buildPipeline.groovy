def call(Closure config) {
    def settings = [:]
    config.delegate = settings
    config.resolveStrategy = Closure.DELEGATE_FIRST
    config()

    echo "Auto-doc: ${settings.enableAutoDocumentation}"
    node {
        WORKSPACE_DIR=env.WORKSPACE
        def repoName = env.JOB_NAME.tokenize('/')[0]
        sh 'printenv'
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
        stage('Create knowledge graph') {
            dir(repoName) {
                print("We are creating virtual environment please wait.")
                sh 'python3 -m venv virtual_env'
                def bin_dir='virtual_env/bin'
                dir(bin_dir) {
                    sh 'bash && source activate && pip3 install -r ../../documentation/requirements.txt && deactivate'
                }
            }

        }
    }
    
}