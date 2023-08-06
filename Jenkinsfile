def SONAR_PROJECT = env.JOB_NAME

node { 

        stage('Checkout') {
            git branch: 'main', credentialsId: 'deokwon2', url: 'https://github.com/deokwon2/lucida-account.git'
        }
                
        stage('SonarQube Analysis') { 

            def scannerHome = tool 'SonarQube Scanner'
            def PROJECTDIR = pwd() 

            withSonarQubeEnv() {
                sonarAnalysis = sh(returnStatus: true, script:
                    """
                    ${scannerHome}/bin/sonar-scanner \
                    -Dsonar.host.url=http://192.168.219.105:9000 \
                    -Dsonar.login=admin
                    -Dsonar.password=admin1234
                    -Dsonar.projectKey=lucida-role \
                    -Dsonar.projectName=lucida-role \
                    -Dsonar.report.export.path=${scannerHome}/.scannerwork/sonar-report.json \
                    -Ddetekt.sonar.kotlin.config.path=default-detekt-config.yml \
                    -Dsonar.sources=src/main/java,src/main/resources \
                    -Dsonar.exclusions='**/util/**,**/support/**,**/dto/**,**/entity/**' \
                    -Dsonar.java.sourcesion=1.8 \
                    -Dsonar.sourceEncoding=UTF-8 \
                    -Dsonar.java.binaries=build/classes \
                    -Dsonar.coverage.jacoco.xmlReportPaths=${PROJECTDIR}/build/reports/jacoco/test/jacocoTestReport.xml \
                    -Dsonar.projectBaseDir=${PROJECTDIR}
                    """)
            }

         }

}