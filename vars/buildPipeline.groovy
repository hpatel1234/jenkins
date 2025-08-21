def call(Closure config) {
    def settings = [:]
    config.delegate = settings
    config.resolveStrategy = Closure.DELEGATE_FIRST
    config()

    echo "Auto-doc: ${settings.enableAutoDocumentation}"
    node {
        print("${env.WORKSPACE}")
        stage('Checkout source code') {
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
        stage('Run knowedge builder') {
            sh 'python3 -c "print(\\"HIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII\\")"'
        }
    }
    
}