package com.orgecc.jpl

def shc(String commands) { sh(returnStdout: true, script: "${commands}").trim() }

def getCommitId() { shc 'git rev-parse HEAD' }

def shManaged(String scriptID) {
  configFileProvider([configFile(fileId: "org.jenkinsci.plugins.managedscripts.${scriptID}", targetLocation: "/tmp/${scriptID}.sh", variable: 'script')]) {
    sh '. $script'
  }
}

def dockerizeProject() { sh """
set -x
mkdir -p .~/shell-lib
curl -H 'Cache-Control: no-cache' -fsSL https://github.com/elifarley/shell-lib/archive/master.tar.gz | \
tar -zx --strip-components 1 -C .~/shell-lib && \
chmod +x .~/shell-lib/bin/* && PATH="$PWD/.~/shell-lib/bin:$PATH"

test -e target || mkdir -p target 

DEBUG=1 dockerize-project

""" }
