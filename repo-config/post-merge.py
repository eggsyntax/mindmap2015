#!/usr/bin/env python
#
# Simple git hook script that packages the
# unpacked contents of an xmind project
# into a single .xmind file
#

import sys, os, subprocess

# Current directory when invoked by git is the top-level repo dir
curDir = os.getcwd()
print 'post-merge> Current Dir: ', curDir

# Create a list of all directories that include the typical
# contents of an unpacked xmind project
#
extns = ('meta.xml', 'styles.xml', 'content.xml')
matches = []

for root, dirnames, fns in os.walk(curDir):
  matches.extend(
    os.path.join(root, fn) for fn in fns if fn.lower().endswith(extns)
    )

# Cull dups
#
xmDirs = []
for match in matches:
  d = os.path.dirname(match)
  if (not d in xmDirs):
    xmDirs.append(d)

# Remove any .xmind file that already exists and package the up contents
#
# Packaged XMind files are named after the unpacked directory and live
# in the parent dir
#
for d in xmDirs:

  print 'post-merge> Content Directory: ', d

  xmindExt = ('.xmind')
  zipName = os.path.basename(d) + '.xmind'
  zipPath = os.path.join(os.path.dirname(d), zipName)
  print 'post-merge> Creating: ', zipPath

  if (os.path.exists(zipPath)):
    try:
      os.remove(zipPath)
    except OSError, e:
      print 'post-merge> Not able to remove: ', mloc, e

  os.chdir(d)

  zipCmd = 'zip -r ../' + zipName + ' .'
  print 'post-merge> Exec''ing: ', zipCmd
  code2 = os.popen(zipCmd).read()






