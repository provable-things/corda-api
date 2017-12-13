#!/usr/bin/env bash

arch[0]=linux_x86_64
arch[1]=win32_x86_64
arch[2]=macosx_x86_64

len=${#arch[@]}

for i in `seq 0 $((len - 1))`; do
    branch=${arch[$i]}
    echo "Setting $branch"
    git checkout $branch
    git merge master -m "updated to master"
    git push
done

git checkout master