node {

    stage('Checkout') {
        checkout scm
    }
    
    stage('SonarQube Analysis') {
        def SONARQUBE_HOME = tool 'SonarQube Scanner'
        def PROJECTDIR = pwd()

        withSonarQubeEnv() {
            sonarAnalysis = sh(returnStatus: true, script:
                """
                ${scannerHome}/bin/sonar-scanner \
                -Dsonar.host.url=http://192.168.10.12:9000 \
                -Dsonar.token=sqa_62bb4850c553228098cdbfd1809f913a59115e14 \
                -Dsonar.projectKey=lucida-account \
                -Dsonar.projectName=lucida-account \
                -Dsonar.report.export.path=${scannerHome}/.scannerwork/sonar-report.json \
                -Ddetekt.sonar.kotlin.config.path=default-detekt-config.yml \
                -Dsonar.sources=src/main/java \
                -Dsonar.exclusions='**/constants/**' \
                -Dsonar.coverage.exclusions='**/config/**, **/controller/**, **/entity/**, **/helper/**, **/kafka/**, **/repository/**, **/service/**' \
                -Dsonar.java.sourcesion=1.8 \
                -Dsonar.sourceEncoding=UTF-8 \
                -Dsonar.java.binaries=build/classes \
                -Dsonar.coverage.jacoco.xmlReportPaths=${PROJECTDIR}/build/reports/jacoco/test/jacocoTestReport.xml \
                -Dsonar.projectBaseDir=${PROJECTDIR}
                """
                )
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
