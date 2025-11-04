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
                        echo "env 파일 적재 완료 ✔"
                    '''
                }

                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
            }
        }
    }

    post {
        success {
            echo 'CI 성공 ✅'
            script {
                if (env.CHANGE_ID) {
                    setGitHubPullRequestStatus state: 'SUCCESS', context: 'Jenkins CI - Build and Test'
                }
            }

        }
        failure {
            echo 'CI 실패 ❌'
            script {
                if (env.CHANGE_ID) {
                    setGitHubPullRequestStatus state: 'FAILURE', context: 'Jenkins CI - Build and Test'
                }
            }
        }
    }
}
