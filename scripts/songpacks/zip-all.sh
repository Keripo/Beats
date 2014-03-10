#!/bin/sh

# File hiearchy:
# /mnt/sdb1
# /mnt/sdb1/Songs
# /mnt/sdb1/Songs/StepMix 5
# /mnt/sdb1/Songs/StepMix 5/blah/blah.sm
# /mnt/sdb1/zip-all.sh

zipdir="Songs-zipped"
mkdir "$zipdir"
mkdir "$zipdir"/"Songs"
for songsdir in "Songs"/*
do	
	mkdir "$zipdir"/"$songsdir"
	for song in "$songsdir"/*
	do
		zip -r "$song.zip" "$song"
		mv "$song.zip" "$zipdir"/"$songsdir"
	done
done