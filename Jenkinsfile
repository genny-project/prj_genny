pipeline {
  agent any
  stages {
    stage('Stage1') {
      parallel {
        stage('Stage1') {
          steps {
            sh '  echo "The Pull Request Branch is:: "'
          }
        }

        stage('stage2') {
          steps {
            sh 'echo "The Pull Request Branch is:: ";'
          }
        }

      }
    }

  }
}