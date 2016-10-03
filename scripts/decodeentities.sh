#!/bin/bash

for f in $1/**/*
do
	if [ -f "$f" ]
	then
		cat "$f" | perl -MHTML::Entities -e 'while(<>) {print decode_entities($_);}' > "$f.txt"
		rm "$f"
	fi
done
