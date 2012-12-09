#!/bin/sh

if [ ! -d src ]
then
	cd ..
fi

if [ -d src ]
then
	ctags-andrew -R src external
fi
