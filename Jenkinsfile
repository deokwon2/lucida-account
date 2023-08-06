def SONAR_PROJECT = env.JOB_NAME

node {
//       stage('SCM') {
//         checkout scm
//       }


    stage('Checkout') {
      git branch: 'main', credentialsId: 'deokwon2', url: 'https://github.com/deokwon2/lucida-account.git'
    }

    stage('SonarQube Analysis') {
       withSonarQubeEnv() {
           sh "./gradlew sonar"
       }
    }


//         stage('SonarQube Analysis') {
//
//             def scannerHome = tool 'SonarQube Scanner'
//             def PROJECTDIR = pwd()
//
//             withSonarQubeEnv() {
//                 sonarAnalysis = sh(returnStatus: true, script:
//                     """
//                     ${scannerHome}/bin/sonar-scanner \
//                     -Dsonar.host.url=http://192.168.219.105:9000 \
//                     -Dsonar.token=sqa_62bb4850c553228098cdbfd1809f913a59115e14
//                     -Dsonar.projectKey=lucida-account \
//                     -Dsonar.projectName=lucida-account \
//                     -Dsonar.report.export.path=${scannerHome}/.scannerwork/sonar-report.json \
//                     -Ddetekt.sonar.kotlin.config.path=default-detekt-config.yml \
//                     -Dsonar.sources=src/main/java,src/main/resources \
//                     -Dsonar.exclusions='**/util/**,**/support/**,**/dto/**,**/entity/**' \
//                     -Dsonar.java.sourcesion=1.8 \
//                     -Dsonar.sourceEncoding=UTF-8 \
//                     -Dsonar.java.binaries=build/classes \
//                     -Dsonar.coverage.jacoco.xmlReportPaths=${PROJECTDIR}/build/reports/jacoco/test/jacocoTestReport.xml \
//                     -Dsonar.projectBaseDir=${PROJECTDIR}
//                     """)
//             }
//
//          }

}