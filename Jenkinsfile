pipeline {
    agent any

    tools {
        jdk 'JDK17'
    }

    stages {
        stage('Build and Test') {
            steps {
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
