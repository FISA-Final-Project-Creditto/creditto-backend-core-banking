pipeline {
    agent any

    stages {
        stage('Build and Test') {
            steps {
				sh 'chmod +x ./gradlew'
				sh './gradlew clean'
				sh './gradlew processResources processTestResources'

                withCredentials([string(credentialsId: 'core_banking_env', variable: 'ENV_CONTENT')]) {
                    sh '''
                        echo "$ENV_CONTENT" > .env
                        chmod 600 .env
                        ls -al .env
                        echo "env 파일 적재 완료 ✔"
                    '''
                }

				sh './gradlew build'
				sh 'rm .env'
            }
        }

		stage('SonarQube Analysis') {
			steps {
				withCredentials([string(credentialsId: 'CORE_SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
					sh """
						./gradlew sonar \
						  -Dsonar.projectKey=core-banking \
						  -Dsonar.host.url=http://192.168.0.79:8490 \
						  -Dsonar.login=$SONAR_TOKEN
					"""
				}
			}
		}

		stage('Quality Gate') {
			steps {
				timeout(time: 3, unit: 'MINUTES') {
					waitForQualityGate abortPipeline: true
				}
			}
		}
    }

    post {
      always {
		  junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
      }
    }
}
