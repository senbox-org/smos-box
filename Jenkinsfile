#!/usr/bin/env groovy

/**
 * Copyright (C) 2019 CS-SI
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

pipeline {
    environment {
        toolName = sh(returnStdout: true, script: "echo ${env.JOB_NAME} | cut -d '/' -f 1").trim()
        branchVersion = sh(returnStdout: true, script: "echo ${env.GIT_BRANCH} | cut -d '/' -f 2").trim()
        toolVersion = ''
        deployDirName = ''
        snapMajorVersion = ''
    }
    agent { label 'snap-test' }
    stages {
        stage('Package') {
            agent {
                docker {
                    label 'snap-test'
                    image 'snap-build-server.tilaa.cloud/maven:3.6.0-jdk-8'
                    args '-e MAVEN_CONFIG=/var/maven/.m2 -v /data/ssd/testData/:/data/ssd/testData/ -v /opt/maven/.m2/settings.xml:/home/snap/.m2/settings.xml -v docker_local-update-center:/local-update-center'
                }
            }
            steps {
                script {
                    // Get snap version from pom file
                    toolVersion = sh(returnStdout: true, script: "cat pom.xml | grep '<version>' | head -1 | cut -d '>' -f 2 | cut -d '-' -f 1").trim()
                    snapMajorVersion = sh(returnStdout: true, script: "echo ${toolVersion} | cut -d '.' -f 1").trim()
                    deployDirName = "${toolName}/${branchVersion}-${toolVersion}-${env.GIT_COMMIT}"
                }
                echo "Build Job ${env.JOB_NAME} from ${env.GIT_BRANCH} with commit ${env.GIT_COMMIT}"
                sh "mvn -Dm2repo=/var/tmp/repository/ -Duser.home=/home/snap -Dsnap.userdir=/home/snap clean package install -U -DskipTests=false"
            }
        }
        stage('Deploy') {
            agent {
                docker {
                    label 'snap-test'
                    image 'snap-build-server.tilaa.cloud/maven:3.6.0-jdk-8'
                    args '-e MAVEN_CONFIG=/var/maven/.m2 -v /data/ssd/testData/:/data/ssd/testData/ -v /opt/maven/.m2/settings.xml:/home/snap/.m2/settings.xml -v docker_local-update-center:/local-update-center'
                }
            }
            when {
                expression {
                    return "${env.GIT_BRANCH}" == 'master' || "${env.GIT_BRANCH}" =~ /\d+\.x/;
                }
            }
            steps {
                script {
                    // Get snap version from pom file
                    toolVersion = sh(returnStdout: true, script: "cat pom.xml | grep '<version>' | head -1 | cut -d '>' -f 2 | cut -d '-' -f 1").trim()
                    snapMajorVersion = sh(returnStdout: true, script: "echo ${toolVersion} | cut -d '.' -f 1").trim()
                    deployDirName = "${toolName}/${branchVersion}-${toolVersion}-${env.GIT_COMMIT}"
                }
                echo "Deploy Job ${env.JOB_NAME} from ${env.GIT_BRANCH} with commit ${env.GIT_COMMIT}"
                // sh "mvn -Dm2repo=/var/tmp/repository/ -Duser.home=/home/snap -Dsnap.userdir=/home/snap deploy -U -DskipTests=true"
                // sh "/opt/scripts/saveToLocalUpdateCenter.sh . ${deployDirName} ${branchVersion} ${toolName}"
            }
        }
    /* disable email send on failure
    post {
        failure {
            step (
                emailext(
                    subject: "[SNAP] JENKINS-NOTIFICATION: ${currentBuild.result ?: 'SUCCESS'} : Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                    body: """Build status : ${currentBuild.result ?: 'SUCCESS'}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':
Check console output at ${env.BUILD_URL}
${env.JOB_NAME} [${env.BUILD_NUMBER}]""",
                    attachLog: true,
                    compressLog: true,
                    recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class:'DevelopersRecipientProvider']]
                )
            )
        }
    }*/
}
