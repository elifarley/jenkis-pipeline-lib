package com.orgecc.jpl

def shc(String commands) { sh(returnStdout: true, script: commands).trim() }

def getCommitId() { shc """
  test -d .git && git 2>/dev/null rev-parse HEAD \
  || \
  test -d .hg && hg log -r . --template '{node}\n'
""" }

def shManaged(String scriptID) {
  configFileProvider([configFile(fileId: "org.jenkinsci.plugins.managedscripts.${scriptID}", targetLocation: "/tmp/${scriptID}.sh", variable: 'script')]) {
    sh '. $script'
  }
}

def dockerizeProject() { withEnv(["PATH=$WORKSPACE/.~/shell-lib/bin:${env.PATH}"]) { stage('Dockerize') {
  sh """
set -x
mkdir -p .~/shell-lib
curl -H 'Cache-Control: no-cache' -fsSL https://github.com/elifarley/shell-lib/archive/master.tar.gz | \
tar -zx --strip-components 1 -C .~/shell-lib && \
chmod +x .~/shell-lib/bin/*

test -e target || mkdir -p target 

DEBUG=1 dockerize-project
""" }

  archiveArtifacts artifacts: 'target/app.tgz'

}}

/**
Example:
import com.orgecc.jpl.Base
def jplb = new com.orgecc.jpl.Base()

jplb.dockerPush('my-repo')
*/
def dockerPush(String dockerRepo) { withEnv(["PATH=$WORKSPACE/.~/shell-lib/bin:${env.PATH}"]) { stage("Docker push to ${dockerRepo}") {sh """
set -x
mkdir -p .~/shell-lib
curl -fsSL -H 'Cache-Control: no-cache' https://github.com/elifarley/shell-lib/archive/master.tar.gz | \
tar -zx --strip-components 1 -C .~/shell-lib && \
chmod +x .~/shell-lib/bin/*

DEBUG=1 jenkins-docker-push \
  "${env.JOB_NAME}" "${env.BUILD_NUMBER}" "${getCommitId()}" "${dockerRepo}"

"""}}}
