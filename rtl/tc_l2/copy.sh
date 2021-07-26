#!/bin/bash

cp -r `ls | grep -v copy.sh | xargs` ../../../oscpu-dev-record/cpu
