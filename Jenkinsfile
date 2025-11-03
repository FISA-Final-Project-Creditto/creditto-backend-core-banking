pipeline {
    agent any

    stages {
        stage('Build and Test') {
            steps {
                withCredentials([file(credentialsId: 'core_banking_env', variable:'ENV_FILE')]) {
                sh 'cp $ENV_FILE .env'
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
