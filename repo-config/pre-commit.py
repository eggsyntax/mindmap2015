#!/usr/bin/env python
#
# Simple script that finds XMind files and un-zips them
# in-place so GIT can track its contents in a way that
# will allow for some level of merging and history when
# collaborating with others.
#
# For best effect ensure any XMind file is in its own reasonbly-named
# directory within the repo.
#
# The script unpacks the contents of the XMind file into
# a directory sharing its name.
#
import sys, os, zipfile, subprocess

# Current directory when invoked by git is the top-level repo dir
curDir = os.getcwd()

# Create a list of all directories that include an XMind file
#
extns = ('.xmind')
matches = []

for root, dirnames, fns in os.walk(curDir):
  matches.extend(
    os.path.join(root, fn) for fn in fns if fn.lower().endswith(extns)
    )

# The content is a sub-directory that matches the name of the .xmind file
#
for fp in matches :

  # Unzip the file into the sub-directory overwriting anything that currently exists
  #
  print 'pre-commit> Found: ', fp

  # The sub-directory containing the contents of the unpacked XMind file
  basePath = os.path.dirname(fp)
  print 'pre-commit> Base Path: ', basePath

  contentDirName = os.path.splitext(os.path.basename(fp))[0]
  print 'pre-commit> Content Dir Name: ', contentDirName

  # Nuke the old directory
  os.chdir(basePath)
  if (os.path.isdir(contentDirName) and (contentDirName != '..')):
    print 'pre-commit> Deleting old dir: ', contentDirName
    cleanCmd = 'rm -rf ' + contentDirName
    try:
      cleanCode = os.popen(cleanCmd).read()
    except:
      print 'pre-commit> Clean failed: ', cleanCmd

  unpackDir = os.path.join(basePath, contentDirName)
  print 'pre-commit> Unpacking to: ', unpackDir

  # Make sure there is a .gitignore file next to the XMind file so
  # it isn't added to the repo
  #
  ignore = os.path.join(basePath, '.gitignore')

  if (not os.path.exists(ignore)) :
    with open(ignore, 'w') as f :
      imsg = '*.xmind\n'
      print 'pre-commit> Creating missing .gitignore file'
      f.write(imsg)

  unzipCmd = 'unzip ' + os.path.basename(fp) + ' -d ' + contentDirName
  print 'pre-commit> Unzip Cmd: ', unzipCmd
  try:
    unzipCode = os.popen(unzipCmd).read()
  except:
    print 'pre-commit> Unzip failed: ', unzipCmd, unzipCode

  # git add the current directory and subdirs
  #
  os.chdir(curDir)
  gitcmd = 'git add --all ' + basePath
  try:
    print 'pre-commit> Exec-ing: ', gitcmd
    gitcode = os.popen(gitcmd).read()
  except:
    print 'pre-commit> Git failed: ', gitcmd


