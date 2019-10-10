pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh '''ls
        ./build-docker.sh ${BUILD_NUMBER}
        docker run -t --rm  gennyproject/prj_genny:${BUILD_NUMBER}
        '''
      }
    }
  }
}
