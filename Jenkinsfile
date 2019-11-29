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
        sonarOption = ""
    }
    agent { label 'snap-test' }
    stages {
        stage('Package and Deploy') {
            agent {
                docker {
                    label 'snap-test'
                    image 'snap-build-server.tilaa.cloud/maven:3.6.0-jdk-8'
                    args '-e MAVEN_CONFIG=/var/maven/.m2 -v /data/ssd/testData/:/data/ssd/testData/ -v /opt/maven/.m2/settings.xml:/home/snap/.m2/settings.xml -v docker_local-update-center:/local-update-center'
                }
            }
            steps {
                script {
                    sonarOption = ""
                    if ("${branchVersion}" == "master") {
                        // Only use sonar on master branch
                        sonarOption = "sonar:sonar"
                    }
                }
                echo "Build Job ${env.JOB_NAME} from ${env.GIT_BRANCH} with commit ${env.GIT_COMMIT}"
                sh "mvn -Dm2repo=/var/tmp/repository/ -Duser.home=/home/snap -Dsnap.userdir=/home/snap clean package install ${sonarOption} -U -DskipTests=false -Dmaven.test.failure.ignore=true"
            }
            post {
                always {
                    junit "**/target/surefire-reports/*.xml"
                    jacoco(execPattern: '**/*.exec')
                }
                success {
                    script {
                        if ("${env.GIT_BRANCH}" == 'master' || "${env.GIT_BRANCH}" =~ /\d+\.x/ || "${env.GIT_BRANCH}" =~ /\d+\.\d+\.\d+(-rc\d+)?$/) {
                            echo "Deploy ${env.JOB_NAME} from ${env.GIT_BRANCH} with commit ${env.GIT_COMMIT}"
                            sh "mvn -Dm2repo=/var/tmp/repository/ -Duser.home=/var/maven -Dsnap.userdir=/home/snap deploy -DskipTests=true"
                        }
                    }
                }
            }
        }
        stage('Save installer data') {
            agent {
                docker {
                    label 'snap-test'
                    image 'snap-build-server.tilaa.cloud/scripts:1.0'
                    args '-v docker_snap-installer:/snap-installer'
                }
            }
            when {
                expression {
                    return "${env.GIT_BRANCH}" == 'master' || "${env.GIT_BRANCH}" =~ /\d+\.\d+\.\d+(-rc\d+)?$/;
                }
            }
            steps {
                echo "Save data for SNAP Installer ${env.JOB_NAME} from ${env.GIT_BRANCH} with commit ${env.GIT_COMMIT}"
                sh "/opt/scripts/saveInstallData.sh ${toolName} ${env.GIT_BRANCH}"
            }
        }
        /*stage('Deploy') {
            agent {
                docker {
                    label 'snap-test'
                    image 'snap-build-server.tilaa.cloud/maven:3.6.0-jdk-8'
                    args '-e MAVEN_CONFIG=/var/maven/.m2 -v /opt/maven/.m2/settings.xml:/var/maven/.m2/settings.xml -v docker_local-update-center:/local-update-center'
                }
            }
            when {
                expression {
                    return "${env.GIT_BRANCH}" == 'master' || "${env.GIT_BRANCH}" =~ /\d+\.x/ || "${env.GIT_BRANCH}" =~ /\d+\.\d+\.\d+(-rc\d+)?$/;
                }
            }
            steps {
                echo "Deploy ${env.JOB_NAME} from ${env.GIT_BRANCH} with commit ${env.GIT_COMMIT}"
                sh "mvn -Duser.home=/var/maven -Dsnap.userdir=/home/snap deploy -U -DskipTests=true"
            }
        }*/
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
