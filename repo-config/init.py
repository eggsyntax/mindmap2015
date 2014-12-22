#!/usr/bin/env python
#
# Script that copies all hook files into the
# .git/hooks directory.
#
# Note: Any extensions are stripped off before copying
#
# Allow the script to be run from either the top-level repo
# directory *or* the config directory itself
#
import sys, os, shutil

cwd = os.getcwd()

if ('repo-config' in os.listdir(cwd)):
  repoDir = cwd
  initDir = os.path.join(cwd, 'repo-config')
else:
  repoDir = os.path.dirname(cwd)
  initDir = cwd

gitDir = os.path.join(repoDir, '.git')
hookDir = os.path.join(gitDir, 'hooks')
print 'init.py> Installing to: ', hookDir

# List of all available hooks
#
# http://git-scm.com/docs/githooks
#
hooks = ['applypatch-msg'
        , 'pre-applypatch'
        , 'post-applypatch'
        , 'pre-commit'
        , 'prepare-commit-msg'
        , 'commit-msg'
        , 'post-commit'
        , 'pre-rebase'
        , 'post-checkout'
        , 'post-merge'
        , 'pre-push'
        , 'pre-receive'
        , 'update'
        , 'post-receive'
        , 'post-update'
        , 'pre-auto-gc'
        , 'post-rewrite']

os.chdir(initDir)
for script in os.listdir(initDir):
  cp = os.path.splitext(script)[0]
  if (cp in hooks):
    dest = os.path.join(hookDir, cp)
    msg = 'init> Copying (' + script +') to ' + cp
    print msg
    shutil.copy(script, dest)








