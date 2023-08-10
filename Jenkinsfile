node {
    environment {
        SONARQUBE_HOME = tool name: 'SonarQubeScanner'
    }
    stage('Checkout') {
        checkout scm
    }
    
    stage('SonarQube Analysis') {
        script {
            def scannerOpts = [
                "-Dsonar.host.url=http://192.168.10.12:9000",
                "-Dsonar.token=sqa_62bb4850c553228098cdbfd1809f913a59115e14",
                "-Dsonar.projectKey=lucida-account",
                "-Dsonar.projectName=lucida-account",
                "-Dsonar.report.export.path=${SONARQUBE_HOME}/.scannerwork/sonar-report.json",
                "-Dsonar.sources=src/main/java",
                "-Dsonar.exclusions='**/constants/**'",
                "-Dsonar.coverage.exclusions='**/config/**, **/controller/**, **/entity/**, **/helper/**, **/kafka/**, **/repository/**, **/service/**'",
                "-Dsonar.java.sourcesion=1.8",
                "-Dsonar.sourceEncoding=UTF-8",
                "-Dsonar.java.binaries=build/classes",
                "-Dsonar.coverage.jacoco.xmlReportPaths=${PROJECTDIR}/build/reports/jacoco/test/jacocoTestReport.xml",
                "-Dsonar.projectBaseDir=${PROJECTDIR}"
            ]
            
            withSonarQubeEnv('SonarQubeServer') {
                sh "${SONARQUBE_HOME}/bin/sonar-scanner", scannerOpts.join(' ')
            }
        }
    }
    
    stage('Quality Gate') {
        script {
            def qualityGate = waitForQualityGate('SonarQubeServer')

            echo "Quality Gate status: ${qualityGate.status}"

            if (qualityGate.status != 'PASSED') {
                error("Quality Gate Failed.")
            }
        }
    }
}
