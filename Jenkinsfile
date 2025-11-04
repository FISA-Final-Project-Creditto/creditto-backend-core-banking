pipeline {
    agent any

    stages {
        stage('Build and Test') {
            steps {
                withCredentials([file(credentialsId: 'env-file', variable: 'ENV_FILE')]) {
                    sh '''
                        cp $ENV_FILE /tmp/.env
                        cp /tmp/.env .env || cat /tmp/.env > .env
                        chmod 600 .env
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
