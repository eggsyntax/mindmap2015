#!/usr/bin/env python
#
# Script that copies all hook files into the
# .git/hooks directory.
#
# Note: Any extensions are stripped off before copying
#
import sys, os, shutil

# we *should* be invoked from the repo-config directory
cwd = os.getcwd()
repoDir = os.path.dirname(cwd)
gitDir = os.path.join(repoDir, '.git')
hookDir = os.path.join(gitDir, 'hooks')

ignore = ['README', 'init.py']

for script in os.listdir(cwd):
  if (not script in ignore):
    cp = os.path.splitext(script)[0]
    dest = os.path.join(hookDir, cp)
    msg = 'init> Copying (' + script +') to: ' + dest
    print msg
    shutil.copy(script, dest)








