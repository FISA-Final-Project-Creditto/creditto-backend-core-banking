pipeline {
    agent any

    stages {
        stage('Build and Test') {
            steps {
                withCredentials([string(credentialsId: 'core_banking_env', variable: 'ENV_CONTENT')]) {
                    sh '''
                        echo "$ENV_CONTENT" > .env
                        chmod 600 .env
                        ls -al .env
                    '''
                }

                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
            }
        }
    }

    post {
        success {
            echo 'CI 성공 PR Status 업데이트 ✅'
            setGitHubPullRequestStatus state: 'SUCCESS', context: 'Jenkins CI - Build and Test'
        }
        failure {
            echo 'CI 실패 PR Status 업데이트 ❌' 
            setGitHubPullRequestStatus state: 'FAILURE', context: 'Jenkins CI - Build and Test'
        }
    }
}
