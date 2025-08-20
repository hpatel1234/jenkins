def call(Closure config) {
    def settings = [:]
    config.delegate = settings
    config.resolveStrategy = Closure.DELEGATE_FIRST
    config()

    echo "Auto-doc: ${settings.enableAutoDocumentation}"
    stage('Checkout source code') {
         checkout([
            $class: 'GitSCM',
            branches: [[name: env.BRANCH_NAME]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [],
            userRemoteConfigs: [[
                url: 'git@github.com:hpatel1234/sample-app-for-doc-updater.git',
                credentialsId: 'GITHUB_CRED'
            ]]
        ])
    }
}